#![allow(dead_code)]
use super::screen::{bound, Screen};
use crate::tools::{
    character::{Character, LineProperty},
    translators::Command,
};
use std::ptr::NonNull;
use tmui::{
    graphics::figure::Rect,
    prelude::*,
    tlib::{
        emit,
        object::{ObjectImpl, ObjectSubclass},
        signals,
    },
};

/// Describes the units which scroll_by() moves the window by.
pub enum RelativeScrollMode {
    /// Scroll the window down by a given number of lines.
    ScrollLines,
    /// Scroll the window down by a given number of pages, where one page is window_lines() lines.
    ScrollPages,
}

/// Provides a window onto a section of a terminal screen.  A terminal widget can then render the contents of
/// the window and use the window to change the terminal screen's selection in response to mouse or keyboard input.
///
/// A new ScreenWindow for a terminal session can be created by calling Emulation::createWindow()
///
/// Use the scroll_to() method to scroll the window up and down on the screen.
/// Use the get_image() method to retrieve the character image which is currently visible in the window.
///
/// set_track_output() controls whether the window moves to the bottom of the associated screen when new lines are added to it.
///
/// Whenever the output from the underlying screen is changed, the notify_output_changed() slot should be called.  
/// This in turn will update the window's position and emit the output_changed() signal if necessary.
#[extends_object]
#[derive(Default)]
pub struct ScreenWindow {
    screen: Option<NonNull<Screen>>,
    window_buffer: Option<Box<Vec<Character>>>,
    window_buffer_size: i32,
    buffer_needs_update: bool,

    window_lines: i32,
    /// @see scroll_to(), current_line()
    current_line: i32,
    /// @see set_track_output(), track_output()
    track_output: bool,
    /// count of lines which the window has been scrolled by since the last call to reset_scroll_count()
    scroll_count: i32,
}
impl ObjectSubclass for ScreenWindow {
    const NAME: &'static str = "ScreenWindow";

    type Type = ScreenWindow;

    type ParentType = Object;
}
impl ObjectImpl for ScreenWindow {}

////////////////////////////////////////////////// Signals //////////////////////////////////////////////////
pub trait ScreenWindowSignals: ActionExt {
    signals! {
        /// Emitted when the contents of the associated terminal screen (see screen()) changes.
        output_changed();

        /// Emitted when the screen window is scrolled to a different position.
        ///
        /// @param line The line which is now at the top of the window.
        scrolled();

        /// Emitted when the selection is changed.
        selection_changed();

        /// Emitted when handle command `ScrollDownToBottomCommand` from keyboard.
        scroll_to_end();
    }
}

impl ScreenWindowSignals for ScreenWindow {}

impl ScreenWindow {
    /// Constructs a new screen window with the given parent.
    /// A screen must be specified by calling set_screen() before calling get_image() or getLineProperties().
    ///
    /// Should not call this constructor directly, instead use the Emulation::create_window() method to create a window
    /// on the emulation which you wish to view.  This allows the emulation to notify the window when the
    /// associated screen has changed and synchronize selection updates between all views on a session.
    pub fn new() -> Box<Self> {
        let mut object: Self = Object::new(&[]);
        object.screen = None;
        object.window_buffer = None;
        object.window_buffer_size = 0;
        object.buffer_needs_update = true;
        object.window_lines = 1;
        object.current_line = 0;
        object.track_output = true;
        object.scroll_count = 0;

        Box::new(object)
    }

    pub fn connect_notify_output_changed(&mut self, signal: Signal) {
        let ptr = NonNull::new(self as *mut Self);
        self.connect_action(signal, move |_| {
            let mut ptr = ptr;
            if let Some(window) = ptr.as_mut() {
                unsafe { window.as_mut().notify_output_changed() }
            }
        });
    }

