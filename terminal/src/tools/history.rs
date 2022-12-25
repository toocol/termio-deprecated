#![allow(dead_code)]
use crate::tools::character::CharacterUnion;

use super::{
    block_array::{BlockArray, ENTRIES},
    character::Character,
    character_color::CharacterColor,
    system_ffi::{
        mmap, munmap, MAP_ANON, MAP_FAILED, MAP_PRIVATE, PROT_READ, PROT_WRITE, SEEK_SET,
    },
};
use bitvec::vec::BitVec;
use libc::{c_void, close, dup, fileno, lseek, memcpy, memset, read, tmpfile, write, FILE};
use log::error;
use std::{
    cell::RefCell,
    collections::HashMap,
    mem::size_of,
    ptr::{null, null_mut},
    rc::Rc,
    slice,
};

const MAP_THRESHOLD: i32 = -1000;
const LINE_SIZE: usize = 1024;

/// An extendable tempfile based buffer.
pub struct HistoryFile {
    ion: i32,
    length: usize,
    tempfile: *mut FILE,

    /// Raw pointer to start of mmap'ed file data, or null() if the file is not mmap'ed
    file_map: *const u8,
    /// Array reference of mmap'ed file data
    file_map_bytes: Option<&'static [u8]>,

    /// Incremented whenver 'add' is called and decremented whenever 'get' is called.
    /// this is used to detect when a large number of lines are being read and
    /// processed from the history and automatically mmap the file for better
    /// performance (saves the overhead of many lseek-read calls).
    read_write_balance: i32,
}

impl HistoryFile {
    pub fn new() -> Self {
        let tempfile = unsafe { tmpfile() };
        Self {
            ion: unsafe { dup(fileno(tempfile)) },
            length: 0,
            tempfile,
            file_map: null(),
            file_map_bytes: None,
            read_write_balance: 0,
        }
    }

    pub fn add(&mut self, bytes: *const u8, len: i32) {
        if self.file_map != null() {
            self.unmap()
        }

        self.read_write_balance += 1;

        let mut rc;
        rc = unsafe { lseek(self.ion, self.length as i32, SEEK_SET) };
        if rc < 0 {
            error!("`HistoryFile` lseek failed");
            return;
        }

        rc = unsafe { write(self.ion, bytes as *const c_void, len as u32) };
        if rc < 0 {
            error!("`HistoryFile` write failed");
            return;
        }
        self.length += rc as usize;
    }

    pub fn get(&mut self, bytes: *mut u8, len: i32, loc: i32) {
        // count number of get() calls vs. number of add() calls.
        // If there are many more get() calls compared with add()
        // calls (decided by using MAP_THRESHOLD) then mmap the log
        // file to improve performance.
        self.read_write_balance -= 1;
        if self.file_map == null() && self.read_write_balance < MAP_THRESHOLD {
            self.map();
        }

        if self.file_map != null() {
            unsafe {
                slice::from_raw_parts_mut(bytes, len as usize)[0..len as usize].copy_from_slice(
                    &self.file_map_bytes.as_ref().unwrap()
                        [loc as usize..loc as usize + len as usize],
                );
            }
        } else {
            let mut rc;

            if loc < 0 || len < 0 || loc + len > self.length as i32 {
                error!("`HistoryFile` get(): invalid args: {}, {}", len, loc);
                return;
            }

            rc = unsafe { lseek(self.ion, loc, SEEK_SET) };
            if rc < 0 {
                error!("`HistoryFile` get(): lseek failed.");
                return;
            }
            rc = unsafe { read(self.ion, bytes as *mut c_void, len as u32) };
            if rc < 0 {
                error!("`HistoryFile` get(): lseek failed.");
                return;
            }
        }
    }

    pub fn len(&self) -> usize {
        self.length
    }

    /// mmaps the file in read-only mode
    pub fn map(&mut self) {
        assert!(self.file_map == null());

        self.file_map = mmap(null(), self.length, PROT_READ, MAP_PRIVATE, self.ion, 0);

        if self.file_map as *const c_void == MAP_FAILED {
            self.read_write_balance = 0;
            self.file_map = null();
            self.file_map_bytes = None;
            return;
        }

        self.file_map_bytes = Some(unsafe { slice::from_raw_parts(self.file_map, self.length) });
    }

    /// un-mmaps the file
    pub fn unmap(&mut self) {
        let result = munmap(self.file_map, self.length);
        assert_eq!(result, 0);
        self.file_map = null();
        self.file_map_bytes = None;
    }

