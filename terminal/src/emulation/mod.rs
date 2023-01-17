#![allow(dead_code)]
pub mod vt102_emulation;
pub mod wrapper;

pub use vt102_emulation::*;
use widestring::U16String;
pub use wrapper::*;

use crate::{
    core::{
        screen::Screen,
        screen_window::{ScreenWindow, ScreenWindowSignals},
    },
    tools::{
        history::HistoryType,
        terminal_character_decoder::TerminalCharacterDecoder,
        translators::{KeyboardTranslator, KeyboardTranslatorManager},
    },
};
use std::{ptr::NonNull, rc::Rc};
use tmui::{
    graphics::figure::Size,
    prelude::*,
    tlib::{
        connect, disconnect, emit,
        events::KeyEvent,
        object::{ObjectImpl, ObjectSubclass},
        signals,
    },
};
use wchar::{wch, wchar_t};

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

/// The terminal emulation ( [`Emulation`] ) receives a serial stream of
/// characters from the program currently running in the terminal.
///
/// From this stream [`Screen`] creates an image of characters which is ultimately
/// rendered by the display widget ( [`TerminalView`] ).  Some types of emulation
/// may have more than one screen image.
///
/// Every running [`Session`] has one specified Emulation.
/// The common abstract to use in dyn trait object was the [`EmulationWrapper`]
#[extends_object]
#[derive(Default)]
pub struct BaseEmulation {
    /// The manager of keyboard translator.
    pub translator_manager: Option<NonNull<KeyboardTranslatorManager>>,
    /// The kayboard layout translator.
    pub key_translator: Option<NonNull<KeyboardTranslator>>,
    /// Current active screen.
    pub current_screen: Option<NonNull<Screen>>,
    /// 0 = primary screen. <br>
    /// 1 = alternate screen (used by vi,emocs etc. scrollBar is not enable in this mode).
    pub screen: [Box<Screen>; 2],

    windows: Vec<Box<ScreenWindow>>,
    use_mouse: bool,
    bracket_paste_mode: bool,
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
    fn new(translator_manager: Option<NonNull<KeyboardTranslatorManager>>) -> Self::Type;

    /// Wrap trait `Emulation` to `EmulationWrapper`.
    fn wrap(self: Self) -> Box<dyn EmulationWrapper> {
        let mut wrapper: Box<dyn EmulationWrapper> = Box::new(Some(self));
        wrapper.initialize();
        wrapper
    }

    /// initialize the emulation.
    fn initialize(&mut self);

    /// Creates a new window onto the output from this emulation.  The contents of the window are then rendered by views
    /// which are set to use this window using the TerminalDisplay::setScreenWindow() method.
    fn create_window(&mut self) -> Option<NonNull<ScreenWindow>>;

    /// Returns the size of the screen image which the emulation produces.
    fn image_size(&self) -> Size;

    /// Returns the total number of lines, including those stored in the history.
    fn line_count(&self) -> i32;

    /// Sets the history store used by this emulation.  When new lines are added to the output,
    /// older lines at the top of the screen are transferred to a history store.
    ///
    /// The number of lines which are kept and the storage location depend on the type of store.
    fn set_history(&mut self, history_type: Rc<dyn HistoryType>);

    /// Returns the history store used by this emulation.  @see set_history().
    fn history(&self) -> Rc<dyn HistoryType>;

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
    fn set_keyboard_layout(&mut self, name: &str);

    /// Returns the name of the emulation's current key bindings.
    /// @see set_key_bindings()
    fn keyboard_layout(&self) -> String;

    /// Copies the current image into the history and clears the screen.
    fn clear_entire_screen(&mut self);

    /// Resets the state of the terminal.
    fn reset(&self);

    /// Returns true if the active terminal program wants mouse input events.
    fn program_use_mouse(&self) -> bool;
    ///The programUsesMouseChanged() signal is emitted when this changes.
    fn set_use_mouse(&mut self, on: bool);

    fn program_bracketed_paste_mode(&self) -> bool;
    fn set_bracketed_paste_mode(&mut self, on: bool);

    fn set_mode(&mut self, mode: usize);
    fn reset_mode(&mut self, mode: usize);

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
    fn set_image_size(&mut self, lines: i32, columns: i32);

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
    fn receive_data(&mut self, buffer: Vec<u8>, len: i32);

