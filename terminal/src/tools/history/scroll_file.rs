use super::{HistoryScroll, HistoryTypeFile, MAP_THRESHOLD};
use crate::tools::{
    character::Character,
    system_ffi::{mmap, munmap, MAP_FAILED, MAP_PRIVATE, PROT_READ, SEEK_SET},
};
use libc::{c_void, close, dup, fileno, lseek, read, tmpfile, write, FILE};
use log::error;
use std::{
    mem::size_of,
    ptr::{null, null_mut},
    rc::Rc,
    slice,
};

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
