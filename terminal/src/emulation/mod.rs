#![allow(dead_code)]
pub mod vt102_emulation;
pub use vt102_emulation::*;

use crate::{
    core::{
        screen::Screen,
        screen_window::{ScreenWindow, ScreenWindowSignals},
    },
    tools::{
        history::HistoryType, terminal_character_decoder::TerminalCharacterDecoder,
        translators::{KeyboardTranslator, KeyboardTranslatorManager},
    },
};
use std::{
    cell::{Cell, RefCell},
    rc::Rc,
};
use tmui::{
    graphics::figure::Size,
    prelude::*,
    tlib::{
        emit,
        events::KeyEvent,
        object::{ObjectImpl, ObjectSubclass},
        signals,
    },
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

#[extends_object]
#[derive(Default)]
pub struct BaseEmulation {
    /// The manager of keyboard translator.
    pub translator_manager: Option<Rc<RefCell<KeyboardTranslatorManager>>>,
    /// The kayboard layout translator.
    pub key_translator: RefCell<Option<Rc<RefCell<Box<KeyboardTranslator>>>>>,
    /// Current active screen.
    pub current_screen: RefCell<Rc<RefCell<Box<Screen>>>>,
    /// 0 = primary screen. <br>
    /// 1 = alternate screen (used by vi,emocs etc. scrollBar is not enable in this mode).
    pub screen: RefCell<[Rc<RefCell<Box<Screen>>>; 2]>,

    windows: RefCell<Vec<Rc<RefCell<Box<ScreenWindow>>>>>,
    use_mouse: Cell<bool>,
    bracket_paste_mode: Cell<bool>,
}
impl ObjectSubclass for BaseEmulation {
    const NAME: &'static str = "BaseEmulation";

    type Type = BaseEmulation;

    type ParentType = Object;
}
impl ObjectImpl for BaseEmulation {}

pub trait Emulation: ActionExt + Sized + 'static {
    type Type: Emulation + ActionExt;

    /// Constructer to create a new Emulation.
    fn new(translator_manager: Rc<RefCell<KeyboardTranslatorManager>>) -> Rc<Self::Type>;

    /// Wrap trait `Emulation` to `EmulationWrapper`.
    fn wrap(self: Rc<Self>) -> Box<dyn EmulationWrapper> {
        Box::new(Some(self))
    }

    /// Creates a new window onto the output from this emulation.  The contents of the window are then rendered by views
    /// which are set to use this window using the TerminalDisplay::setScreenWindow() method.
    fn create_window(self: &Rc<Self>) -> Rc<RefCell<Box<ScreenWindow>>>;

    /// Returns the size of the screen image which the emulation produces.
    fn image_size(&self) -> Size;

    /// Returns the total number of lines, including those stored in the history.
    fn line_count(&self) -> i32;

    /// Sets the history store used by this emulation.  When new lines are added to the output,
    /// older lines at the top of the screen are transferred to a history store.
    ///
    /// The number of lines which are kept and the storage location depend on the type of store.
    fn set_history(&self, history_type: Rc<dyn HistoryType>);

    /// Returns the history store used by this emulation.  @see set_history().
    fn history(&self) -> Rc<dyn HistoryType>;

    /// Clears the history scroll.
    fn clear_history(&self);

    /// Copies the output history from @p startLine to @p endLine
    /// into @p stream, using @p decoder to convert the terminal characters into text.
    ///
    /// @param decoder A decoder which converts lines of terminal characters with
    /// appearance attributes into output text.  PlainTextDecoder is the most commonly used decoder. <br>
    /// @param startLine Index of first line to copy <br>
    /// @param endLine Index of last line to copy
    fn write_to_stream(
        &self,
        decoder: &mut dyn TerminalCharacterDecoder,
        start_line: i32,
        end_line: i32,
    );

    /// Return the char of erase.
    fn erase_char(&self) -> char;

    /// Sets the key bindings used to key events ( received through send_key_event() ) into character
    /// streams to send to the terminal.
    fn set_keyboard_layout(&self, name: &str);

    /// Returns the name of the emulation's current key bindings.
    /// @see set_key_bindings()
    fn keyboard_layout(&self) -> String;

    /// Copies the current image into the history and clears the screen.
    fn clear_entire_screen(&self);

    /// Resets the state of the terminal.
    fn reset(&self);

    /// Returns true if the active terminal program wants mouse input events.
    fn program_use_mouse(&self) -> bool;
    ///The programUsesMouseChanged() signal is emitted when this changes.
    fn set_use_mouse(&self, on: bool);

    fn program_bracketed_paste_mode(&self) -> bool;
    fn set_bracketed_paste_mode(&self, on: bool);

    fn set_mode(&self, mode: i32);
    fn reset_mode(&self, mode: i32);

    /// Processes an incoming character.  @see receive_data() <br>
    /// @p ch A unicode character code.
    fn receive_char(&self, ch: wchar_t);

    /// Sets the active screen.  The terminal has two screens, primary andalternate.
    /// The primary screen is used by default.  When certain interactive
    /// programs such as Vim are run, they trigger a switch to the alternate screen.
    ///
    /// @param index 0 to switch to the primary screen, or 1 to switch to the alternate screen
    fn set_screen(&self, index: i32);

    ////////////////////////////////////////////////// Signals //////////////////////////////////////////////////
    signals! {
        /// Emitted when a buffer of data is ready to send to the standard input of the terminal.
        ///
        /// @param data The buffer of data ready to be sent <br>
        /// @param len The length of @p data in bytes
        send_data();

        ///  Requests that sending of input to the emulation from the terminal process be suspended or resumed.
        ///
        /// @param suspend If true, requests that sending of input from the terminal process' stdout be suspended.
        /// Otherwise requests that sending of input be resumed.
        lock_pty_request();

        /// Requests that the pty used by the terminal process be set to UTF 8 mode.
        use_utf8_request();

        /// Emitted when the activity state of the emulation is set.
        ///
        /// @param state The new activity state, one of NOTIFYNORMAL, NOTIFYACTIVITY or NOTIFYBELL
        state_set();

        /// Emmitted when the `zmodem` detected.
        zmodem_detected();

        /// Requests that the color of the text used to represent the tabs associated with this
        /// emulation be changed.  This is a Konsole-specific extension from pre-KDE 4 times.
        change_tab_text_color_request();

        /// This is emitted when the program running in the shell indicates whether or not it is interested in mouse events.
        ///
        /// @param usesMouse This will be true if the program wants to be informed about mouse events or false otherwise.
        program_uses_mouse_changed();

        program_bracketed_paste_mode_changed();

        /// Emitted when the contents of the screen image change.
        /// The emulation buffers the updates from successive image changes,
        /// and only emits outputChanged() at sensible intervals when there is a lot of terminal activity.
        ///
        /// Normally there is no need for objects other than the screen windows
        /// created with createWindow() to listen for this signal.
        ///
        /// ScreenWindow objects created using createWindow() will emit their
        /// own outputChanged() signal in response to this signal.
        output_changed();

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
        title_changed();

        /// Emitted when the program running in the terminal changes the screen size.
        image_size_changed();

        /// Emitted when the setImageSize() is called on this emulation for the first time.
        image_size_initialized();

        /// Emitted after receiving the escape sequence which asks to change the terminal emulator's size.
        image_resize_request();

        /// Emitted when the terminal program requests to change various properties of the terminal display.
        ///
        /// A profile change command occurs when a special escape sequence, followed
        /// by a string containing a series of name and value pairs is received.
        /// This string can be parsed using a ProfileCommandParser instance.
        ///
        /// @param text A string expected to contain a series of key and value pairs in
        /// the form:  name=value;name2=value2 ...
        profile_change_command_received();

        /// Emitted when a flow control key combination ( Ctrl+S or Ctrl+Q ) is pressed.
        ///
        /// @param suspendKeyPressed True if Ctrl+S was pressed to suspend output or Ctrl+Q to resume output.
        flow_control_key_pressed();

        /// Emitted when the cursor shape or its blinking state is changed via DECSCUSR sequences.
        ////
        /// @param cursorShape One of 3 possible values in KeyboardCursorShape enum. <br>
        /// @param blinkingCursorEnabled Whether to enable blinking or not
        cursor_changed();

        handle_command_from_keyboard();

        output_from_keypress_event();
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////// Slots //////////////////////////////////////////////////
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

impl Emulation for BaseEmulation {
    type Type = BaseEmulation;

    fn new(translator_manager: Rc<RefCell<KeyboardTranslatorManager>>) -> Rc<Self::Type> {
        let screen_0 = Rc::new(RefCell::new(Box::new(Screen::new(40, 80))));
        let screen_1 = Rc::new(RefCell::new(Box::new(Screen::new(40, 80))));

        let mut object: Self = Object::new(&[]);
        object.translator_manager = Some(translator_manager);
        object.key_translator = RefCell::new(None);
        object.current_screen = RefCell::new(screen_0.clone());
        object.screen = RefCell::new([screen_0, screen_1]);
        object.windows = RefCell::new(vec![]);

        let emulation = Rc::new(object);

        let rc = emulation.clone();
        emulation.connect_action(emulation.program_uses_mouse_changed(), move |param| {
            let uses_mouse = param.unwrap().get::<bool>();
            rc.uses_mouse_changed(uses_mouse);
        });

        let rc = emulation.clone();
        emulation.connect_action(
            emulation.program_bracketed_paste_mode_changed(),
            move |param| {
                let bracketed_paste_mode = param.unwrap().get::<bool>();
                rc.bracketed_paste_mode_changed(bracketed_paste_mode);
            },
        );

        let rc = emulation.clone();
        emulation.connect_action(emulation.cursor_changed(), move |param| {
            let (cursor_shape, blinking_cursor_enable) = param.unwrap().get::<(u8, bool)>();
            emit!(
                rc.title_changed(),
                (
                    50,
                    format!(
                        "CursorShape={};BlinkingCursorEnabled={}",
                        cursor_shape, blinking_cursor_enable
                    )
                )
            );
        });

        emulation
    }

    fn create_window(self: &Rc<Self>) -> Rc<RefCell<Box<ScreenWindow>>> {
        let window = ScreenWindow::new();

        window
            .borrow_mut()
            .set_screen(self.current_screen.borrow().clone());
        self.windows.borrow_mut().push(window.clone());
        let len = self.windows.borrow().len();
        if len > 1 {
            self.windows.borrow_mut().remove(0);
        }

        let rc = self.clone();
        self.connect_action(window.borrow().selection_changed(), move |_| {
            rc.buffer_update();
        });

        ScreenWindow::connect_notify_output_changed(&window, self.output_changed());
        ScreenWindow::connect_handle_command_from_keyboard(
            &window,
            self.handle_command_from_keyboard(),
        );
        ScreenWindow::connect_scroll_to_end(&window, self.output_from_keypress_event());

        window
    }

    fn image_size(&self) -> Size {
        Size::from((
            self.current_screen.borrow().borrow().get_columns(),
            self.current_screen.borrow().borrow().get_lines(),
        ))
    }

    fn line_count(&self) -> i32 {
        self.current_screen.borrow().borrow().get_lines()
            + self.current_screen.borrow().borrow().get_history_lines()
    }

    fn set_history(&self, history_type: Rc<dyn HistoryType>) {
        self.screen.borrow()[0]
            .borrow_mut()
            .set_scroll(history_type, None)
    }

    fn history(&self) -> Rc<dyn HistoryType> {
        self.screen.borrow()[0].borrow().get_scroll()
    }

    fn clear_history(&self) {
        let scroll = self.screen.borrow()[0].borrow().get_scroll();
        self.screen.borrow()[0]
            .borrow_mut()
            .set_scroll(scroll, Some(false));
    }

    fn write_to_stream(
        &self,
        decoder: &mut dyn TerminalCharacterDecoder,
        start_line: i32,
        end_line: i32,
    ) {
        self.current_screen
            .borrow()
            .borrow_mut()
            .write_lines_to_stream(decoder, start_line, end_line);
    }

    fn erase_char(&self) -> char {
        '\u{b}'
    }

    fn set_keyboard_layout(&self, name: &str) {
        todo!()
    }

    fn keyboard_layout(&self) -> String {
        todo!()
    }

    fn clear_entire_screen(&self) {
        todo!()
    }

    fn reset(&self) {
        todo!()
    }

    fn program_use_mouse(&self) -> bool {
        todo!()
    }

    fn set_use_mouse(&self, on: bool) {
        todo!()
    }

    fn program_bracketed_paste_mode(&self) -> bool {
        todo!()
    }

    fn set_bracketed_paste_mode(&self, on: bool) {
        todo!()
    }

    fn set_mode(&self, mode: i32) {
        todo!()
    }

    fn reset_mode(&self, mode: i32) {
        todo!()
    }

    fn receive_char(&self, ch: wchar_t) {
        todo!()
    }

    fn set_screen(&self, index: i32) {
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
