use crate::{
    core::screen_window::ScreenWindow,
    tools::{
        history::HistoryType,
        terminal_character_decoder::TerminalCharacterDecoder,
    },
};
use std::{
    cell::{RefCell},
    rc::Rc,
};
use tmui::{
    graphics::figure::Size,
    prelude::*,
    tlib::{
        events::KeyEvent,
    },
};
use wchar::wchar_t;

use super::Emulation;

/// The wrapper trait used in dyn object, represents the abstraction of `Emulation`.
pub trait EmulationWrapper {
    fn create_window(&self) -> Rc<RefCell<Box<ScreenWindow>>>;

    fn image_size(&self) -> Size;

    fn line_count(&self) -> i32;

    fn set_history(&self, history_type: Rc<dyn HistoryType>);

    fn history(&self) -> Rc<dyn HistoryType>;

    fn clear_history(&self);

    fn write_to_stream(
        &self,
        decoder: &mut dyn TerminalCharacterDecoder,
        start_line: i32,
        end_line: i32,
    );

    fn erase_char(&self) -> char;

    fn set_keyboard_layout(&self, name: &str);

    fn keyboard_layout(&self) -> String;

    fn clear_entire_screen(&self);

    fn reset(&self);

    fn program_use_mouse(&self) -> bool;
    fn set_use_mouse(&self, on: bool);

    fn program_bracketed_paste_mode(&self) -> bool;
    fn set_bracketed_paste_mode(&self, on: bool);

    fn set_mode(&self, mode: i32);
    fn reset_mode(&self, mode: i32);

    fn receive_char(&self, ch: wchar_t);

    fn set_screen(&self, index: i32);

    fn set_image_size(&self, lines: i32, columns: i32);

    fn send_text(&self, text: String);

    fn send_key_event(&self, event: KeyEvent, from_paste: bool);

    fn send_mouse_event(&self, buttons: i32, column: i32, line: i32, event_type: u8);

    fn send_string(&self, string: String, length: i32);

    fn receive_data(&self, buffer: Vec<u8>, len: i32);

    fn show_bulk(&self);

    fn buffer_update(&self);

    fn uses_mouse_changed(&self, uses_mouse: bool);

    fn bracketed_paste_mode_changed(&self, bracketed_paste_mode: bool);

    fn send_data(&self) -> Signal;

    fn lock_pty_request(&self) -> Signal;

    fn use_utf8_request(&self) -> Signal;

    fn state_set(&self) -> Signal;

    fn zmodem_detected(&self) -> Signal;

    fn change_tab_text_color_request(&self) -> Signal;

    fn program_uses_mouse_changed(&self) -> Signal;

    fn program_bracketed_paste_mode_changed(&self) -> Signal;

    fn output_changed(&self) -> Signal;

    fn title_changed(&self) -> Signal;

    fn image_size_changed(&self) -> Signal;

    fn image_size_initialized(&self) -> Signal;

    fn image_resize_request(&self) -> Signal;

    fn profile_change_command_received(&self) -> Signal;

    fn flow_control_key_pressed(&self) -> Signal;

    fn cursor_changed(&self) -> Signal;

    fn handle_command_from_keyboard(&self) -> Signal;

    fn output_from_keypress_event(&self) -> Signal;
}

impl<T: Emulation + ActionExt> EmulationWrapper for Option<Rc<T>> {
    fn create_window(&self) -> Rc<RefCell<Box<ScreenWindow>>> {
        self.as_ref().unwrap().create_window()
    }

    fn image_size(&self) -> Size {
        self.as_ref().unwrap().image_size()
    }

    fn line_count(&self) -> i32 {
        self.as_ref().unwrap().line_count()
    }

    fn set_history(&self, history_type: Rc<dyn HistoryType>) {
        self.as_ref().unwrap().set_history(history_type)
    }

    fn history(&self) -> Rc<dyn HistoryType> {
        self.as_ref().unwrap().history()
    }

    fn clear_history(&self) {
        self.as_ref().unwrap().clear_history()
    }

    fn write_to_stream(
        &self,
        decoder: &mut dyn TerminalCharacterDecoder,
        start_line: i32,
        end_line: i32,
    ) {
        self.as_ref()
            .unwrap()
            .write_to_stream(decoder, start_line, end_line)
    }

    fn erase_char(&self) -> char {
        self.as_ref().unwrap().erase_char()
    }

    fn set_keyboard_layout(&self, name: &str) {
        self.as_ref().unwrap().set_keyboard_layout(name)
    }

    fn keyboard_layout(&self) -> String {
        self.as_ref().unwrap().keyboard_layout()
    }

