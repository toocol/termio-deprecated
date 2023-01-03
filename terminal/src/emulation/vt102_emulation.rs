#![allow(unused_macros)]
use super::{BaseEmulation, Emulation};
use crate::{
    core::{screen::MODES_SCREEN, screen_window::ScreenWindow},
    tools::{
        history::HistoryType, terminal_character_decoder::TerminalCharacterDecoder,
        translators::KeyboardTranslatorManager,
    },
};
use std::{
    cell::{Cell, Ref, RefCell},
    rc::Rc, collections::HashMap,
};
use tmui::{graphics::figure::Size, prelude::*, tlib::events::KeyEvent};
use wchar::wchar_t;

/// Processing the incoming byte stream.
/// --------------------------------------------------------------------
/// Incoming Bytes Event pipeline
///
/// This section deals with decoding the incoming character stream.
/// Decoding means here, that the stream is first separated into `tokens'
//// which are then mapped to a `meaning' provided as operations by the`Screen' class or by the emulation class itself.
///
/// The pipeline proceeds as follows:
///
/// - Tokenizing the ESC codes (onReceiveChar). <br>
/// - VT100 code page translation of plain characters (applyCharset). <br>
/// - Interpretation of ESC codes (processToken). <br>
///
/// The escape codes and their meaning are described in the technical reference of this program.
///
/// Tokens:
/// --------------------------------------------------------------------
///
/// Since the tokens are the central notion if this section, we've put them in front.
/// They provide the syntactical elements used to represent the terminals operations as byte sequences.
///
/// They are encodes here into a single machine word, so that we can later switch over them easily.
/// Depending on the token itself, additional argument variables are filled with parameter values.
///
/// The tokens are defined below:
///
/// - CHR        - Printable characters     (32..255 but DEL (=127)) <br>
/// - CTL        - Control characters       (0..31 but ESC (= 27), DEL)
/// - ESC        - Escape codes of the form <ESC><CHR but `[]()+*#'>
/// - ESC_DE     - Escape codes of the form <ESC><any of `()+*#%'> C
/// - CSI_PN     - Escape codes of the form <ESC>'['     {Pn} ';' {Pn} C
/// - CSI_PS     - Escape codes of the form <ESC>'['     {Pn} ';' ...  C
/// - CSI_PS_SP  - Escape codes of the form <ESC>'['     {Pn} ';' ... {Space} C
/// - CSI_PR     - Escape codes of the form <ESC>'[' '?' {Pn} ';' ...  C
/// - CSI_PE     - Escape codes of the form <ESC>'[' '!' {Pn} ';' ...  C
/// - VT52       - VT52 escape codes
///              - <ESC><Chr>
///              - <ESC>'Y'{Pc}{Pc}
/// - XTE_HA     - Xterm window/terminal attribute commands
///                of the form <ESC>`]' {Pn} `;' {Text} <BEL>
///                (Note that these are handled differently to the other formats)
///
/// The last two forms allow list of arguments.
/// Since the elements of the lists are treated individually the same way,
/// they are passed as individual tokens to the interpretation. Further,
/// because the meaning of the parameters are names (althought represented as numbers),
/// they are includes within the token ('N').<br>
macro_rules! ty_construct {
    ( $t:expr, $a:expr, $n:expr ) => {
        (((($t as i32) & 0xffff) << 16) | ((($a as i32) & 0xff) << 8) | (($n as i32) & 0xff))
    };
}
macro_rules! ty_chr {
    () => {
        ty_construct!(0, 0, 0)
    };
}
macro_rules! ty_ctl {
    ( $a:expr ) => {
        ty_construct!(1, $a, 0)
    };
}
macro_rules! ty_esc {
    ( $a:expr ) => {
        ty_construct!(2, $a, 0)
    };
}
macro_rules! ty_esc_cs {
    ( $a:expr, $b:expr ) => {
        ty_construct!(3, $a, $b)
    };
}
macro_rules! ty_esc_de {
    ( $a:expr ) => {
        ty_construct!(4, $a, 0)
    };
}
macro_rules! ty_csi_ps {
    ( $a:expr, $n:expr ) => {
        ty_construct!(5, $a, $n)
    };
}
macro_rules! ty_csi_pn {
    ( $a:expr ) => {
        ty_construct!(6, $a, 0)
    };
}
macro_rules! ty_csi_pr {
    ( $a:expr, $n:expr ) => {
        ty_construct!(7, $a, $n)
    };
}
macro_rules! ty_csi_ps_sp {
    ( $a:expr, $n:expr ) => {
        ty_construct!(11, $a, $n)
    };
}