    pub fn connect_handle_command_from_keyboard(&mut self, signal: Signal) {
        let ptr = NonNull::new(self as *mut Self);
        self.connect_action(signal, move |param| {
            let command = param.unwrap().get::<u16>();
            let mut ptr = ptr;
            if let Some(window) = ptr.as_mut() {
                unsafe { window.as_mut().handle_command_from_keyboard(command) }
            }
        });
    }

    pub fn connect_scroll_to_end(&mut self, signal: Signal) {
        let ptr = NonNull::new(self as *mut Self);
        self.connect_action(signal, move |param| {
            let line = param.unwrap().get::<i32>();
            let mut ptr = ptr;
            if let Some(window) = ptr.as_mut() {
                unsafe { window.as_mut().scroll_to(line) }
            }
        });
    }

    /// Sets the screen which this window looks onto
    pub fn set_screen(&mut self, screen: Option<NonNull<Screen>>) {
        self.screen = screen;
    }

    /// Returns the screen which this window looks onto.
    #[inline]
    pub fn screen(&self) -> &Screen {
        unsafe { self.screen.as_ref().unwrap().as_ref() }
    }

    /// Returns the image of characters which are currently visible through this
    /// window onto the screen.
    pub fn get_image(&mut self) -> &mut Vec<Character> {
        // reallocate internal buffer if the window size has changed
        let size = self.window_lines() * self.window_columns();
        if self.window_buffer.is_none() || self.window_buffer_size != size {
            self.window_buffer_size = size;
            self.window_buffer = Some(Box::new(vec![Character::default(); size as usize]));
            self.buffer_needs_update = true;
        }

        if !self.buffer_needs_update {
            return self.window_buffer.as_deref_mut().unwrap();
        }

        let current_line = self.current_line();
        let end_line = self.end_window_line();
        unsafe {
            self.screen.as_ref().unwrap().as_ref().get_image(
                self.window_buffer.as_deref_mut().unwrap(),
                size,
                current_line,
                end_line,
            )
        };

        // this window may look beyond the end of the screen, in which
        // case there will be an unused area which needs to be filled with blank characters.
        self.fill_unused_area();

        self.buffer_needs_update = false;
        self.window_buffer.as_deref_mut().unwrap()
    }

    /// Returns the line attributes associated with the lines of characters which
    /// are currently visible through this window
    pub fn get_line_properties(&self) -> Vec<LineProperty> {
        let mut result = self
            .screen()
            .get_line_properties(self.current_line(), self.end_window_line());

        if result.len() != self.window_lines as usize {
            result.resize(self.window_lines() as usize, 0);
        }

        result
    }

    /// Returns the number of lines which the region of the window specified by scroll_region() has been scrolled by
    /// since the last call to reset_scroll_count().  scroll_region() is in most cases the whole window,
    /// but will be a smaller area in, for example, applications which provide split-screen facilities.
    ///
    /// This is not guaranteed to be accurate, but allows views to optimize rendering by reducing the amount of
    /// costly text rendering that needs to be done when the output is scrolled.
    pub fn scroll_count(&self) -> i32 {
        self.scroll_count
    }

    /// Resets the count of scrolled lines returned by scroll_count().
    pub fn reset_scroll_count(&mut self) {
        self.scroll_count = 0
    }

    /// Returns the area of the window which was last scrolled, this is usually the whole window area.
    ///
    /// Like scroll_count(), this is not guaranteed to be accurate, but allows views to optimize rendering.
    pub fn scroll_region(&self) -> Rect {
        let equal_to_screen_size = self.window_lines() == self.screen().get_lines();

        if self.at_end_of_output() && equal_to_screen_size {
            self.screen().last_scrolled_region().clone()
        } else {
            Rect::new(0, 0, self.window_columns(), self.window_lines)
        }
    }

