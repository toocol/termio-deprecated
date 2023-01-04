#![allow(unused_macros)]
use super::{BaseEmulation, Emulation};
use crate::{
    core::{screen::MODES_SCREEN, screen_window::ScreenWindow},
    emulation::EmulationState,
    tools::{
        history::HistoryType, terminal_character_decoder::TerminalCharacterDecoder,
        translators::KeyboardTranslatorManager,
    },
};
use std::{
    cell::{Cell, RefCell},
    collections::HashMap,
    rc::Rc,
};
use tmui::{
    graphics::figure::Size,
    prelude::*,
    tlib::{emit, events::KeyEvent},
};
use wchar::{wch, wchar_t};
use widestring::U16String;

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
/// (Doc comment on struct [`VT102Emulation`])
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
/// Definitions: <br>
/// - C          - A single (required) character.
/// - Pn         - Numeric parameter
/// - Pt         - A text parameter composed of printable characters.
/// - CSI        - `Esc [` (Control Sequence Introducer, 0x9b)
///
/// The tokens are defined below:
///
/// - CHR        - Printable characters     (32..255 but DEL (=127)) <br>
/// - CTL        - Control characters       (0..31 but ESC (= 27), DEL)
/// - ESC        - Escape codes of the form <ESC><CHR but '[]()+*#'>
/// - ESC_DE     - Escape codes of the form <ESC><any of '()+*#%'> C
/// - CSI_PN     - Escape codes of the form <ESC>'['     {Pn} ';' {Pn} C
/// - CSI_PS     - Escape codes of the form <ESC>'['     {Pn} ';' ...  C
/// - CSI_PS_SP  - Escape codes of the form <ESC>'['     {Pn} ';' ... {Space} C
/// - CSI_PR     - Escape codes of the form <ESC>'[' '?' {Pn} ';' ...  C
/// - CSI_PE     - Escape codes of the form <ESC>'[' '!' {Pn} ';' ...  C
/// - VT52       - VT52 escape codes
///              - <ESC><Chr>
///              - <ESC>'Y'{Pc}{Pc}
/// - XTE_HA     - Xterm window/terminal attribute commands
///                of the form <ESC>']' {Pn} ';' {Pt} <BEL>
///                (Note that these are handled differently to the other formats
///                see also https://invisible-island.net/xterm/ctlseqs/ctlseqs.html#h3-Operating-System-Commands)
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
    charset: [u8; 4],
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

//////////////////////// Character Class flags used while decoding
/// Control character
const CTL: i32 = 1;
/// Printable character
const CHR: i32 = 2;
/// The character used in control sequnce
const CPN: i32 = 4;
/// Numberic digit
const DIG: i32 = 8;
/// Special characters in "()+*%"
const SCS: i32 = 16;
/// Special characters in "()+*#[]%"
const GRP: i32 = 32;
/// Character('t') which indicates end of window resize (escape sequence '\e[8;<row>;<col>t')
const CPS: i32 = 64;

impl VT102Emulation {
    //////////////////////////////////////////////////////// Private function
    fn init_tokenizer(&self) {
        for i in 0..256 {
            self.char_class.borrow_mut()[i] = 0;
        }
        for i in 0..32 {
            self.char_class.borrow_mut()[i] |= CTL;
        }
        for i in 32..256 {
            self.char_class.borrow_mut()[i] |= CHR;
        }
        for s in "@ABCDEFGHILMPSTXZbcdfry".as_bytes().iter() {
            self.char_class.borrow_mut()[*s as usize] |= CPN;
        }
        for s in "0123456789".as_bytes().iter() {
            self.char_class.borrow_mut()[*s as usize] |= DIG;
        }
        for s in "()+*%".as_bytes().iter() {
            self.char_class.borrow_mut()[*s as usize] |= SCS;
        }
        for s in "()+*#[]%".as_bytes().iter() {
            self.char_class.borrow_mut()[*s as usize] |= GRP;
        }
        for s in "t".as_bytes().iter() {
            self.char_class.borrow_mut()[*s as usize] |= CPS;
        }

        self.reset_tokenizer();
    }