    /// returns true if the file is mmap'ed
    pub fn is_mapped(&self) -> bool {
        self.file_map != null()
    }
}

impl Drop for HistoryFile {
    fn drop(&mut self) {
        if self.file_map != null() {
            self.unmap();
        }
        if self.tempfile != null_mut() {
            unsafe { close(self.ion) };
        }
    }
}

///////////////////////// History scroll
pub trait HistoryScroll: Sized + 'static {
    /// The type of history scroll
    type HistoryType: HistoryType;

    fn wrap(self) -> Box<dyn HistoryScrollWrapper> {
        Box::new(RefCell::new(self))
    }

    fn dynamic_cast_type<T: HistoryType>(&self) -> &mut T {
        unsafe { &mut *(self.get_type().as_ref() as *const Self::HistoryType as *mut T) }
    }

    fn has_scroll(&self) -> bool;

    fn get_lines(&self) -> i32;
    fn get_line_len(&mut self, lineno: i32) -> i32;
    fn get_cells(&mut self, lineno: i32, colno: i32, count: i32, res: &mut [Character]);
    fn is_wrapped_line(&mut self, lineno: i32) -> bool;

    ///  backward compatibility (obsolete)
    fn get_cell(&mut self, lineno: i32, colno: i32) -> Character {
        let mut res = [Character::default()];
        self.get_cells(lineno, colno, 1, &mut res);
        res[0]
    }

    /// adding lines.
    fn add_cells(&mut self, character: &[Character], count: i32);
    fn add_cells_list(&mut self, list: Vec<Character>) {
        self.add_cells(&list, list.len() as i32);
    }

    fn add_line(&mut self, previous_wrapped: bool);

    fn get_type(&self) -> Rc<Self::HistoryType>;

    fn set_max_nb_lines(&mut self, _: usize) {}
}
pub trait HistoryScrollWrapper {
    fn has_scroll(&self) -> bool;
    fn get_lines(&self) -> i32;
    fn get_line_len(&self, lineno: i32) -> i32;
    fn get_cells(&self, lineno: i32, colno: i32, count: i32, res: &mut [Character]);
    fn is_wrapped_line(&self, lineno: i32) -> bool;
    fn get_cell(&self, lineno: i32, colno: i32) -> Character;
    fn add_cells(&self, character: &[Character], count: i32);
    fn add_cells_list(&self, list: Vec<Character>);
    fn add_line(&self, previous_wrapped: bool);
    fn get_type(&self) -> Rc<dyn HistoryType>;
    fn set_max_nb_lines(&self, nb_lines: usize);
}
impl<T: HistoryScroll> HistoryScrollWrapper for RefCell<T> {
    fn has_scroll(&self) -> bool {
        self.borrow().has_scroll()
    }

    fn get_lines(&self) -> i32 {
        self.borrow().get_lines()
    }

    fn get_line_len(&self, lineno: i32) -> i32 {
        self.borrow_mut().get_line_len(lineno)
    }

    fn get_cells(&self, lineno: i32, colno: i32, count: i32, res: &mut [Character]) {
        self.borrow_mut().get_cells(lineno, colno, count, res)
    }

    fn is_wrapped_line(&self, lineno: i32) -> bool {
        self.borrow_mut().is_wrapped_line(lineno)
    }

    fn get_cell(&self, lineno: i32, colno: i32) -> Character {
        self.borrow_mut().get_cell(lineno, colno)
    }

    fn add_cells(&self, character: &[Character], count: i32) {
        self.borrow_mut().add_cells(character, count)
    }

    fn add_cells_list(&self, list: Vec<Character>) {
        self.borrow_mut().add_cells_list(list)
    }

    fn add_line(&self, previous_wrapped: bool) {
        self.borrow_mut().add_line(previous_wrapped)
    }

    fn get_type(&self) -> Rc<dyn HistoryType> {
        self.borrow().get_type()
    }

    fn set_max_nb_lines(&self, nb_lines: usize) {
        self.borrow_mut().set_max_nb_lines(nb_lines)
    }
}

///////////////////////////////////////////////////////////////////////
// History Scroll None (No history)
///////////////////////////////////////////////////////////////////////
pub struct HistoryScrollNone {
    history_type: Rc<HistoryTypeNone>,
}
impl HistoryScrollNone {
    pub fn new() -> Self {
        Self {
            history_type: Rc::new(HistoryTypeNone::new()),
        }
    }
}
impl HistoryScroll for HistoryScrollNone {
    type HistoryType = HistoryTypeNone;

    fn has_scroll(&self) -> bool {
        false
    }

    fn get_lines(&self) -> i32 {
        0
    }