    /// Sets the start of the selection to the given @p line and @p column within the window.
    pub fn set_selection_start(&mut self, column: i32, line: i32, column_mode: bool) {
        let current_line = self.current_line();
        let end_line = self.end_window_line();
        unsafe {
            self.screen.as_mut().unwrap().as_mut().set_selection_start(
                column,
                (line + current_line).min(end_line),
                column_mode,
            )
        };

        self.buffer_needs_update = true;
        emit!(self.selection_changed());
    }

    /// Sets the end of the selection to the given @p line and @p column within the window.
    pub fn set_selection_end(&mut self, column: i32, line: i32) {
        let current_line = self.current_line();
        let end_line = self.end_window_line();
        unsafe {
            self.screen
                .as_mut()
                .unwrap()
                .as_mut()
                .set_selection_end(column, (line + current_line).min(end_line))
        };

        self.buffer_needs_update = true;
        emit!(self.selection_changed());
    }

    /// Retrieves the start of the selection within the window.
    pub fn get_selection_start(&self, column: &mut i32, line: &mut i32) {
        self.screen().get_selection_start(column, line);
        *line -= self.current_line();
    }

    /// Retrieves the end of the selection within the window.
    pub fn get_selection_end(&self, column: &mut i32, line: &mut i32) {
        self.screen().get_selection_end(column, line);
        *line -= self.current_line();
    }

    /// Returns true if the character at @p line , @p column is part of the selection.
    pub fn is_selected(&self, column: i32, line: i32) -> bool {
        let current_line = self.current_line();
        let end_line = self.end_window_line();
        self.screen()
            .is_selected(column, (line + current_line).min(end_line))
    }

    /// Clears the current selection.
    pub fn clear_selection(&mut self) {
        unsafe { self.screen.as_mut().unwrap().as_mut().clear_selection() };

        emit!(self.selection_changed());
    }

    /// Sets the number of lines in the window.
    pub fn set_window_lines(&mut self, lines: i32) {
        assert!(lines > 0);
        self.window_lines = lines;
    }

    /// Returns the number of lines in the window.
    pub fn window_lines(&self) -> i32 {
        self.window_lines
    }

    /// Returns the number of columns in the window.
    pub fn window_columns(&self) -> i32 {
        self.screen().get_columns()
    }

    /// Returns the total number of lines in the screen.
    pub fn line_count(&self) -> i32 {
        self.screen().get_history_lines() + self.screen().get_lines()
    }

    /// Returns the total number of columns in the screen.
    pub fn column_count(&self) -> i32 {
        self.screen().get_columns()
    }

    /// Returns the index of the line which is currently at the top of this window.
    pub fn current_line(&self) -> i32 {
        bound(
            0,
            self.current_line,
            self.line_count() - self.window_lines(),
        )
    }

    /// Returns the position of the cursor within the window.
    pub fn cursor_position(&self) -> Point {
        Point::new(self.screen().get_cursor_x(), self.screen().get_cursor_y())
    }

    /// Returns true if the window is currently at the bottom of the screen.
    pub fn at_end_of_output(&self) -> bool {
        self.current_line() == self.line_count() - self.window_lines()
    }

    /// Scrolls the window so that @p line is at the top of the window.
    pub fn scroll_to(&mut self, line: i32) {
        let max_current_line_number = self.line_count() - self.window_lines();
        let line = bound(0, line, max_current_line_number);

        let delta = line - self.current_line;
        self.current_line = line;

        // Keep track of number of lines scrolled by, this can be reset by calling reset_scroll_count()
        self.scroll_count += delta;

        self.buffer_needs_update = true;

        emit!(self.scrolled(), self.current_line());
    }

    /// Scrolls the window relative to its current position on the screen.
    ///
    /// @param mode Specifies whether @p amount refers to the number of lines or the number of pages to scroll.
    ///
    /// @param amount The number of lines or pages ( depending on @p mode ) to scroll by.  
    /// If this number is positive, the view is scrolled down.  If this number is negative, the view is scrolled up.
    pub fn scroll_by(&mut self, mode: RelativeScrollMode, amount: i32) {
        match mode {
            RelativeScrollMode::ScrollLines => self.scroll_to(self.current_line() + amount),
            RelativeScrollMode::ScrollPages => {
                self.scroll_to(self.current_line() + amount * (self.window_lines() / 2))
            }
        }
    }