    fn reset_tokenizer(&self) {
        self.token_buffer_pos.set(0);
        self.argc.set(0);
        self.argv.borrow_mut()[0] = 0;
        self.argv.borrow_mut()[1] = 0;
        self.prev_cc.set(0);
    }

    fn process_token(&self, code: i32, p: wchar_t, q: i32) {
        todo!()
    }

    fn process_window_attribute_change(&self) {
        // Describes the window or terminal session attribute to change
        // See [`Session::user_title_change`] for possible values
        let mut attribute_to_change = 0;
        let mut i = 2;
        loop {
            if !(i < self.token_buffer_pos.get()
                && self.token_buffer.borrow()[i] >= wch!('0')
                && self.token_buffer.borrow()[i] <= wch!('9'))
            {
                break;
            }

            attribute_to_change = 10 * attribute_to_change
                + (self.token_buffer.borrow()[i] as i32 - wch!('0') as i32);
            i += 1;
        }

        if self.token_buffer.borrow()[i] != wch!(';') {
            self.report_decoding_error();
            return;
        }

        // copy from the first char after ';', and skipping the ending delimiter
        // 0x07 or 0x92. Note that as control characters in OSC text parts are
        // ignored, only the second char in ST ("\e\\") is appended to tokenBuffer.
        let mut new_value = U16String::new();
        new_value.push_slice(
            &self.token_buffer.borrow()
                [i as usize + 1..(self.token_buffer_pos.get() - i - 2) as usize],
        );

        self.pending_title_updates.borrow_mut().insert(
            attribute_to_change,
            new_value
                .to_string()
                .expect("`U16String` transmit to String failed."),
        );
        // TODO: Update title update timer
    }

    fn request_window_attribute(&self, p: i32) {
        todo!()
    }

    fn add_to_current_token(&self, cc: wchar_t) {
        todo!()
    }

    fn add_digit(&self, digit: i32) {
        if self.argv.borrow()[self.argc.get() as usize] < MAX_ARGUMENT as i32 {
            self.argv.borrow_mut()[self.argc.get() as usize] =
                10 * self.argv.borrow()[self.argc.get() as usize] + digit;
        }
    }

    fn add_argument(&self) {
        self.argc.set((self.argc.get() + 1).min(MAXARGS as i32 - 1));
        self.argv.borrow_mut()[self.argc.get() as usize] = 0;
    }

    //////////////////////////////////////// VT100 Charset ////////////////////////////////////////
    /*
       The processing contains a VT100 specific code translation layer.
       It's still in use and mainly responsible for the line drawing graphics.

       These and some other glyphs are assigned to codes (0x5f-0xfe)
       normally occupied by the latin letters. Since this codes also
       appear within control sequences, the extra code conversion
       does not permute with the tokenizer and is placed behind it
       in the pipeline. It only applies to tokens, which represent
       plain characters.

       This conversion it eventually continued in TerminalDisplay.C, since
       it might involve VT100 enhanced fonts, which have these
       particular glyphs allocated in (0x00-0x1f) in their code page.
    */
    fn charset(&self) -> Option<&CharCodes> {
        let codes = self.charset.borrow().get(if self
            .emulation
            .as_ref()
            .unwrap()
            .current_screen
            .borrow()
            .borrow()
            .id()
            == self.emulation.as_ref().unwrap().screen.borrow()[1].borrow().id()
        {
            1
        } else {
            0
        });
        todo!()
    }

    fn apply_charset(&self, c: wchar_t) -> wchar_t {
        todo!()
    }