    fn get_line_len(&mut self, _lineno: i32) -> i32 {
        0
    }

    fn get_cells(&mut self, _lineno: i32, _colno: i32, _count: i32, _res: &mut [Character]) {}

    fn is_wrapped_line(&mut self, _lineno: i32) -> bool {
        false
    }

    fn add_cells(&mut self, _character: &[Character], _count: i32) {}

    fn add_line(&mut self, _previous_wrapped: bool) {}

    fn get_type(&self) -> Rc<Self::HistoryType> {
        self.history_type.clone()
    }
}

////////////////////////////////////////////////////////////////////////
// File-based history (e.g. file log, no limitation in length)
////////////////////////////////////////////////////////////////////////
pub struct HistoryScrollFile {
    history_type: Rc<HistoryTypeFile>,

    log_file_name: String,
    /// lines Row(int)
    index: Box<HistoryFile>,
    /// text  Row(Character)
    cells: Box<HistoryFile>,
    /// flags Row(unsigned char)
    line_flags: Box<HistoryFile>,
}
/// The history scroll makes a Row(Row(Cell)) from
/// two history buffers. The index buffer contains
/// start of line positions which refere to the cells
/// buffer.
///
/// Note that index[0] addresses the second line
/// (line #1), while the first line (line #0) starts
/// at 0 in cells.
impl HistoryScrollFile {
    pub fn new(log_file_name: String) -> Self {
        Self {
            history_type: Rc::new(HistoryTypeFile::new(log_file_name.clone())),
            log_file_name: log_file_name,
            index: Box::new(HistoryFile::new()),
            cells: Box::new(HistoryFile::new()),
            line_flags: Box::new(HistoryFile::new()),
        }
    }

    fn start_of_line(&mut self, lineno: i32) -> i32 {
        if lineno <= 0 {
            return 0;
        }

        if lineno <= self.get_lines() {
            if !self.index.is_mapped() {
                self.index.map()
            }

            let mut res = 0;
            self.index.get(
                &mut res as *mut i32 as *mut u8,
                size_of::<i32>() as i32,
                (lineno - 1) * size_of::<i32>() as i32,
            );
            return res;
        }
        self.cells.len() as i32
    }
}
impl HistoryScroll for HistoryScrollFile {
    type HistoryType = HistoryTypeFile;

    fn has_scroll(&self) -> bool {
        true
    }

    fn get_lines(&self) -> i32 {
        self.index.len() as i32 / size_of::<i32>() as i32
    }

    fn get_line_len(&mut self, lineno: i32) -> i32 {
        (self.start_of_line(lineno + 1) - self.start_of_line(lineno))
            / size_of::<Character>() as i32
    }

    fn get_cells(&mut self, lineno: i32, colno: i32, count: i32, res: &mut [Character]) {
        let start_of_line = self.start_of_line(lineno);
        self.cells.get(
            res as *mut [Character] as *mut u8,
            count * size_of::<Character>() as i32,
            start_of_line + colno * size_of::<Character>() as i32,
        );
    }

    fn is_wrapped_line(&mut self, lineno: i32) -> bool {
        if lineno >= 0 && lineno <= self.get_lines() {
            let mut flag = 0u8;
            self.line_flags.get(
                &mut flag,
                size_of::<u8>() as i32,
                lineno * size_of::<u8>() as i32,
            );
            return flag > 0;
        }
        false
    }

    fn add_cells(&mut self, character: &[Character], count: i32) {
        self.cells.add(
            character as *const [Character] as *const u8,
            count * size_of::<Character>() as i32,
        );
    }

    fn add_line(&mut self, previous_wrapped: bool) {
        if self.index.is_mapped() {
            self.index.unmap()
        }

        let locn = self.cells.len();
        self.index
            .add(&locn as *const usize as *const u8, size_of::<i32>() as i32);
        let flags = if previous_wrapped { 0x01u8 } else { 0x00 };
        self.line_flags
            .add(&flags as *const u8, size_of::<u8>() as i32)
    }

    fn get_type(&self) -> Rc<Self::HistoryType> {
        self.history_type.clone()
    }
}

////////////////////////////////////////////////////////////////////////
// Buffer-based history (limited to a fixed nb of lines)
////////////////////////////////////////////////////////////////////////
type HistoryLine = Vec<Character>;
pub struct HistoryScrollBuffer {
    history_type: Rc<HistoryTypeBuffer>,

