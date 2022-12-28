use super::{HistoryScroll, HistoryTypeBlockArray};
use crate::tools::{
    block_array::{BlockArray, ENTRIES},
    character::Character,
};
use libc::{c_void, memcpy, memset};
use std::{collections::HashMap, mem::size_of, rc::Rc};

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
