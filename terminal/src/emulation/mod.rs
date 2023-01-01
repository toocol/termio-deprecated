#![allow(dead_code)]
pub mod vt102_emulation;
pub use vt102_emulation::*;

use crate::{
    core::{screen::Screen, screen_window::ScreenWindow},
    tools::{
        history::HistoryType, terminal_character_decoder::TerminalCharacterDecoder,
        translators::KeyboardTranslator,
    },
};
use std::{cell::RefCell, rc::Rc};
use tmui::{
    graphics::figure::Size,
    prelude::{ActionHubExt, KeyEvent},
};
use wchar::wchar_t;

#[repr(u8)]
#[derive(Debug, PartialEq, Eq, Clone, Copy)]
pub enum EmulationState {
    /// The emulation is currently receiving user input.
    NotifyNormal = 0,
    /// The terminal program has triggered a bell event to get the user's attention.
    NotifyBell,
    /// The emulation is currently receiving data from its terminal input.
    NotifyActivity,
    NotifySilence,
}

#[repr(u8)]
#[derive(Debug, PartialEq, Eq, Clone, Copy)]
pub enum EmulationCodec {
    LocalCodec = 0,
    Utf8Codec = 1,
}

pub struct EmulationStorage {
    /// The kayboard layout translator.
    pub key_translator: Option<Box<KeyboardTranslator>>,
    /// Current active screen.
    pub current_screen: Rc<RefCell<Box<Screen>>>,
    /// 0 = primary screen. <br>
    /// 1 = alternate screen (used by vi,emocs etc. scrollBar is not enable in this mode).
    pub screen: [Rc<RefCell<Box<Screen>>>; 2],

    windows: Vec<Rc<RefCell<Box<ScreenWindow>>>>,
    use_mouse: bool,
    bracket_paste_mode: bool,
}

impl EmulationStorage {
    pub fn new() -> Self {
        let screen_0 = Rc::new(RefCell::new(Box::new(Screen::new(40, 80))));
        let screen_1 = Rc::new(RefCell::new(Box::new(Screen::new(40, 80))));

        Self {
            key_translator: None,
            current_screen: screen_0.clone(),
            screen: [screen_0, screen_1],
            windows: vec![],
            use_mouse: false,
            bracket_paste_mode: false,
        }
    }
}

pub trait Emulation {
    const ACTION_SEND_DATA: &'static str = "action-send-data";
    const ACTION_LOCK_PTY_REQUEST: &'static str = "action-lock-pty-request";
    const ACTION_USE_UTF8_REQUEST: &'static str = "action-use-utf8-request";
    const ACTION_STATE_SET: &'static str = "action-state-set";
    const ACTION_ZMODEM_DETECTED: &'static str = "action-zmodem-detected";
    const ACTION_CHANGE_TAB_TEXT_COLOR_REQUEST: &'static str =
        "action-change-tab-text-color-request";
    const ACTION_PROGRAM_USE_MOUSE_CHANGED: &'static str = "action-program-use-mouse-changed";
    const ACTION_PROGRAM_BRACKETED_PASTE_MODE_CHANGED: &'static str =
        "action-program-bracketed-paste-mode-changed";
    const ACTION_OUTPUT_CHANGED: &'static str = "action-output-changed";
    const ACTION_TITLE_CHANGED: &'static str = "action-title-changed";
    const ACTION_IMAGE_SIZE_CHANGED: &'static str = "action-iamge-size-changed";
    const ACTION_IMAGE_SIZE_INITIALIZED: &'static str = "action-iamge-size-initialized";
    const ACTION_IMAGE_RESIZE_REQUEST: &'static str = "action-iamge-resize-request";
    const ACTION_PROFILE_CHANGE_COMMAND_RECEIVED: &'static str =
        "action-profile-change-command-received";
    const ACTION_FLOW_CONTROL_KEY_PRESSED: &'static str = "action-flow-control-key-pressed";
    const ACTION_CURSOR_CHANGED: &'static str = "action-cursor-changed";
    const ACTION_HANDLE_COMMAND_FROM_KEYBOARD: &'static str = "action-handle-command-from-keyboard";
    const ACTION_OUTPUT_FROM_KEY_PRESS_EVENT: &'static str = "action-output-from-key-press-event";

    /// Creates a new window onto the output from this emulation.  The contents of the window are then rendered by views
    /// which are set to use this window using the TerminalDisplay::setScreenWindow() method.
    fn create_window(&self) -> Rc<RefCell<Box<ScreenWindow>>>;