    fn clear_entire_screen(&self) {
        self.as_ref().unwrap().clear_entire_screen()
    }

    fn reset(&self) {
        self.as_ref().unwrap().reset()
    }

    fn program_use_mouse(&self) -> bool {
        self.as_ref().unwrap().program_use_mouse()
    }

    fn set_use_mouse(&self, on: bool) {
        self.as_ref().unwrap().set_use_mouse(on)
    }

    fn program_bracketed_paste_mode(&self) -> bool {
        self.as_ref().unwrap().program_bracketed_paste_mode()
    }

    fn set_bracketed_paste_mode(&self, on: bool) {
        self.as_ref().unwrap().set_bracketed_paste_mode(on)
    }

    fn set_mode(&self, mode: i32) {
        self.as_ref().unwrap().set_mode(mode)
    }

    fn reset_mode(&self, mode: i32) {
        self.as_ref().unwrap().reset_mode(mode)
    }

    fn receive_char(&self, ch: wchar_t) {
        self.as_ref().unwrap().receive_char(ch)
    }

    fn set_screen(&self, index: i32) {
        self.as_ref().unwrap().set_screen(index)
    }

    fn set_image_size(&self, lines: i32, columns: i32) {
        self.as_ref().unwrap().set_image_size(lines, columns)
    }

    fn send_text(&self, text: String) {
        self.as_ref().unwrap().send_text(text)
    }

    fn send_key_event(&self, event: KeyEvent, from_paste: bool) {
        self.as_ref().unwrap().send_key_event(event, from_paste)
    }

    fn send_mouse_event(&self, buttons: i32, column: i32, line: i32, event_type: u8) {
        self.as_ref()
            .unwrap()
            .send_mouse_event(buttons, column, line, event_type)
    }

    fn send_string(&self, string: String, length: i32) {
        self.as_ref().unwrap().send_string(string, length)
    }

    fn receive_data(&self, buffer: Vec<u8>, len: i32) {
        self.as_ref().unwrap().receive_data(buffer, len)
    }

    fn show_bulk(&self) {
        self.as_ref().unwrap().show_bulk()
    }

    fn buffer_update(&self) {
        self.as_ref().unwrap().buffer_update()
    }

    fn uses_mouse_changed(&self, uses_mouse: bool) {
        self.as_ref().unwrap().uses_mouse_changed(uses_mouse)
    }

    fn bracketed_paste_mode_changed(&self, bracketed_paste_mode: bool) {
        self.as_ref()
            .unwrap()
            .bracketed_paste_mode_changed(bracketed_paste_mode)
    }

    fn send_data(&self) -> Signal {
        self.as_ref().unwrap().send_data()
    }

    fn lock_pty_request(&self) -> Signal {
        self.as_ref().unwrap().lock_pty_request()
    }

    fn use_utf8_request(&self) -> Signal {
        self.as_ref().unwrap().use_utf8_request()
    }

    fn state_set(&self) -> Signal {
        self.as_ref().unwrap().state_set()
    }

    fn zmodem_detected(&self) -> Signal {
        self.as_ref().unwrap().zmodem_detected()
    }

    fn change_tab_text_color_request(&self) -> Signal {
        self.as_ref().unwrap().change_tab_text_color_request()
    }

    fn program_uses_mouse_changed(&self) -> Signal {
        self.as_ref().unwrap().program_uses_mouse_changed()
    }

    fn program_bracketed_paste_mode_changed(&self) -> Signal {
        self.as_ref()
            .unwrap()
            .program_bracketed_paste_mode_changed()
    }

    fn output_changed(&self) -> Signal {
        self.as_ref().unwrap().output_changed()
    }

    fn title_changed(&self) -> Signal {
        self.as_ref().unwrap().title_changed()
    }

    fn image_size_changed(&self) -> Signal {
        self.as_ref().unwrap().image_size_changed()
    }

    fn image_size_initialized(&self) -> Signal {
        self.as_ref().unwrap().image_size_initialized()
    }

    fn image_resize_request(&self) -> Signal {
        self.as_ref().unwrap().image_resize_request()
    }

    fn profile_change_command_received(&self) -> Signal {
        self.as_ref().unwrap().profile_change_command_received()
    }

    fn flow_control_key_pressed(&self) -> Signal {
        self.as_ref().unwrap().flow_control_key_pressed()
    }

    fn cursor_changed(&self) -> Signal {
        self.as_ref().unwrap().cursor_changed()
    }

    fn handle_command_from_keyboard(&self) -> Signal {
        self.as_ref().unwrap().handle_command_from_keyboard()
    }

    fn output_from_keypress_event(&self) -> Signal {
        self.as_ref().unwrap().output_from_keypress_event()
    }
}