    history_buffer: Vec<HistoryLine>,
    wrapped_line: BitVec,
    max_line_count: usize,
    used_lines: usize,
    head: usize,
}
impl HistoryScrollBuffer {
    pub fn new(max_nb_lines: Option<usize>) -> Self {
        let max_nb_lines = if max_nb_lines.is_some() {
            max_nb_lines.unwrap()
        } else {
            1000
        };

        let mut scroll = Self {
            history_type: Rc::new(HistoryTypeBuffer::new(max_nb_lines)),
            history_buffer: vec![],
            wrapped_line: BitVec::new(),
            max_line_count: 0,
            used_lines: 0,
            head: 0,
        };
        scroll.set_max_nb_lines(scroll.max_line_count);
        scroll
    }

    fn buffer_index(&self, line_number: usize) -> usize {
        assert!(line_number < self.max_line_count);
        assert!(self.used_lines == self.max_line_count || line_number <= self.head);

        if self.used_lines == self.max_line_count {
            (self.head + line_number + 1) % self.max_line_count
        } else {
            line_number
        }
    }

    pub fn max_nb_lines(&self) -> usize {
        self.max_line_count
    }
}
impl HistoryScroll for HistoryScrollBuffer {
    type HistoryType = HistoryTypeBuffer;

    fn has_scroll(&self) -> bool {
        true
    }

    fn get_lines(&self) -> i32 {
        self.used_lines as i32
    }

    fn get_line_len(&mut self, lineno: i32) -> i32 {
        assert!(lineno >= 0 && lineno < self.max_line_count as i32);

        if lineno < self.used_lines as i32 {
            let buffer_index = self.buffer_index(lineno as usize);
            self.history_buffer[buffer_index].len() as i32
        } else {
            0
        }
    }

    fn get_cells(&mut self, lineno: i32, colno: i32, count: i32, res: &mut [Character]) {
        if count == 0 {
            return;
        }

        assert!(lineno < self.max_line_count as i32);

        if lineno >= self.used_lines as i32 {
            unsafe {
                memset(
                    res as *mut [Character] as *mut c_void,
                    0,
                    count as usize * size_of::<Character>(),
                )
            };
            return;
        }

        let buffer_index = self.buffer_index(lineno as usize);
        let line = &self.history_buffer[buffer_index];

        assert!(colno <= line.len() as i32 - count);

        unsafe {
            memcpy(
                res as *mut [Character] as *mut c_void,
                &line[colno as usize..] as *const [Character] as *const c_void,
                count as usize * size_of::<Character>(),
            )
        };
    }

    fn is_wrapped_line(&mut self, lineno: i32) -> bool {
        assert!(lineno >= 0 && lineno < self.max_line_count as i32);

        if lineno < self.used_lines as i32 {
            let buffer_index = self.buffer_index(lineno as usize);
            let opt = self.wrapped_line.get(buffer_index);
            if let Some(bit) = opt {
                bit == true
            } else {
                false
            }
        } else {
            false
        }
    }

    fn add_cells(&mut self, character: &[Character], _count: i32) {
        let new_line = character.to_vec();
        self.add_cells_list(new_line);
    }

    fn add_cells_list(&mut self, list: Vec<Character>) {
        self.head += 1;
        if self.used_lines < self.max_line_count {
            self.used_lines += 1;
        }
        if self.head >= self.max_line_count {
            self.head = 0;
        }

        let buffer_index = self.buffer_index(self.used_lines - 1);
        self.history_buffer[buffer_index] = list;
        self.wrapped_line.set(buffer_index, false);
    }

    fn add_line(&mut self, previous_wrapped: bool) {
        let buffer_index = self.buffer_index(self.used_lines - 1);
        self.wrapped_line.set(buffer_index, previous_wrapped);
    }

    fn get_type(&self) -> Rc<Self::HistoryType> {
        self.history_type.clone()
    }

    fn set_max_nb_lines(&mut self, nb_lines: usize) {
        let old_buffer = &self.history_buffer;
        let mut new_buffer = vec![vec![]; nb_lines];

        for i in 0..self.used_lines.min(nb_lines) {
            new_buffer[i] = old_buffer.get(self.buffer_index(i)).unwrap().to_owned()
        }

        self.used_lines = self.used_lines.min(nb_lines);
        self.max_line_count = nb_lines;
        self.head = if self.used_lines == self.max_line_count {
            0
        } else {
            self.used_lines - 1
        };

        self.history_buffer = new_buffer;
        self.wrapped_line.resize(nb_lines, false);
        self.dynamic_cast_type::<HistoryTypeBuffer>().nb_lines = nb_lines;
    }
}

