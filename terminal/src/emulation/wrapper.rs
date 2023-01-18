use crate::{
    core::screen_window::ScreenWindow,
    tools::{history::HistoryType, terminal_character_decoder::TerminalCharacterDecoder},
};
use std::{ptr::NonNull, rc::Rc};
use tmui::{graphics::figure::Size, prelude::*, tlib::events::KeyEvent};
use wchar::wchar_t;

use super::Emulation;

/// The wrapper trait used in dyn object, represents the abstraction of `Emulation`.
pub trait EmulationWrapper {
    fn initialize(&mut self);

    fn create_window(&mut self) -> Option<NonNull<ScreenWindow>>;

    fn image_size(&self) -> Size;

    fn line_count(&self) -> i32;

    fn set_history(&mut self, history_type: Rc<dyn HistoryType>);

    fn history(&self) -> Rc<dyn HistoryType>;

    fn clear_history(&mut self);

    fn write_to_stream(
        &mut self,
        decoder: &mut dyn TerminalCharacterDecoder,
        start_line: i32,
        end_line: i32,
    );

    fn erase_char(&self) -> char;

    fn set_keyboard_layout(&mut self, name: &str);

    fn keyboard_layout(&self) -> String;

    fn clear_entire_screen(&mut self);

    fn reset(&self);

    fn program_use_mouse(&self) -> bool;
    fn set_use_mouse(&mut self, on: bool);

    fn program_bracketed_paste_mode(&self) -> bool;
    fn set_bracketed_paste_mode(&mut self, on: bool);

    fn set_mode(&mut self, mode: usize);
    fn reset_mode(&mut self, mode: usize);

    fn receive_char(&mut self, ch: wchar_t);

    fn set_screen(&mut self, index: i32);

    fn set_image_size(&mut self, lines: i32, columns: i32);

    fn send_text(&self, text: String);

    fn send_key_event(&self, event: KeyEvent, from_paste: bool);

    fn send_mouse_event(&self, buttons: i32, column: i32, line: i32, event_type: u8);

    fn send_string(&self, string: String, length: i32);

    fn receive_data(&mut self, buffer: Vec<u8>, len: i32);

    fn show_bulk(&mut self);

    fn buffered_update(&mut self);

    fn uses_mouse_changed(&mut self, uses_mouse: bool);

    fn bracketed_paste_mode_changed(&mut self, bracketed_paste_mode: bool);

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

impl<T: Emulation + ActionExt> EmulationWrapper for Option<T> {
    fn initialize(&mut self) {
        self.as_mut().unwrap().initialize()
    }

    fn create_window(&mut self) -> Option<NonNull<ScreenWindow>> {
        self.as_mut().unwrap().create_window()
    }

    fn image_size(&self) -> Size {
        self.as_ref().unwrap().image_size()
    }

    fn line_count(&self) -> i32 {
        self.as_ref().unwrap().line_count()
    }

    fn set_history(&mut self, history_type: Rc<dyn HistoryType>) {
        self.as_mut().unwrap().set_history(history_type)
    }

    fn history(&self) -> Rc<dyn HistoryType> {
        self.as_ref().unwrap().history()
    }

    fn clear_history(&mut self) {
        self.as_mut().unwrap().clear_history()
    }

    fn write_to_stream(
        &mut self,
        decoder: &mut dyn TerminalCharacterDecoder,
        start_line: i32,
        end_line: i32,
    ) {
        self.as_mut()
            .unwrap()
            .write_to_stream(decoder, start_line, end_line)
    }

    fn erase_char(&self) -> char {
        self.as_ref().unwrap().erase_char()
    }

    fn set_keyboard_layout(&mut self, name: &str) {
        self.as_mut().unwrap().set_keyboard_layout(name)
    }

    fn keyboard_layout(&self) -> String {
        self.as_ref().unwrap().keyboard_layout()
    }

    fn clear_entire_screen(&mut self) {
        self.as_mut().unwrap().clear_entire_screen()
    }

    fn reset(&self) {
        self.as_ref().unwrap().reset()
    }

    fn program_use_mouse(&self) -> bool {
        self.as_ref().unwrap().program_use_mouse()
    }

    fn set_use_mouse(&mut self, on: bool) {
        self.as_mut().unwrap().set_use_mouse(on)
    }

    fn program_bracketed_paste_mode(&self) -> bool {
        self.as_ref().unwrap().program_bracketed_paste_mode()
    }

    fn set_bracketed_paste_mode(&mut self, on: bool) {
        self.as_mut().unwrap().set_bracketed_paste_mode(on)
    }

    fn set_mode(&mut self, mode: usize) {
        self.as_mut().unwrap().set_mode(mode)
    }

    fn reset_mode(&mut self, mode: usize) {
        self.as_mut().unwrap().reset_mode(mode)
    }

    fn receive_char(&mut self, ch: wchar_t) {
        self.as_mut().unwrap().receive_char(ch)
    }

    fn set_screen(&mut self, index: i32) {
        self.as_mut().unwrap().set_screen(index)
    }

    fn set_image_size(&mut self, lines: i32, columns: i32) {
        self.as_mut().unwrap().set_image_size(lines, columns)
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

    fn receive_data(&mut self, buffer: Vec<u8>, len: i32) {
        self.as_mut().unwrap().receive_data(buffer, len)
    }

    fn show_bulk(&mut self) {
        self.as_mut().unwrap().show_bulk()
    }

    fn buffered_update(&mut self) {
        self.as_mut().unwrap().buffered_update()
    }

    fn uses_mouse_changed(&mut self, uses_mouse: bool) {
        self.as_mut().unwrap().uses_mouse_changed(uses_mouse)
    }

    fn bracketed_paste_mode_changed(&mut self, bracketed_paste_mode: bool) {
        self.as_mut()
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
