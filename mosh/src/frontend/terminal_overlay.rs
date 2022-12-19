#![allow(dead_code)]

use std::{cell::RefCell, rc::Rc};

use crate::{statesync::UserEvent, terminal::Emulator};
//////////////////////// OverlayManager
pub struct OverlayManager {
    pub emulator: Rc<RefCell<Emulator>>,
}

impl OverlayManager {
    pub fn new(emulator: Rc<RefCell<Emulator>>) -> Self {
        OverlayManager { emulator }
    }

    pub fn read(&self) -> Option<UserEvent> {
        self.emulator.borrow().read()
    }

    pub fn terminal_size_aware(&self) -> Option<UserEvent> {
        self.emulator.borrow_mut().terminal_size_aware()
    }
}