////////////////////////////////////////////////////////////////////////
/// BlockArray-based history
////////////////////////////////////////////////////////////////////////
pub struct HistoryScrollBlockArray {
    history_type: Rc<HistoryTypeBlockArray>,

    block_array: BlockArray,
    line_lengths: HashMap<i32, usize>,
}
impl HistoryScrollBlockArray {
    pub fn new(size: usize) -> Self {
        Self {
            history_type: Rc::new(HistoryTypeBlockArray::new(size)),
            block_array: BlockArray::new(),
            line_lengths: HashMap::new(),
        }
    }
}
impl HistoryScroll for HistoryScrollBlockArray {
    type HistoryType = HistoryTypeBlockArray;

    fn has_scroll(&self) -> bool {
        true
    }

    fn get_lines(&self) -> i32 {
        self.line_lengths.len() as i32
    }

    fn get_line_len(&mut self, lineno: i32) -> i32 {
        if self.line_lengths.contains_key(&lineno) {
            self.line_lengths[&lineno] as i32
        } else {
            0
        }
    }

    fn get_cells(&mut self, lineno: i32, colno: i32, count: i32, res: &mut [Character]) {
        if count <= 0 {
            return;
        }

        let block = self.block_array.at(lineno as usize);
        if let Some(block) = block {
            assert!((colno as usize + count as usize) * size_of::<Character>() < ENTRIES);
            unsafe {
                memcpy(
                    res as *mut [Character] as *mut c_void,
                    &block.data[colno as usize * size_of::<Character>()..] as *const [u8]
                        as *const c_void,
                    count as usize * size_of::<Character>(),
                );
            }
        } else {
            unsafe {
                memset(
                    res as *mut [Character] as *mut c_void,
                    0,
                    count as usize * size_of::<Character>(),
                )
            };
        }
    }

    fn is_wrapped_line(&mut self, _: i32) -> bool {
        false
    }

    fn add_cells(&mut self, character: &[Character], count: i32) {
        let block = self.block_array.last_block();

        if let Some(block) = block {
            assert!(count as usize * size_of::<Character>() < ENTRIES);
            unsafe {
                memset(block.data.as_ptr() as *mut c_void, 0, block.data.len());
                memcpy(
                    block.data.as_ptr() as *mut c_void,
                    character as *const [Character] as *const c_void,
                    count as usize * size_of::<Character>(),
                );
            }
            block.size = count as usize * size_of::<Character>();
            let res = self.block_array.new_block();
            assert!(res > 0);
            self.line_lengths
                .insert(self.block_array.get_current(), count as usize);
        }
    }

    fn add_line(&mut self, _: bool) {}

    fn get_type(&self) -> Rc<Self::HistoryType> {
        self.history_type.clone()
    }
}

type TextLine = Vec<Character>;
/// History using compact storage
/// This implementation uses a list of fixed-sized blocks
/// where history lines are allocated in (avoids heap fragmentation)
pub struct CharacterFormat {
    fg_color: CharacterColor,
    bg_color: CharacterColor,
    start_pos: u16,
    rendition: u8,
}
impl CharacterFormat {
    pub fn new(c: &Character) -> Self {
        Self {
            fg_color: c.foreground_color,
            bg_color: c.background_color,
            start_pos: 0,
            rendition: c.rendition,
        }
    }

    pub fn equals_with_format(&self, other: &CharacterFormat) -> bool {
        other.rendition == self.rendition
            && other.fg_color == self.fg_color
            && other.bg_color == self.bg_color
    }

    pub fn equals_with_character(&self, other: &Character) -> bool {
        other.rendition == self.rendition
            && other.foreground_color == self.fg_color
            && other.background_color == self.bg_color
    }

    pub fn set_format(&mut self, c: &Character) {
        self.rendition = c.rendition;
        self.fg_color = c.foreground_color;
        self.bg_color = c.background_color;
    }
}

pub struct CompactHistoryBlock {
    block_length: usize,
    head: *mut u8,
    tail: *mut u8,
    block_start: *mut u8,
    alloc_count: i32,
}
impl CompactHistoryBlock {
    pub fn new() -> Self {
        let block_length = 4096usize * 64;
        let ptr = mmap(
            null(),
            block_length,
            PROT_READ | PROT_WRITE,
            MAP_PRIVATE | MAP_ANON,
            -1,
            0,
        ) as *mut u8;
        assert!(ptr as *const c_void != MAP_FAILED);
        Self {
            block_length: block_length,
            head: ptr,
            tail: ptr,
            block_start: ptr,
            alloc_count: 0,
        }
    }