macro_rules! ty_vt52 {
    ( $a:expr ) => {
        ty_construct!(8, $a, 0)
    };
}
macro_rules! ty_csi_pg {
    ( $a:expr ) => {
        ty_construct!(9, $a, 0)
    };
}
macro_rules! ty_csi_pe {
    ( $a:expr ) => {
        ty_construct!(10, $a, 0)
    };
}

const MAX_ARGUMENT: usize = 4096;
const MAX_TOKEN_LENGTH: usize = 256;
const MAXARGS: usize = 15;

/// Mode #1.
const MODE_APP_SCREEN: usize = MODES_SCREEN + 0;
/// Application cursor key (DECCKM).
const MODE_APP_CURSOR_KEY: usize = MODES_SCREEN + 1;
/// Application key pad.
const MODE_APP_KEY_PAD: usize = MODES_SCREEN + 2;
/// Send mouse X,Y position on press and release
const MODE_MOUSE_1000: usize = MODES_SCREEN + 3;
/// Use hilight mouse tracking.
const MODE_MOUSE_1001: usize = MODES_SCREEN + 4;
/// Use cell motion mouse tracking.
const MODE_MOUSE_1002: usize = MODES_SCREEN + 5;
/// Use all motion mouse tracking.
const MODE_MOUSE_1003: usize = MODES_SCREEN + 6;
/// Xterm-style extended coordinates.
const MODE_MOUSE_1005: usize = MODES_SCREEN + 7;
/// 2nd Xterm-style extended coordinates.
const MODE_MOUSE_1006: usize = MODES_SCREEN + 8;
/// Urxvt-style extended coordinates.
const MODE_MOUSE_1015: usize = MODES_SCREEN + 9;
/// Use US Ascii for character sets G0-G3 (DECANM).
const MODE_ANSI: usize = MODES_SCREEN + 10;
/// 80 <-> 132 column mode switch (DECCOLM).
const MODE_132_COLUMNS: usize = MODES_SCREEN + 11;
/// Allow DECCOLM mode.
const MODE_ALLOW_132_COLUMNS: usize = MODES_SCREEN + 12;
/// Xterm-style bracketed paste mode.
const MODE_BRACKETD_PASTE: usize = MODES_SCREEN + 13;
/// The total size of modes.
const MODE_TOTAL: usize = MODES_SCREEN + 14;

#[derive(Default)]
struct CharCodes {
    /// Coding information.
    charset: [char; 4],
    /// Actual charset.
    current_charset: i32,
    /// Some VT100 tricks.
    graphic: bool,
    /// Some VT100 tricks.
    pound: bool,
    /// Saved graphic.
    saved_graphic: bool,
    /// Saved pound.
    saved_pound: bool,
}

#[derive(Default)]
struct TerminalState {
    mode: [bool; MODE_TOTAL],
}

/// The VT102 Emulation.
///--------------------------------------------------------------------------------------
/// Provides an `xterm` compatible terminal emulation based on the DEC `VT102`
/// terminal. A full description of this terminal can be found at
/// http://vt100.net/docs/vt102-ug/
///
/// In addition, various additional xterm escape sequences are supported to
/// provide features such as mouse input handling. See
/// http://rtfm.etla.org/xterm/ctlseq.html for a description of xterm's escape sequences.
pub struct VT102Emulation {
    emulation: Option<Rc<BaseEmulation>>,
    token_buffer: RefCell<[wchar_t; MAX_TOKEN_LENGTH]>,
    token_buffer_pos: Cell<usize>,
    argv: RefCell<[i32; MAXARGS]>,
    argc: Cell<i32>,
    prev_cc: Cell<i32>,
    /// Set of flags for each of the ASCII characters which indicates what category they fall into
    /// (printable character, control, digit etc.) for the purposes of decoding terminal output
    char_class: RefCell<[i32; 256]>,
    charset: RefCell<[CharCodes; 2]>,
    current_modes: TerminalState,
    saved_modes: TerminalState,
    pending_title_updates: RefCell<HashMap<i32, String>>,
    report_focus_event: bool,
    // TODO: Add timer: title_update_timer
}
impl ObjectOperation for VT102Emulation {
    fn id(&self) -> u64 {
        self.emulation.as_ref().unwrap().id()
    }

