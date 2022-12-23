#![allow(dead_code)]
use super::system_ffi::{chsize, mmap, munmap, MAP_PRIVATE, PROT_READ, SEEK_SET, MAP_FAILED};
use libc::{
    c_void, close, dup, fclose, fdopen, fileno, fread, fseek, fwrite, lseek, tmpfile, write, FILE,
};
use log::error;
use std::{
    ffi::CString,
    mem::size_of,
    ptr::{null, null_mut},
    sync::atomic::{AtomicI32, Ordering},
};

const CONST_BLOCK_SIZE: usize = 1 << 12;
pub const ENTRIES: usize = (CONST_BLOCK_SIZE - size_of::<usize>()) / size_of::<u8>();

static BLOCK_SIZE: AtomicI32 = AtomicI32::new(0);

#[repr(C)]
#[derive(Debug, Clone, Copy)]
pub struct Block {
    pub data: [u8; ENTRIES],
    pub size: usize,
}
impl Block {
    pub fn new() -> Self {
        Self {
            data: [0u8; ENTRIES],
            size: 0,
        }
    }
}

/// Creates a history file for holding maximal size blocks. If more blocks
/// are requested, then it drops earlier added ones.
pub struct BlockArray {
    size: usize,
    /// `current` always shows to the last inserted block
    current: i32,
    index: i32,

    last_map: Option<Block>,
    last_map_index: i32,
    last_block: Option<Block>,

    ion: i32,
    length: usize,
}

impl BlockArray {
    pub fn new() -> Self {
        if BLOCK_SIZE.load(Ordering::SeqCst) == 0 {
            let page_size = page_size::get();
            let block_size = ((size_of::<Block>() / page_size) + 1) * page_size;
            BLOCK_SIZE.store(block_size as i32, Ordering::SeqCst);
        }

        Self {
            size: 0,
            current: -1,
            index: -1,
            last_map: None,
            last_map_index: -1,
            last_block: None,
            ion: -1,
            length: 0,
        }
    }

    /// adds the Block at the end of history. This may drop other blocks.
    ///
    /// The ownership on the block is transfered.
    /// An unique index number is returned for accessing it later (if not yet dropped then)
    ///
    /// Note, that the block may be dropped completely if history is turned off.
    pub fn append(&mut self) -> i32 {
        if self.size <= 0 {
            return -1;
        }

        self.current += 1;
        if self.current >= self.size as i32 {
            self.current = 0
        }

        let mut rc = unsafe {
            lseek(
                self.ion,
                self.current * BLOCK_SIZE.load(Ordering::SeqCst),
                SEEK_SET,
            )
        };

        if rc < 0 {
            self.set_history_size(0);
            return -1;
        }

        let ptr = self.last_block.as_ref().unwrap() as *const Block as *const c_void;
        rc = unsafe { write(self.ion, ptr, BLOCK_SIZE.load(Ordering::SeqCst) as u32) };

        if rc < 0 {
            self.set_history_size(0);
            return -1;
        }

        self.length += 1;
        if self.length > self.size {
            self.length = self.size;
        }

        self.index += 1;
        self.current
    }

    /// gets the block at the index.
    /// Function may return 0 if the block isn't available any more.
    ///
    /// The returned block is strictly readonly as only
    /// maped in memory - and will be invalid on the next
    /// operation on this class.
    pub fn at(&mut self, i: usize) -> Option<&Block> {
        if i == self.index as usize + 1 {
            return Some(&self.last_block.as_ref().unwrap());
        }

        if i == self.last_map_index as usize {
            return Some(self.last_map.as_ref().unwrap());
        }

        if i > self.index as usize {
            error!("BlockArray `at()`, index overflow.");
            return None;
        }

        let j = i;
        self.unmap();

        let block = mmap(
            null(),
            BLOCK_SIZE.load(Ordering::SeqCst) as usize,
            PROT_READ,
            MAP_PRIVATE,
            self.ion,
            j as i64 * BLOCK_SIZE.load(Ordering::SeqCst) as i64,
        );
        if block as *const c_void == MAP_FAILED {
            error!("`mmap` failed.");
            return None
        }
        let block = unsafe { *(block as *const Block) };

        self.last_map = Some(block);
        self.last_map_index = i as i32;

        Some(self.last_map.as_ref().unwrap())
    }

    /// reorders blocks as needed. If newsize is 0, the history is emptied completely.
    /// The indices returned on append won't change their semantic,
    /// but they may not be valid after this call.
    pub fn set_history_size(&mut self, new_size: usize) -> bool {
        if self.size == new_size {
            return false;
        }

        self.unmap();

        if new_size == 0 {
            self.last_block = None;
            if self.ion >= 0 {
                unsafe { close(self.ion) };
            }
            self.ion = -1;
            self.current = -1;
            return true;
        }

        if self.size == 0 {
            let tmp = unsafe { tmpfile() };
            self.ion = unsafe { dup(fileno(tmp)) };
            if self.ion < 0 {
                unsafe { fclose(tmp) };
                return false;
            }

            assert!(self.last_block.is_none());

            self.last_block = Some(Block::new());
            self.size = new_size;
            return false;
        }

        if new_size > self.size {
            self.increase_buffer();
            self.size = new_size;
            false
        } else {
            self.decrease_buffer(new_size);
            chsize(
                self.ion,
                self.length as i32 * BLOCK_SIZE.load(Ordering::SeqCst),
            );
            self.size = new_size;
            true
        }
    }

    pub fn new_block(&mut self) -> i32 {
        if self.size <= 0 {
            return -1;
        }
        self.append();

        self.last_block = Some(Block::new());
        self.index + 1
    }

    pub fn last_block(&mut self) -> Option<&mut Block> {
        if let Some(block) = self.last_block.as_mut() {
            Some(block)
        } else {
            None
        }
    }