    fn set_charset(&self, n: i32, cs: i32) {
        self.charset.borrow_mut()[0].charset[(n & 3) as usize] = cs as u8;
        self.use_charset(self.charset.borrow()[0].current_charset);
        self.charset.borrow_mut()[1].charset[(n & 3) as usize] = cs as u8;
        self.use_charset(self.charset.borrow()[1].current_charset);
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

    //////////////////////////////////////// Modes operations ////////////////////////////////////////
    /*
       Some of the emulations state is either added to the state of the screens.

       This causes some scoping problems, since different emulations choose to
       located the mode either to the current _screen or to both.

       For strange reasons, the extend of the rendition attributes ranges over
       all screens and not over the actual _screen.

       We decided on the precise precise extend, somehow.
    */
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

    fn report_decoding_error(&self) {
        if self.token_buffer_pos.get() == 0
            || self.token_buffer_pos.get() == 1 && self.token_buffer.borrow()[0] & 0xff >= 32
        {
            return;
        }
    }

    fn report_terminal_type(&self) {
        // Primary device attribute response (Request was: ^[[0c or ^[[c (from TT321
        // Users Guide)) VT220:  ^[[?63;1;2;3;6;7;8c   (list deps on emul.
        // capabilities) VT100:  ^[[?1;2c VT101:  ^[[?1;0c VT102:  ^[[?6v
        if self.get_mode(MODE_ANSI as i32) {
            // VT100
            self.send_string("\u{001b}[?1;2c".to_string(), -1)
        } else {
            // VT52
            self.send_string("\u{001b}/Z".to_string(), -1)
        }
    }

    fn report_secondary_attributes(&self) {
        // Seconday device attribute response (Request was: ^[[>0c or ^[[>c)
        if self.get_mode(MODE_ANSI as i32) {
            self.send_string("\u{001b}[>0;115;0c".to_string(), -1)
        } else {
            self.send_string("\u{001b}/Z".to_string(), -1)
        }
    }

    fn report_status(&self) {
        self.send_string("\u{001b}[0n".to_string(), -1)
    }

    fn report_answer_back(&self) {
        // This is really obsolete VT100 stuff.
        self.send_string("".to_string(), -1)
    }

    fn report_cursor_position(&self) {
        let borrowed = self.emulation.as_ref().unwrap().current_screen.borrow();
        let current_screen = borrowed.borrow();

        let str = format!(
            "\u{001b}[{};{}R",
            current_screen.get_cursor_y() + 1,
            current_screen.get_cursor_x() + 1
        );
        self.send_string(str, -1)
    }

    fn report_terminal_params(&self, p: i32) {
        let str = format!("\u{001b}[{};1;1;112;112;1;0;x", p);
        self.send_string(str, -1)
    }

    fn on_scroll_lock(&self) {
        todo!()
    }

    fn scroll_lock(&self, lock: bool) {
        todo!()
    }

    fn clear_screen_and_set_columns(&self, column_count: i32) {
        self.set_image_size(
            self.emulation
                .as_ref()
                .unwrap()
                .current_screen
                .borrow()
                .borrow()
                .get_lines(),
            column_count,
        );
        self.clear_entire_screen();
        self.set_default_margins();
        self.emulation
            .as_ref()
            .unwrap()
            .current_screen
            .borrow()
            .borrow_mut()
            .set_cursor_yx(0, 0);
    }

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
        for arg in self.pending_title_updates.borrow().keys() {
            let borrowed = self.pending_title_updates.borrow();
            let title = match borrowed.get(arg) {
                Some(val) => val,
                None => "",
            };
            emit!(self.title_changed(), (*arg, title));
        }
        self.pending_title_updates.borrow_mut().clear();
    }
}