    /// Specifies whether the window should automatically move to the bottom
    /// of the screen when new output is added.
    ///
    /// If this is set to true, the window will be moved to the bottom of the
    /// associated screen ( see screen() ) when the notify_output_changed() method is called.
    pub fn set_track_output(&mut self, track_output: bool) {
        self.track_output = track_output
    }

    /// Returns whether the window automatically moves to the bottom of the screen
    /// as new output is added.  See set_track_output()
    pub fn track_output(&self) -> bool {
        self.track_output
    }

    ///  Returns the text which is currently selected.
    ///
    /// @param preserveLineBreaks See Screen::selected_text()
    pub fn selected_text(&self, preserve_line_break: bool) -> String {
        self.screen().selected_text(preserve_line_break)
    }

    fn end_window_line(&self) -> i32 {
        (self.current_line() + self.window_lines() - 1).min(self.line_count() - 1)
    }

    fn fill_unused_area(&mut self) {
        let screen_end_line = self.screen().get_history_lines() + self.screen().get_lines() - 1;
        let window_end_line = self.current_line() + self.window_lines() - 1;

        let unused_lines = window_end_line - screen_end_line;
        let chars_to_till = unused_lines * self.window_columns();

        let buffer_slice = &mut self.window_buffer.as_deref_mut().unwrap()
            [(self.window_buffer_size - chars_to_till) as usize..];
        Screen::fill_with_default_char(buffer_slice, chars_to_till)
    }

    pub fn notify_output_changed(&mut self) {
        // move window to the bottom of the screen and update scroll count
        // if this window is currently tracking the bottom of the screen
        if self.track_output {
            let scrolled_line = self.screen().scrolled_lines();
            self.scroll_count -= scrolled_line;

            let history_line = self.screen().get_history_lines();
            let lines = self.screen().get_lines();
            self.current_line = 0.max(history_line - (self.window_lines() - lines));
        } else {
            // if the history is not unlimited then it may have run out of space and dropped the oldest
            // lines of output - in this case the screen window's current line number will need to
            // be adjusted - otherwise the output will scroll
            let dropped_lines = self.screen().dropped_lines();
            self.current_line = 0.max(self.current_line - dropped_lines);

            // ensure that the screen window's current position does
            // not go beyond the bottom of the screen
            let history_lines = self.screen().get_history_lines();
            self.current_line = self.current_line.min(history_lines);
        }

        self.buffer_needs_update = true;

        emit!(self.output_changed())
    }

    pub fn handle_command_from_keyboard(&mut self, command: u16) {
        let command = Command::from(command);
        let mut update = false;

        // EraseCommand is handled in Vt102Emulation.
        if command.has(Command::ScrollPageUpCommand) {
            self.scroll_by(RelativeScrollMode::ScrollPages, -1);
            update = true;
        }
        if command.has(Command::ScrollPageDownCommand) {
            self.scroll_by(RelativeScrollMode::ScrollPages, 1);
            update = true;
        }
        if command.has(Command::ScrollLineUpCommand) {
            self.scroll_by(RelativeScrollMode::ScrollLines, -1);
            update = true;
        }
        if command.has(Command::ScrollLineDownCommand) {
            self.scroll_by(RelativeScrollMode::ScrollLines, 1);
            update = true;
        }
        if command.has(Command::ScrollDownToBottomCommand) {
            emit!(self.scroll_to_end());
            update = true;
        }
        if command.has(Command::ScrollUpToTopCommand) {
            self.scroll_to(0);
            update = true;
        }

        if update {
            self.set_track_output(self.at_end_of_output());
            emit!(self.output_changed());
        }
    }
}