    /// Returns the size of the screen image which the emulation produces.
    fn image_size(&self) -> Size;

    /// Returns the total number of lines, including those stored in the history.
    fn line_count(&self) -> i32;

    /// Sets the history store used by this emulation.  When new lines are added to the output,
    /// older lines at the top of the screen are transferred to a history store.
    ///
    /// The number of lines which are kept and the storage location depend on the type of store.
    fn set_history(&mut self, history_type: Box<dyn HistoryType>);

    /// Returns the history store used by this emulation.  @see set_history().
    fn history(&self) -> Rc<Box<dyn HistoryType>>;

    /// Clears the history scroll.
    fn clear_history(&mut self);

    /// Copies the output history from @p startLine to @p endLine
    /// into @p stream, using @p decoder to convert the terminal characters into text.
    ///
    /// @param decoder A decoder which converts lines of terminal characters with
    /// appearance attributes into output text.  PlainTextDecoder is the most commonly used decoder. <br>
    /// @param startLine Index of first line to copy <br>
    /// @param endLine Index of last line to copy
    fn write_to_stream(
        &mut self,
        decoder: &mut dyn TerminalCharacterDecoder,
        start_line: i32,
        end_line: i32,
    );

    /// Return the char of erase.
    fn erase_char(&self) -> char;

    /// Sets the key bindings used to key events ( received through send_key_event() ) into character
    /// streams to send to the terminal.
    fn set_keyboard_layout<T: ToString>(&mut self, name: T);

    /// Returns the name of the emulation's current key bindings.
    /// @see set_key_bindings()
    fn keyboard_layout(&self) -> String;

    /// Copies the current image into the history and clears the screen.
    fn clear_entire_screen(&mut self);

    /// Resets the state of the terminal.
    fn reset(&mut self);

    /// Returns true if the active terminal program wants mouse input events.
    fn program_use_mouse(&self) -> bool;
    ///The programUsesMouseChanged() signal is emitted when this changes.
    fn set_use_mouse(&mut self, on: bool);

    fn program_bracketed_paste_mode(&self) -> bool;
    fn set_bracketed_paste_mode(&mut self, on: bool);

    fn set_mode(&mut self, mode: i32);
    fn reset_mode(&mut self, mode: i32);

    /// Processes an incoming character.  @see receive_data() <br>
    /// @p ch A unicode character code.
    fn receive_char(&mut self, ch: wchar_t);

    /// Sets the active screen.  The terminal has two screens, primary andalternate.
    /// The primary screen is used by default.  When certain interactive
    /// programs such as Vim are run, they trigger a switch to the alternate screen.
    ///
    /// @param index 0 to switch to the primary screen, or 1 to switch to the alternate screen
    fn set_screen(&mut self, index: i32);