    pub fn remaining(&self) -> u32 {
        self.block_start as u32 + self.block_length as u32 - self.tail as u32
    }

    pub fn length(&self) -> usize {
        self.block_length
    }

    pub fn allocate(&mut self, length: usize) -> *mut c_void {
        if self.tail as usize - self.block_start as usize + length > self.block_length {
            return null_mut();
        }

        let block = self.tail;
        self.tail = unsafe { self.tail.add(length) };
        self.alloc_count += 1;
        block as *mut c_void
    }

    pub fn contains(&self, addr: *const c_void) -> bool {
        addr as i32 >= self.block_start as i32
            && (self.block_start as i32 + self.block_length as i32) > addr as i32
    }

    pub fn deallocate(&mut self) {
        self.alloc_count -= 1;
        assert!(self.alloc_count >= 0)
    }

    pub fn is_in_use(&self) -> bool {
        self.alloc_count != 0
    }
}
impl Drop for CompactHistoryBlock {
    fn drop(&mut self) {
        munmap(self.block_start, self.block_length);
    }
}

pub struct CompactHistoryBlockList {
    list: Vec<RefCell<CompactHistoryBlock>>,
}
impl CompactHistoryBlockList {
    pub fn new() -> Self {
        Self { list: vec![] }
    }
    pub fn allocate(&mut self, size: usize) -> *mut c_void {
        if self.list.is_empty() || self.list.last().unwrap().borrow().remaining() < size as u32 {
            let b = RefCell::new(CompactHistoryBlock::new());
            self.list.push(b);
        }
        let block = self.list.last().unwrap();
        block.borrow_mut().allocate(size)
    }

    pub fn deallocate(&mut self, ptr: *mut c_void) {
        assert!(!self.list.is_empty());

        let mut i = 0;
        let mut block = self.list.get(i).unwrap();
        while i < self.list.len() && !block.borrow().contains(ptr) {
            i += 1;
            block = self.list.get(i).unwrap();
        }

        assert!(i < self.list.len());

        block.borrow_mut().deallocate();

        if !block.borrow().is_in_use() {
            self.list.remove(i);
        }
    }

    pub fn length(&self) -> usize {
        self.list.len()
    }
}

pub struct CompactHistoryLine {
    block_list: CompactHistoryBlockList,
    format_array: *mut CharacterFormat,
    format_array_ref: Option<&'static mut [CharacterFormat]>,
    length: usize,
    format_length: usize,
    text: *mut u16,
    text_ref: Option<&'static mut [u16]>,
    wrapped: bool,
}
impl CompactHistoryLine {
    pub fn new(size: usize, block_list: &mut CompactHistoryBlockList) -> *mut c_void {
        block_list.allocate(size)
    }

    pub fn create(line: TextLine, mut block_list: CompactHistoryBlockList) -> Self {
        let length = line.len();
        let mut format_length = 0usize;
        let mut format_array = null_mut();
        let mut format_array_ref: Option<&'static mut [CharacterFormat]> = None;
        let mut text = null_mut();
        let mut text_ref: Option<&'static mut [u16]> = None;
        let wrapped = false;

        // count number of different formats in this text line
        let mut c = &line[0];
        if !line.is_empty() {
            format_length = 1;
            let mut k = 1usize;
            while k < length {
                if !line[k].equals_format(c) {
                    format_length += 1;
                    c = &line[k];
                }
                k += 1;
            }

            format_array = block_list
                .allocate(size_of::<CharacterFormat>() * format_length)
                .cast::<CharacterFormat>();
            assert!(format_array != null_mut());
            format_array_ref =
                Some(unsafe { slice::from_raw_parts_mut(format_array, format_length) });

            text = block_list
                .allocate(size_of::<u16>() * line.len())
                .cast::<u16>();
            assert!(text != null_mut());
            text_ref = Some(unsafe { slice::from_raw_parts_mut(text, line.len()) });

            // record formats and their positions in the format array
            c = &line[0];
            format_array_ref.as_mut().unwrap()[0].set_format(c);
            format_array_ref.as_mut().unwrap()[0].start_pos = 0;

            k = 1;
            let mut j = 1usize;
            while k < length && j < format_length {
                if !&line[k].equals_format(c) {
                    c = &line[k];
                    format_array_ref.as_mut().unwrap()[j].set_format(c);
                    format_array_ref.as_mut().unwrap()[j].start_pos = k as u16;
                    j += 1;
                }
                k += 1;
            }

            for i in 0..line.len() {
                text_ref.as_mut().unwrap()[i] = line[i].character_union.into();
            }
        }

        Self {
            block_list,
            format_array,
            format_array_ref,
            length,
            text,
            text_ref,
            format_length,
            wrapped,
        }
    }