    fn set_property(&self, name: &str, value: Value) {
        self.emulation.as_ref().unwrap().set_property(name, value)
    }

    fn get_property(&self, name: &str) -> Option<std::cell::Ref<Box<Value>>> {
        self.emulation.as_ref().unwrap().get_property(name)
    }
}
impl Default for VT102Emulation {
    fn default() -> Self {
        Self {
            emulation: Default::default(),
            token_buffer: RefCell::new([0; MAX_TOKEN_LENGTH]),
            token_buffer_pos: Default::default(),
            argv: RefCell::new([0; MAXARGS]),
            argc: Default::default(),
            prev_cc: Default::default(),
            char_class: RefCell::new([0; 256]),
            charset: RefCell::new(Default::default()),
            current_modes: Default::default(),
            saved_modes: Default::default(),
            pending_title_updates: Default::default(),
            report_focus_event: Default::default(),
        }
    }
}
impl ActionExt for VT102Emulation {}

impl VT102Emulation {
    //////////////////////////////////////////////////////// Slots
    pub fn focus_lost(&self) {
        todo!()
    }

    pub fn focus_gained(&self) {
        todo!()
    }

    /// causes changeTitle() to be emitted for each (int,QString) pair in
    /// pendingTitleUpdates used to buffer multiple title updates
    fn update_title(&self) {
        todo!()
    }

    //////////////////////////////////////////////////////// Private function
    fn reset_tokenizer(&self) {
        todo!()
    }

    fn init_tokenizer(&self) {
        todo!()
    }

    fn add_to_current_token(&self, cc: wchar_t) {
        todo!()
    }

    fn apply_charset(&self, c: wchar_t) -> wchar_t {
        todo!()
    }

    fn add_digit(&self, digit: i32) {
        todo!()
    }

    fn add_argument(&self) {
        todo!()
    }

    fn set_charset(&self, n: i32, cs: i32) {
        todo!()
    }

    fn use_charset(&self, n: i32) {
        todo!()
    }

    fn set_and_use_charset(&self, n: i32, cs: i32) {
        todo!()
    }

    fn save_cursor(&self) {
        todo!()
    }

    fn restore_cursor(&self) {
        todo!()
    }

    fn reset_charset(&self, scrno: i32) {
        todo!()
    }

    fn set_margins(&self, top: i32, bottom: i32) {
        todo!()
    }

    /// Set margins for all screens back to their defaults.
    fn set_default_margins(&self) {
        todo!()
    }

    /// Returns true if 'mode' is set or false otherwise.
    fn get_mode(&self, mode: i32) -> bool {
        todo!()
    }

    /// Saves the current boolean value of 'mode'.
    fn save_mode(&self, mode: i32) {
        todo!()
    }

    /// Restores the boolean value of 'mode'.
    fn restore_mode(&self, mode: i32) {
        todo!()
    }

    /// Resets all modes (except MODE_Allow132Columns).
    fn reset_modes(&self) {
        todo!()
    }
}

impl Emulation for VT102Emulation {
    type Type = VT102Emulation;

    fn new(translator_manager: Rc<RefCell<KeyboardTranslatorManager>>) -> Rc<Self::Type> {
        let base_emulation = BaseEmulation::new(translator_manager);
        let mut vt102_emulation: VT102Emulation = Default::default();
        vt102_emulation.emulation = Some(base_emulation);
        Rc::new(vt102_emulation)
    }

    fn create_window(self: &Rc<Self>) -> Rc<RefCell<Box<ScreenWindow>>> {
        self.emulation.as_ref().unwrap().create_window()
    }