    /// triggered by timer, causes the emulation to send an updated screen image to each view.
    fn show_bulk(&self);

    /// Schedules an update of attached views.
    /// Repeated calls to bufferedUpdate() in close succession will result in only
    /// a single update, much like the Qt buffered update of widgets.
    fn buffer_update(&self);

    fn uses_mouse_changed(&mut self, uses_mouse: bool);

    fn bracketed_paste_mode_changed(&mut self, bracketed_paste_mode: bool);

    fn emit_cursor_change(&mut self, cursor_shape: u8, enable_blinking_cursor: bool);
}

impl Emulation for BaseEmulation {
    type Type = BaseEmulation;

    fn new(translator_manager: Option<NonNull<KeyboardTranslatorManager>>) -> Self::Type {
        let mut screen_0 = Box::new(Screen::new(40, 80));
        let screen_1 = Box::new(Screen::new(40, 80));

        let mut emulation: Self = Object::new(&[]);
        emulation.translator_manager = translator_manager;
        emulation.key_translator = None;
        emulation.current_screen = NonNull::new(screen_0.as_mut() as *mut Screen);
        emulation.screen = [screen_0, screen_1];
        emulation.windows = vec![];
        emulation
    }

    fn initialize(&mut self) {
        connect!(
            self,
            program_uses_mouse_changed(),
            self,
            uses_mouse_changed(bool)
        );
        connect!(
            self,
            program_bracketed_paste_mode_changed(),
            self,
            bracketed_paste_mode_changed(bool)
        );
        connect!(self, cursor_changed(), self, emit_cursor_change(u8:0, bool:1));
    }

    fn create_window(&mut self) -> Option<NonNull<ScreenWindow>> {
        let mut window = ScreenWindow::new();

        window.set_screen(self.current_screen.clone());

        let window_ptr = NonNull::new(window.as_mut() as *mut ScreenWindow);
        self.windows.push(window);
        let len = self.windows.len();
        if len > 1 {
            let window_pre = self.windows.remove(0);
            disconnect!(window_pre, null, null, null);
            disconnect!(null, null, window_pre, null);
        }

        connect!(
            self.windows.last().unwrap(),
            selection_changed(),
            self,
            buffer_update()
        );

        connect!(
            self,
            output_changed(),
            self.windows.last_mut().unwrap(),
            notify_output_changed()
        );
        connect!(
            self,
            handle_command_from_keyboard(),
            self.windows.last_mut().unwrap(),
            handle_command_from_keyboard(u16)
        );
        connect!(
            self,
            output_from_keypress_event(),
            self.windows.last_mut().unwrap(),
            scroll_to(i32)
        );

        window_ptr
    }

    fn image_size(&self) -> Size {
        unsafe {
            Size::from((
                self.current_screen.as_ref().unwrap().as_ref().get_columns(),
                self.current_screen.as_ref().unwrap().as_ref().get_lines(),
            ))
        }
    }

    fn line_count(&self) -> i32 {
        unsafe {
            self.current_screen.as_ref().unwrap().as_ref().get_lines()
                + self
                    .current_screen
                    .as_ref()
                    .unwrap()
                    .as_ref()
                    .get_history_lines()
        }
    }

    fn set_history(&mut self, history_type: Rc<dyn HistoryType>) {
        self.screen[0].set_scroll(history_type, None)
    }

    fn history(&self) -> Rc<dyn HistoryType> {
        self.screen[0].get_scroll()
    }

    fn clear_history(&mut self) {
        let scroll = self.screen[0].get_scroll();
        self.screen[0].set_scroll(scroll, Some(false));
    }

    fn write_to_stream(
        &mut self,
        decoder: &mut dyn TerminalCharacterDecoder,
        start_line: i32,
        end_line: i32,
    ) {
        unsafe {
            self.current_screen
                .as_mut()
                .unwrap()
                .as_mut()
                .write_lines_to_stream(decoder, start_line, end_line)
        };
    }

    fn erase_char(&self) -> char {
        '\u{b}'
    }

    fn set_keyboard_layout(&mut self, name: &str) {
        unsafe {
            let translator = self
                .translator_manager
                .as_mut()
                .unwrap()
                .as_mut()
                .find_translator(name.to_string());
            self.key_translator = translator;
        }
    }

