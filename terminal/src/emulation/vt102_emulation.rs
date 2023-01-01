use super::{Emulation, EmulationStorage};
use crate::{
    core::screen_window::ScreenWindow,
    tools::{history::HistoryType, terminal_character_decoder::TerminalCharacterDecoder},
};
use std::{cell::RefCell, rc::Rc};
use tmui::{graphics::figure::Size, prelude::*};
use wchar::wchar_t;

pub struct VT102Emulation {
    emulation: EmulationStorage,
}

impl Emulation for VT102Emulation {
    fn create_window(&self) -> Rc<RefCell<Box<ScreenWindow>>> {
        self.emulation.create_window()
    }

    fn image_size(&self) -> Size {
        self.emulation.image_size()
    }

    fn line_count(&self) -> i32 {
        self.emulation.line_count()
    }

    fn set_history(&mut self, history_type: Box<dyn HistoryType>) {
        self.emulation.set_history(history_type)
    }

    fn history(&self) -> Rc<Box<dyn HistoryType>> {
        self.emulation.history()
    }

    fn clear_history(&mut self) {
        self.emulation.clear_history()
    }

    fn write_to_stream(
        &mut self,
        decoder: &mut dyn TerminalCharacterDecoder,
        start_line: i32,
        end_line: i32,
    ) {
        self.emulation.write_to_stream(decoder, start_line, end_line)
    }

    fn erase_char(&self) -> char {
        self.emulation.erase_char()
    }

    fn set_keyboard_layout<T: ToString>(&mut self, name: T) {
        self.emulation.set_keyboard_layout(name)
    }

    fn keyboard_layout(&self) -> String {
        self.emulation.keyboard_layout()
    }

    fn clear_entire_screen(&mut self) {
        self.emulation.clear_entire_screen()
    }

    fn reset(&mut self) {
        self.emulation.reset()
    }

    fn program_use_mouse(&self) -> bool {
        self.emulation.program_use_mouse()
    }

    fn set_use_mouse(&mut self, on: bool) {
        self.emulation.set_use_mouse(on)
    }

    fn program_bracketed_paste_mode(&self) -> bool {
        self.emulation.program_bracketed_paste_mode()
    }

    fn set_bracketed_paste_mode(&mut self, on: bool) {
        self.emulation.set_bracketed_paste_mode(on)
    }

    fn set_mode(&mut self, mode: i32) {
        self.emulation.set_mode(mode)
    }

    fn reset_mode(&mut self, mode: i32) {
        self.emulation.reset_mode(mode)
    }

    fn receive_char(&mut self, ch: wchar_t) {
        self.emulation.receive_char(ch)
    }

    fn set_screen(&mut self, index: i32) {
        self.emulation.set_screen(index)
    }

    ////////////////////////////////////////////////// Slots //////////////////////////////////////////////////
    fn set_image_size(&self, lines: i32, columns: i32) {
        self.emulation.set_image_size(lines, columns)
    }

    fn send_text(&self, text: String) {
        self.emulation.send_text(text)
    }

    fn send_key_event(&self, event: KeyEvent, from_paste: bool) {
        self.emulation.send_key_event(event, from_paste)
    }

    fn send_mouse_event(&self, buttons: i32, column: i32, line: i32, event_type: u8) {
        self.emulation.send_mouse_event(buttons, column, line, event_type)
    }

    fn send_string(&self, string: String, length: i32) {
        self.emulation.send_string(string, length)
    }

    fn receive_data(&self, buffer: Vec<u8>, len: i32) {
        self.emulation.receive_data(buffer, len)
    }

    fn show_bulk(&self) {
        self.emulation.show_bulk()
    }

    fn buffer_update(&self) {
        self.emulation.buffer_update()
    }

    fn uses_mouse_changed(&self, uses_mouse: bool) {
        self.emulation.uses_mouse_changed(uses_mouse)
    }

    fn bracketed_paste_mode_changed(&self, bracketed_paste_mode: bool) {
        self.emulation.bracketed_paste_mode_changed(bracketed_paste_mode)
    }
}