    pub fn get_characters(&self, array: &mut [Character], length: usize, start_column: i32) {
        assert!(start_column >= 0);
        assert!(start_column as usize + length <= self.get_length());
        for i in start_column as usize..length + start_column as usize {
            self.get_character(i, &mut array[i - start_column as usize]);
        }
    }

    pub fn get_character(&self, index: usize, r: &mut Character) {
        assert!(index < self.length);
        let mut format_pos = 0usize;
        while format_pos + 1 < self.format_length
            && index >= self.format_array_ref.as_ref().unwrap()[format_pos + 1].start_pos as usize
        {
            format_pos += 1;
        }

        r.character_union = CharacterUnion::from(self.text_ref.as_ref().unwrap()[index]);
        r.rendition = self.format_array_ref.as_ref().unwrap()[format_pos].rendition;
        r.foreground_color = self.format_array_ref.as_ref().unwrap()[format_pos].fg_color;
        r.background_color = self.format_array_ref.as_ref().unwrap()[format_pos].bg_color;
    }

    pub fn is_wrapped(&self) -> bool {
        self.wrapped
    }

    pub fn set_wrapped(&mut self, wrapped: bool) {
        self.wrapped = wrapped
    }

    pub fn get_length(&self) -> usize {
        self.length
    }
}
impl Drop for CompactHistoryLine {
    fn drop(&mut self) {
        if self.length > 0 {
            self.block_list.deallocate(self.text as *mut c_void);
            self.block_list.deallocate(self.format_array as *mut c_void);
        }
        let ptr = self as *mut Self as *mut c_void;
        self.block_list.deallocate(ptr);
    }
}

type HistoryArray = Vec<CompactHistoryLine>;
////////////////////////////////////////////////////////////////////////
/// compact history scroll
////////////////////////////////////////////////////////////////////////
pub struct CompactHistoryScroll {
    history_type: Rc<CompactHistoryType>,

    lines: HistoryArray,
    block_list: CompactHistoryBlockList,

    max_line_count: u32,
}
impl CompactHistoryScroll {
    pub fn new(max_nb_lines: Option<i32>) -> Self {
        let max_nb_lines = if let Some(line) = max_nb_lines {
            line
        } else {
            1000
        };

        let mut scroll = Self {
            history_type: Rc::new(CompactHistoryType::new(max_nb_lines as usize)),
            lines: vec![],
            block_list: CompactHistoryBlockList::new(),
            max_line_count: 0,
        };
        scroll.set_max_nb_lines(max_nb_lines as usize);
        scroll
    }

    pub fn max_nb_lines(&self) -> u32 {
        self.max_line_count
    }
}
impl HistoryScroll for CompactHistoryScroll {
    type HistoryType = CompactHistoryType;

    fn has_scroll(&self) -> bool {
        true
    }

    fn get_lines(&self) -> i32 {
        self.lines.len() as i32
    }

    fn get_line_len(&mut self, lineno: i32) -> i32 {
        assert!(lineno >= 0 && lineno < self.lines.len() as i32);
        let line = &self.lines[lineno as usize];
        line.get_length() as i32
    }

    fn get_cells(&mut self, lineno: i32, colno: i32, count: i32, res: &mut [Character]) {
        if count == 0 {
            return;
        }
        assert!(lineno < self.lines.len() as i32);
        let line = &self.lines[lineno as usize];
        assert!(colno >= 0);
        assert!(colno <= line.get_length() as i32 - count);
        line.get_characters(res, count as usize, colno);
    }

    fn is_wrapped_line(&mut self, lineno: i32) -> bool {
        assert!(lineno < self.lines.len() as i32);
        (&self.lines[lineno as usize]).is_wrapped()
    }

    fn add_cells(&mut self, character: &[Character], count: i32) {
        let mut new_line = vec![Character::default(); count as usize];
        new_line.copy_from_slice(&character[0..count as usize])
    }

    fn add_line(&mut self, previous_wrapped: bool) {
        let line = self.lines.last_mut();
        if let Some(line) = line {
            line.set_wrapped(previous_wrapped)
        }
    }

    fn get_type(&self) -> Rc<Self::HistoryType> {
        self.history_type.clone()
    }

    fn set_max_nb_lines(&mut self, nb_lines: usize) {
        self.max_line_count = nb_lines as u32;

        while self.lines.len() > nb_lines as usize {
            self.lines.remove(0);
        }
    }
}

///////////////////////// History Type
pub trait HistoryType {
    fn is_enabled(&self) -> bool;