    fn image_size(&self) -> Size {
        self.emulation.as_ref().unwrap().image_size()
    }

    fn line_count(&self) -> i32 {
        self.emulation.as_ref().unwrap().line_count()
    }

    fn set_history(&self, history_type: Rc<dyn HistoryType>) {
        self.emulation.as_ref().unwrap().set_history(history_type)
    }

    fn history(&self) -> Rc<dyn HistoryType> {
        self.emulation.as_ref().unwrap().history()
    }

    fn clear_history(&self) {
        self.emulation.as_ref().unwrap().clear_history()
    }

    fn write_to_stream(
        &self,
        decoder: &mut dyn TerminalCharacterDecoder,
        start_line: i32,
        end_line: i32,
    ) {
        self.emulation
            .as_ref()
            .unwrap()
            .write_to_stream(decoder, start_line, end_line)
    }

    fn erase_char(&self) -> char {
        self.emulation.as_ref().unwrap().erase_char()
    }

    fn set_keyboard_layout(&self, name: &str) {
        self.emulation.as_ref().unwrap().set_keyboard_layout(name)
    }

    fn keyboard_layout(&self) -> String {
        self.emulation.as_ref().unwrap().keyboard_layout()
    }

    fn clear_entire_screen(&self) {
        self.emulation.as_ref().unwrap().clear_entire_screen()
    }

    fn reset(&self) {
        self.emulation.as_ref().unwrap().reset()
    }

    fn program_use_mouse(&self) -> bool {
        self.emulation.as_ref().unwrap().program_use_mouse()
    }

    fn set_use_mouse(&self, on: bool) {
        self.emulation.as_ref().unwrap().set_use_mouse(on)
    }

    fn program_bracketed_paste_mode(&self) -> bool {
        self.emulation
            .as_ref()
            .unwrap()
            .program_bracketed_paste_mode()
    }

    fn set_bracketed_paste_mode(&self, on: bool) {
        self.emulation
            .as_ref()
            .unwrap()
            .set_bracketed_paste_mode(on)
    }

    fn set_mode(&self, mode: i32) {
        self.emulation.as_ref().unwrap().set_mode(mode)
    }

    fn reset_mode(&self, mode: i32) {
        self.emulation.as_ref().unwrap().reset_mode(mode)
    }

    fn receive_char(&self, ch: wchar_t) {
        self.emulation.as_ref().unwrap().receive_char(ch)
    }

    fn set_screen(&self, index: i32) {
        self.emulation.as_ref().unwrap().set_screen(index)
    }

    ////////////////////////////////////////////////// Slots //////////////////////////////////////////////////
    fn set_image_size(&self, lines: i32, columns: i32) {
        self.emulation
            .as_ref()
            .unwrap()
            .set_image_size(lines, columns)
    }

    fn send_text(&self, text: String) {
        self.emulation.as_ref().unwrap().send_text(text)
    }

    fn send_key_event(&self, event: KeyEvent, from_paste: bool) {
        self.emulation
            .as_ref()
            .unwrap()
            .send_key_event(event, from_paste)
    }

    fn send_mouse_event(&self, buttons: i32, column: i32, line: i32, event_type: u8) {
        self.emulation
            .as_ref()
            .unwrap()
            .send_mouse_event(buttons, column, line, event_type)
    }

    fn send_string(&self, string: String, length: i32) {
        self.emulation.as_ref().unwrap().send_string(string, length)
    }

    fn receive_data(&self, buffer: Vec<u8>, len: i32) {
        self.emulation.as_ref().unwrap().receive_data(buffer, len)
    }

    fn show_bulk(&self) {
        self.emulation.as_ref().unwrap().show_bulk()
    }

    fn buffer_update(&self) {
        self.emulation.as_ref().unwrap().buffer_update()
    }

    fn uses_mouse_changed(&self, uses_mouse: bool) {
        self.emulation
            .as_ref()
            .unwrap()
            .uses_mouse_changed(uses_mouse)
    }

    fn bracketed_paste_mode_changed(&self, bracketed_paste_mode: bool) {
        self.emulation
            .as_ref()
            .unwrap()
            .bracketed_paste_mode_changed(bracketed_paste_mode)
    }
}
