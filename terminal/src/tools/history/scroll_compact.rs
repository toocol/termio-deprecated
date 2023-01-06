use super::{CompactHistoryType, HistoryScroll};
use crate::tools::{
    character::{Character, CharacterUnion},
    character_color::CharacterColor,
    system_ffi::{mmap, munmap, MAP_ANON, MAP_FAILED, MAP_PRIVATE, PROT_READ, PROT_WRITE},
};
use libc::c_void;
use std::{
    cell::RefCell,
    mem::size_of,
    ptr::{null, null_mut},
    rc::Rc,
    slice,
};

type TextLine = Vec<Character>;
/// History using compact storage
/// This implementation uses a list of fixed-sized blocks
/// where history lines are allocated in (avoids heap fragmentation)
pub struct CharacterFormat {
    fg_color: CharacterColor,
    bg_color: CharacterColor,
    start_pos: u16,
    rendition: u16,
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
