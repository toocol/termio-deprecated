#![allow(dead_code)]
pub mod scroll_block_array;
pub mod scroll_buffer;
pub mod scroll_compact;
pub mod scroll_file;
pub mod scroll_none;

pub use scroll_block_array::*;
pub use scroll_buffer::*;
pub use scroll_compact::*;
pub use scroll_file::*;
pub use scroll_none::*;

use super::character::Character;
use std::{cell::RefCell, rc::Rc};

const MAP_THRESHOLD: i32 = -1000;
const LINE_SIZE: usize = 1024;

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

///////////////////////// History Type
pub trait HistoryType {
    fn is_enabled(&self) -> bool;

    fn maximum_line_count(&self) -> i32;

    fn scroll(&self, old: Option<Rc<Box<dyn HistoryScrollWrapper>>>) -> Rc<Box<dyn HistoryScrollWrapper>>;

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

    fn scroll(&self, _: Option<Rc<Box<dyn HistoryScrollWrapper>>>) -> Rc<Box<dyn HistoryScrollWrapper>> {
        Rc::new(HistoryScrollNone::new().wrap())
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

    fn scroll(&self, _: Option<Rc<Box<dyn HistoryScrollWrapper>>>) -> Rc<Box<dyn HistoryScrollWrapper>> {
        Rc::new(HistoryScrollBlockArray::new(self.size).wrap())
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

    fn scroll(&self, old: Option<Rc<Box<dyn HistoryScrollWrapper>>>) -> Rc<Box<dyn HistoryScrollWrapper>> {
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
        Rc::new(scroll.wrap())
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

    fn scroll(&self, old: Option<Rc<Box<dyn HistoryScrollWrapper>>>) -> Rc<Box<dyn HistoryScrollWrapper>> {
        if let Some(old) = old {
            old.set_max_nb_lines(self.nb_lines);
            old
        } else {
            Rc::new(HistoryScrollBuffer::new(Some(self.nb_lines)).wrap())
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

    fn scroll(&self, old: Option<Rc<Box<dyn HistoryScrollWrapper>>>) -> Rc<Box<dyn HistoryScrollWrapper>> {
        if let Some(old) = old {
            old.set_max_nb_lines(self.nb_lines);
            old
        } else {
            Rc::new(CompactHistoryScroll::new(Some(self.nb_lines as i32)).wrap())
        }
    }
}