    fn keyboard_layout(&self) -> String {
        unsafe {
            self.key_translator
                .as_ref()
                .unwrap()
                .as_ref()
                .name()
                .to_string()
        }
    }

    fn clear_entire_screen(&mut self) {
        unsafe {
            self.current_screen
                .as_mut()
                .unwrap()
                .as_mut()
                .clear_entire_screen();
            self.buffer_update();
        }
    }

    fn reset(&self) {
        // Default implementation does nothing.
    }

    fn program_use_mouse(&self) -> bool {
        self.use_mouse
    }

    fn set_use_mouse(&mut self, on: bool) {
        self.use_mouse = on
    }

    fn program_bracketed_paste_mode(&self) -> bool {
        self.bracket_paste_mode
    }

    fn set_bracketed_paste_mode(&mut self, on: bool) {
        self.bracket_paste_mode = on
    }

    fn set_screen(&mut self, index: i32) {
        unsafe {
            let old = self.current_screen.as_ref().unwrap().as_ref().id();
            let current = NonNull::new(self.screen[(index & 1) as usize].as_mut() as *mut Screen);
            self.current_screen = current.clone();
            if old != current.as_ref().unwrap().as_ref().id() {
                // Tell all windows onto this emulation to switch to the newly active screen.
                for window in self.windows.iter_mut() {
                    window.set_screen(current.clone())
                }
            }
        }
    }

    fn set_mode(&mut self, _: usize) {
        // Default implementation does nothing.
    }

    fn reset_mode(&mut self, _: usize) {
        // Default implementation does nothing.
    }

    fn receive_char(&mut self, c: wchar_t) {
        let c = c & 0xff;
        let current_screen = unsafe { self.current_screen.as_mut().unwrap().as_mut() };
        match c {
            wch!('\u{b}') => current_screen.backspace(),
            wch!('\t') => current_screen.tab(1),
            wch!('\n') => current_screen.new_line(),
            wch!('\r') => current_screen.to_start_of_line(),
            0x07 => emit!(self.state_set(), EmulationState::NotifyBell as u8),
            _ => current_screen.display_character(c),
        }
    }

    ////////////////////////////////////////////////// Slots //////////////////////////////////////////////////
    fn set_image_size(&mut self, lines: i32, columns: i32) {
        if lines < 1 || columns < 1 {
            return;
        }

        let screen_size: [Size; 2] = [
            (self.screen[0].get_columns(), self.screen[0].get_lines()).into(),
            (self.screen[1].get_columns(), self.screen[1].get_lines()).into(),
        ];
        let new_size: Size = (columns, lines).into();
        if new_size == screen_size[0] && new_size == screen_size[1] {
            return;
        }

        self.screen[0].resize_image(lines, columns);
        self.screen[1].resize_image(lines, columns);

        emit!(self.image_size_changed(), (lines, columns));

        self.buffer_update();
    }

    fn send_text(&self, _: String) {
        // Default implementation does nothing.
    }

    fn send_key_event(&self, event: KeyEvent, _from_paste: bool) {
        emit!(self.state_set(), EmulationState::NotifyNormal as u8);

        if !event.text().is_empty() {
            emit!(self.send_data(), event.text())
        }
    }

    fn send_mouse_event(&self, _: i32, _: i32, _: i32, _: u8) {
        // Default implementation does nothing.
    }

    fn send_string(&self, _: String, _: i32) {
        // Default implementation does nothing.
    }

    fn receive_data(&mut self, buffer: Vec<u8>, len: i32) {
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

    fn show_bulk(&self) {
        // TODO: need add timer
        todo!()
    }

    fn buffer_update(&self) {
        // TODO: need add timer
        todo!()
    }

    fn uses_mouse_changed(&mut self, uses_mouse: bool) {
        self.use_mouse = uses_mouse
    }

    fn bracketed_paste_mode_changed(&mut self, bracketed_paste_mode: bool) {
        self.bracket_paste_mode = bracketed_paste_mode
    }

    fn emit_cursor_change(&mut self, cursor_shape: u8, blinking_cursor_enable: bool) {
        emit!(
            self.title_changed(),
            (
                50,
                format!(
                    "CursorShape={};BlinkingCursorEnabled={}",
                    cursor_shape, blinking_cursor_enable
                )
            )
        );
    }
}