/**
   Ok, here comes the nasty part of the decoder.

   Instead of keeping an explicit state, we deduce it from the
   token scanned so far. It is then immediately combined with
   the current character to form a scanning decision.

   This is done by the following defines.

   - P is the length of the token scanned so far.
   - L (often P-1) is the position on which contents we base a decision.
   - C is a character or a group of characters (taken from 'charClass').

   - 'cc' is the current character
   - 's' is a pointer to the start of the token buffer
   - 'p' is the current position within the token buffer

   Note that they need to applied in proper order
   and thoes macros used in [`VT102Emulation::receive_char(&self, cc: wchar_t)`]]
*/
macro_rules! lec {
    ( $P:expr, $L:expr, $C:expr ) => {
        p == $P && s[$L as usize] == wch!($C)
    };
}
macro_rules! lun {
    () => {
        p == 1 && cc >= 32
    };
}
macro_rules! les {
    ( $P:expr, $L:expr, $C:expr ) => {
        (p == $P && s[$L as usize] < 256)
            && (char_class[s[$L as usize] as usize] & (wch!($C) as i32) == wch!($C) as i32)
    };
}
macro_rules! eec {
    ( $C:expr ) => {
        p >= 3 && cc = wch!($C)
    };
}
macro_rules! ees {
    ( $C:expr ) => {
        p >= 3 && cc < 256 && (char_class[cc as usize] & (wch!($C) as i32) == wch!($C) as i32)
    };
}
macro_rules! eps {
    ( $C:expr ) => {
        p >= 3
            && s[2] != wch!('?')
            && s[2] != wch('!')
            && s[2] != wch!('>')
            && cc < 256
            && (char_class[cc as usize] & (wch!($C) as i32) == wch!($C) as i32)
    };
}
macro_rules! epp {
    () => {
        p >= 3 && s[2] == wch!('?')
    };
}
macro_rules! epe {
    () => {
        p >= 3 && s[2] == wch('!')
    };
}
macro_rules! egt {
    () => {
        p >= 3 && s[2] == wch('>')
    };
}
macro_rules! esp {
    () => {
        p >= 4 && s[3] == wch(' ')
    };
}
macro_rules! xpe {
    () => {
        token_buffer_pos >= 2 && token_buffer[1] = wch!(']')
    };
}
macro_rules! xte {
    () => {
        xpe!() && (cc == 7 || (prev_cc == 27 && cc = 92))
    };
}
macro_rules! ces {
    ( $C:expr ) => {
        cc < 256 && (char_class[cc as usize] & (wch!($C) as i32) == wch!($C) as i32 && !xte())
    };
}
macro_rules! cntl {
    ( $c:expr ) => {
        wch!(c) - wch!('@')
    };
}
const ESC: u16 = 27;
const DEL: u16 = 127;
impl Emulation for VT102Emulation {
    type Type = VT102Emulation;

    fn new(translator_manager: Rc<RefCell<KeyboardTranslatorManager>>) -> Rc<Self::Type> {
        let base_emulation = BaseEmulation::new(translator_manager);
        let mut vt102_emulation: VT102Emulation = Default::default();
        vt102_emulation.emulation = Some(base_emulation);
        Rc::new(vt102_emulation)
    }

    fn receive_char(&self, cc: wchar_t) {
        self.emulation.as_ref().unwrap().receive_char(cc)
    }

    fn receive_data(&self, buffer: Vec<u8>, len: i32) {
        emit!(self.state_set(), EmulationState::NotifyActivity as u8);

        self.buffer_update();

        let utf8_text = String::from_utf8(buffer.clone())
            .expect("`Emulation` receive_data() parse utf-8 string failed.");
        let utf16_text = U16String::from_str(&utf8_text);

        // Send characters to terminal emulator
        let text_slice = utf16_text.as_slice();
        for i in 0..text_slice.len() {
            self.receive_char(text_slice[i]);
        }

        // Look for z-modem indicator
        for i in 0..len as usize {
            if buffer[i] == '\u{0030}' as u8 {
                if len as usize - i - 1 > 3
                    && String::from_utf8(buffer[i + 1..i + 4].to_vec()).unwrap() == "B00"
                {
                    emit!(self.zmodem_detected())
                }
            }
        }
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
        if length >= 0 {
            emit!(self.send_data(), (string, length));
        } else {
            let len = string.len() as i32;
            emit!(self.send_data(), (string, len));
        }
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