    /// Convenient function to set the size in KBytes instead of blocks.
    pub fn set_size(&mut self, new_size: usize) -> bool {
        self.set_history_size(new_size)
    }

    pub fn len(&self) -> usize {
        self.length
    }

    pub fn has(&self, i: usize) -> bool {
        if i == self.index as usize + 1 {
            return true;
        }

        if i > self.index as usize {
            return false;
        }
        if self.index as usize - i >= self.length {
            return false;
        }
        true
    }

    pub fn get_current(&self) -> i32 {
        self.current
    }

    fn unmap(&mut self) {
        if self.last_map.is_some() {
            let ptr = self.last_map.as_ref().unwrap() as *const Block as *const u8;
            let res = munmap(ptr, BLOCK_SIZE.load(Ordering::SeqCst) as usize);
            if res < 0 {
                error!("munmap() failed.")
            }
        }
        self.last_map = None;
        self.last_map_index = -1;
    }

    #[allow(unused_assignments)]
    fn increase_buffer(&mut self) {
        if self.index < self.size as i32 {
            return;
        }

        let offset = (self.current + self.size as i32 + 1) % self.size as i32;
        if offset <= 0 {
            return;
        }

        // The Block constructor could do somthing in future...
        let buffer1 = vec![0u8; BLOCK_SIZE.load(Ordering::SeqCst) as usize];
        let buffer2 = vec![0u8; BLOCK_SIZE.load(Ordering::SeqCst) as usize];

        let mut runs = 1;
        let mut bpr = self.size as i32;

        if self.size as i32 % offset == 0 {
            bpr = self.size as i32 / offset;
            runs = offset;
        }

        let mode = CString::new("w+b").unwrap();
        let fion = unsafe { fdopen(dup(self.ion), mode.as_ptr()) };
        if fion == null_mut() {
            error!("`fdopen/dup` failed.");
            return;
        }

        let mut res;
        for i in 0..runs {
            let first_block = (offset + i) % self.size as i32;
            res = unsafe {
                fseek(
                    fion,
                    first_block * BLOCK_SIZE.load(Ordering::SeqCst),
                    SEEK_SET,
                )
            };
            if res > 0 {
                error!("`fseek` result > 0")
            }
            res = unsafe {
                fread(
                    buffer1.as_ptr() as *mut c_void,
                    BLOCK_SIZE.load(Ordering::SeqCst) as usize,
                    1,
                    fion,
                ) as i32
            };
            if res != 1 {
                error!("`fread` result != 1")
            }

            let mut newpos = 0;
            let mut cursor = first_block;
            for _ in 1..bpr {
                cursor = (cursor + offset) % self.size as i32;
                newpos = (cursor - offset + self.size as i32) % self.size as i32;
                self.move_block(fion, cursor, newpos, buffer2.as_ptr());
            }
            res = unsafe { fseek(fion, i * BLOCK_SIZE.load(Ordering::SeqCst), SEEK_SET) };
            if res > 0 {
                error!("`fseek` result > 0")
            }
            res = unsafe {
                fwrite(
                    buffer1.as_ptr() as *mut c_void,
                    BLOCK_SIZE.load(Ordering::SeqCst) as usize,
                    1usize,
                    fion,
                ) as i32
            };
            if res != 1 {
                error!("`fwrite` result != 1")
            }
        }

        self.current = self.size as i32 - 1;
        self.length = self.size;

        unsafe { fclose(fion) };
    }

    fn decrease_buffer(&mut self, new_size: usize) {
        if self.index < new_size as i32 {
            return;
        }

        let offset = (self.current - (new_size as i32 - 1) + self.size as i32) % self.size as i32;
        if offset <= 0 {
            return;
        }

        let buffer1 = vec![0u8; BLOCK_SIZE.load(Ordering::SeqCst) as usize];

        let mode = CString::new("w+b").unwrap();
        let fion = unsafe { fdopen(dup(self.ion), mode.as_ptr()) };
        if fion == null_mut() {
            error!("`fdopen/dup` failed.");
            return;
        }

        let first_block;
        if self.current <= new_size as i32 {
            first_block = self.current + 1;
        } else {
            first_block = 0;
        }

        let mut old_pos;
        let mut cursor = first_block;
        for _ in 0..new_size {
            old_pos = (self.size as i32 + cursor + offset) % self.size as i32;
            self.move_block(fion, old_pos, cursor, buffer1.as_ptr());
            if old_pos < new_size as i32 {
                cursor = old_pos;
            } else {
                cursor += 1;
            }
        }

        self.current = new_size as i32 - 1;
        self.length = new_size;

        unsafe { fclose(fion) };
    }

    fn move_block(&self, fion: *mut FILE, cursor: i32, newpos: i32, buffer2: *const u8) {
        let res = unsafe { fseek(fion, cursor * BLOCK_SIZE.load(Ordering::SeqCst), SEEK_SET) };
        if res > 0 {
            error!("`fseek` result > 0")
        }
        let res = unsafe {
            fread(
                buffer2 as *mut c_void,
                BLOCK_SIZE.load(Ordering::SeqCst) as usize,
                1usize,
                fion,
            )
        };
        if res != 1 {
            error!("`fread` result != 1")
        }

        let res = unsafe { fseek(fion, newpos * BLOCK_SIZE.load(Ordering::SeqCst), SEEK_SET) };
        if res > 0 {
            error!("`fseek` result > 0")
        }
        let res = unsafe {
            fread(
                buffer2 as *mut c_void,
                BLOCK_SIZE.load(Ordering::SeqCst) as usize,
                1usize,
                fion,
            )
        };
        if res != 1 {
            error!("`fread` result != 1")
        }
    }
}