    fn maximum_line_count(&self) -> i32;

    fn scroll(self, old: Option<Box<dyn HistoryScrollWrapper>>) -> Box<dyn HistoryScrollWrapper>;

    fn is_unlimited(&self) -> bool {
        self.maximum_line_count() == 0
    }
}

pub struct HistoryTypeNone;
impl HistoryTypeNone {
    pub fn new() -> Self {
        Self {}
    }
}
impl HistoryType for HistoryTypeNone {
    fn is_enabled(&self) -> bool {
        false
    }

    fn maximum_line_count(&self) -> i32 {
        0
    }

    fn scroll(self, _: Option<Box<dyn HistoryScrollWrapper>>) -> Box<dyn HistoryScrollWrapper> {
        HistoryScrollNone::new().wrap()
    }
}

pub struct HistoryTypeBlockArray {
    size: usize,
}
impl HistoryTypeBlockArray {
    pub fn new(size: usize) -> Self {
        Self { size }
    }
}
impl HistoryType for HistoryTypeBlockArray {
    fn is_enabled(&self) -> bool {
        true
    }

    fn maximum_line_count(&self) -> i32 {
        self.size as i32
    }

    fn scroll(self, _: Option<Box<dyn HistoryScrollWrapper>>) -> Box<dyn HistoryScrollWrapper> {
        HistoryScrollBlockArray::new(self.size).wrap()
    }
}

pub struct HistoryTypeFile {
    file_name: String,
}
impl HistoryTypeFile {
    pub fn new(file_name: String) -> Self {
        Self { file_name }
    }
}
impl HistoryTypeFile {
    pub fn get_file_name(&self) -> &str {
        &self.file_name
    }
}
impl HistoryType for HistoryTypeFile {
    fn is_enabled(&self) -> bool {
        true
    }

    fn maximum_line_count(&self) -> i32 {
        0
    }

    fn scroll(self, old: Option<Box<dyn HistoryScrollWrapper>>) -> Box<dyn HistoryScrollWrapper> {
        let mut scroll = HistoryScrollFile::new(self.file_name.clone());
        let mut line = [Character::default(); LINE_SIZE];
        let lines = if old.is_some() {
            old.as_ref().unwrap().get_lines() as usize
        } else {
            0
        };
        for i in 0..lines {
            let size = old.as_ref().unwrap().get_line_len(i as i32);
            if size > LINE_SIZE as i32 {
                let mut tmp_line = vec![Character::default(); size as usize];
                old.as_ref()
                    .unwrap()
                    .get_cells(i as i32, 0, size, &mut tmp_line);
                scroll.add_cells(&tmp_line, size);
                scroll.add_line(old.as_ref().unwrap().is_wrapped_line(i as i32));
            } else {
                old.as_ref()
                    .unwrap()
                    .get_cells(i as i32, 0, size, &mut line);
                scroll.add_cells(&line, size);
                scroll.add_line(old.as_ref().unwrap().is_wrapped_line(i as i32));
            }
        }
        scroll.wrap()
    }
}

pub struct HistoryTypeBuffer {
    nb_lines: usize,
}
impl HistoryTypeBuffer {
    pub fn new(nb_lines: usize) -> Self {
        Self { nb_lines }
    }
}
impl HistoryType for HistoryTypeBuffer {
    fn is_enabled(&self) -> bool {
        true
    }

    fn maximum_line_count(&self) -> i32 {
        self.nb_lines as i32
    }

    fn scroll(self, old: Option<Box<dyn HistoryScrollWrapper>>) -> Box<dyn HistoryScrollWrapper> {
        if let Some(old) = old {
            old.set_max_nb_lines(self.nb_lines);
            old
        } else {
            HistoryScrollBuffer::new(Some(self.nb_lines)).wrap()
        }
    }
}

pub struct CompactHistoryType {
    nb_lines: usize,
}
impl CompactHistoryType {
    pub fn new(size: usize) -> Self {
        Self { nb_lines: size }
    }
}
impl HistoryType for CompactHistoryType {
    fn is_enabled(&self) -> bool {
        true
    }

    fn maximum_line_count(&self) -> i32 {
        self.nb_lines as i32
    }

    fn scroll(self, old: Option<Box<dyn HistoryScrollWrapper>>) -> Box<dyn HistoryScrollWrapper> {
        if let Some(old) = old {
            old.set_max_nb_lines(self.nb_lines);
            old
        } else {
            CompactHistoryScroll::new(Some(self.nb_lines as i32)).wrap()
        }
    }
}