    ////////////////////////////////////////////////// Signals //////////////////////////////////////////////////
    /// Emitted when a buffer of data is ready to send to the standard input of the terminal.
    ///
    /// @param data The buffer of data ready to be sent <br>
    /// @param len The length of @p data in bytes
    fn send_data() -> &'static str {
        Self::ACTION_SEND_DATA
    }

    ///  Requests that sending of input to the emulation from the terminal process be suspended or resumed.
    ///
    /// @param suspend If true, requests that sending of input from the terminal process' stdout be suspended.  
    /// Otherwise requests that sending of input be resumed.
    fn lock_pty_request() -> &'static str {
        Self::ACTION_LOCK_PTY_REQUEST
    }

    /// Requests that the pty used by the terminal process be set to UTF 8 mode.
    fn use_utf8_request() -> &'static str {
        Self::ACTION_USE_UTF8_REQUEST
    }

    /// Emitted when the activity state of the emulation is set.
    ///
    /// @param state The new activity state, one of NOTIFYNORMAL, NOTIFYACTIVITY or NOTIFYBELL
    fn state_set() -> &'static str {
        Self::ACTION_STATE_SET
    }

    /// Emmitted when the `zmodem` detected.
    fn zmodem_detected() -> &'static str {
        Self::ACTION_ZMODEM_DETECTED
    }

    /// Requests that the color of the text used to represent the tabs associated with this
    /// emulation be changed.  This is a Konsole-specific extension from pre-KDE 4 times.
    fn change_tab_text_color_request() -> &'static str {
        Self::ACTION_CHANGE_TAB_TEXT_COLOR_REQUEST
    }

    /// This is emitted when the program running in the shell indicates whether or not it is interested in mouse events.
    ///
    /// @param usesMouse This will be true if the program wants to be informed about mouse events or false otherwise.
    fn program_uses_mouse_changed() -> &'static str {
        Self::ACTION_PROGRAM_USE_MOUSE_CHANGED
    }

    fn program_bracketed_paste_mode_changed() -> &'static str {
        Self::ACTION_PROGRAM_BRACKETED_PASTE_MODE_CHANGED
    }

    /// Emitted when the contents of the screen image change.
    /// The emulation buffers the updates from successive image changes,
    /// and only emits outputChanged() at sensible intervals when there is a lot of terminal activity.
    ///
    /// Normally there is no need for objects other than the screen windows
    /// created with createWindow() to listen for this signal.
    ///
    /// ScreenWindow objects created using createWindow() will emit their
    /// own outputChanged() signal in response to this signal.
    fn output_changed() -> &'static str {
        Self::ACTION_OUTPUT_CHANGED
    }

    /// Emitted when the program running in the terminal wishes to update the session's title.
    /// This also allows terminal programs to customize other aspects of the terminal emulation display.
    ///
    /// This signal is emitted when the escape sequence "\033]ARG;VALUE\007" is received in the input string,
    /// where ARG is a number specifying what should change and VALUE is a string specifying the new value.
    ///
    /// @param title Specifies what to change.
    /// <ul>
    /// <li>0 - Set window icon text and session title to @p newTitle</li>
    /// <li>1 - Set window icon text to @p newTitle</li>
    /// <li>2 - Set session title to @p newTitle</li>
    /// <li>11 - Set the session's default background color to @p newTitle,
    ///         where @p newTitle can be an HTML-style string ("#RRGGBB") or a
    /// named color (eg 'red', 'blue').
    /// </li>
    /// <li>31 - Supposedly treats @p newTitle as a URL and opens it (NOT
    /// IMPLEMENTED)</li> <li>32 - Sets the icon associated with the session.  @p
    /// newTitle is the name of the icon to use, which can be the name of any icon
    /// in the current KDE icon theme (eg: 'konsole', 'kate', 'folder_home')</li>
    /// </ul>
    /// @param newTitle Specifies the new title
    fn title_changed() -> &'static str {
        Self::ACTION_TITLE_CHANGED
    }

    /// Emitted when the program running in the terminal changes the screen size.
    fn image_size_changed() -> &'static str {
        Self::ACTION_IMAGE_SIZE_CHANGED
    }

    /// Emitted when the setImageSize() is called on this emulation for the first time.
    fn image_size_initialized() -> &'static str {
        Self::ACTION_IMAGE_SIZE_INITIALIZED
    }

    /// Emitted after receiving the escape sequence which asks to change the terminal emulator's size.
    fn image_resize_request() -> &'static str {
        Self::ACTION_IMAGE_RESIZE_REQUEST
    }

    /// Emitted when the terminal program requests to change various properties of the terminal display.
    ///
    /// A profile change command occurs when a special escape sequence, followed
    /// by a string containing a series of name and value pairs is received.
    /// This string can be parsed using a ProfileCommandParser instance.
    ///
    /// @param text A string expected to contain a series of key and value pairs in
    /// the form:  name=value;name2=value2 ...
    fn profile_change_command_received() -> &'static str {
        Self::ACTION_PROFILE_CHANGE_COMMAND_RECEIVED
    }

    /// Emitted when a flow control key combination ( Ctrl+S or Ctrl+Q ) is pressed.
    ///
    /// @param suspendKeyPressed True if Ctrl+S was pressed to suspend output or Ctrl+Q to resume output.
    fn flow_control_key_pressed() -> &'static str {
        Self::ACTION_FLOW_CONTROL_KEY_PRESSED
    }

    /// Emitted when the cursor shape or its blinking state is changed via DECSCUSR sequences.
    ////
    /// @param cursorShape One of 3 possible values in KeyboardCursorShape enum. <br>
    /// @param blinkingCursorEnabled Whether to enable blinking or not
    fn cursor_changed() -> &'static str {
        Self::ACTION_CURSOR_CHANGED
    }

    fn handle_command_from_keyboard() -> &'static str {
        Self::ACTION_HANDLE_COMMAND_FROM_KEYBOARD
    }

    fn output_from_keypress_event() -> &'static str {
        Self::ACTION_OUTPUT_FROM_KEY_PRESS_EVENT
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /// Change the size of the emulation's image.
    fn set_image_size(&self, lines: i32, columns: i32);

    /// Interprets a sequence of characters and sends the result to the terminal.
    /// This is equivalent to calling sendKeyEvent() for each character in @p text in succession.
    fn send_text(&self, text: String);

    /// Interprets a key press event and emits the sendData() signal with
    /// the resulting character stream.
    fn send_key_event(&self, event: KeyEvent, from_paste: bool);

    /// Converts information about a mouse event into an xterm-compatible escape
    /// sequence and emits the character sequence via sendData()
    fn send_mouse_event(&self, buttons: i32, column: i32, line: i32, event_type: u8);

    /// Sends a string of characters to the foreground terminal process.
    ///
    /// @param string The characters to send. <br>
    /// @param length Length of @p string or if set to a negative value, @p string
    /// will be treated as a null-terminated string and its length will be determined automatically.
    fn send_string(&self, string: String, length: i32);

    /// Processes an incoming stream of characters.  receiveData() decodes the
    /// incoming character buffer using the current codec(), and then calls
    /// receiveChar() for each unicode character in the resulting buffer.
    ///
    /// receiveData() also starts a timer which causes the outputChanged() signal to be emitted when it expires.
    /// The timer allows multiple updates in quick succession to be buffered into a single outputChanged() signal emission.
    ///
    /// @param buffer A string of characters received from the terminal program. <br>
    /// @param len The length of @p buffer
    fn receive_data(&self, buffer: Vec<u8>, len: i32);

    /// triggered by timer, causes the emulation to send an updated screen image to each view.
    fn show_bulk(&self);

    /// Schedules an update of attached views.
    /// Repeated calls to bufferedUpdate() in close succession will result in only
    /// a single update, much like the Qt buffered update of widgets.
    fn buffer_update(&self);

    fn uses_mouse_changed(&self, uses_mouse: bool);

    fn bracketed_paste_mode_changed(&self, bracketed_paste_mode: bool);
}

pub trait EmulationActionInitalizer: ActionHubExt + Emulation {
    fn initialize_action_slots(&self) {}
}

impl Emulation for EmulationStorage {
    fn create_window(&self) -> Rc<RefCell<Box<ScreenWindow>>> {
        todo!()
    }

    fn image_size(&self) -> Size {
        todo!()
    }

    fn line_count(&self) -> i32 {
        todo!()
    }

    fn set_history(&mut self, history_type: Box<dyn HistoryType>) {
        todo!()
    }

    fn history(&self) -> Rc<Box<dyn HistoryType>> {
        todo!()
    }

    fn clear_history(&mut self) {
        todo!()
    }

    fn write_to_stream(
        &mut self,
        decoder: &mut dyn TerminalCharacterDecoder,
        start_line: i32,
        end_line: i32,
    ) {
        todo!()
    }

    fn erase_char(&self) -> char {
        todo!()
    }

    fn set_keyboard_layout<T: ToString>(&mut self, name: T) {
        todo!()
    }

    fn keyboard_layout(&self) -> String {
        todo!()
    }

    fn clear_entire_screen(&mut self) {
        todo!()
    }

    fn reset(&mut self) {
        todo!()
    }

    fn program_use_mouse(&self) -> bool {
        todo!()
    }

    fn set_use_mouse(&mut self, on: bool) {
        todo!()
    }

    fn program_bracketed_paste_mode(&self) -> bool {
        todo!()
    }

    fn set_bracketed_paste_mode(&mut self, on: bool) {
        todo!()
    }

    fn set_mode(&mut self, mode: i32) {
        todo!()
    }

    fn reset_mode(&mut self, mode: i32) {
        todo!()
    }

    fn receive_char(&mut self, ch: wchar_t) {
        todo!()
    }

    fn set_screen(&mut self, index: i32) {
        todo!()
    }

    ////////////////////////////////////////////////// Slots //////////////////////////////////////////////////
    fn set_image_size(&self, lines: i32, columns: i32) {
        todo!()
    }

    fn send_text(&self, text: String) {
        todo!()
    }

    fn send_key_event(&self, event: KeyEvent, from_paste: bool) {
        todo!()
    }

    fn send_mouse_event(&self, buttons: i32, column: i32, line: i32, event_type: u8) {
        todo!()
    }

    fn send_string(&self, string: String, length: i32) {
        todo!()
    }

    fn receive_data(&self, buffer: Vec<u8>, len: i32) {
        todo!()
    }

    fn show_bulk(&self) {
        todo!()
    }

    fn buffer_update(&self) {
        todo!()
    }

    fn uses_mouse_changed(&self, uses_mouse: bool) {
        todo!()
    }

    fn bracketed_paste_mode_changed(&self, bracketed_paste_mode: bool) {
        todo!()
    }
}