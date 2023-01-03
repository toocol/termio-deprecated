#![allow(dead_code)]
use crate::tools::{
    character::{
        Character, LineProperty, DEFAULT_RENDITION, LINE_DEFAULT, LINE_WRAPPED, RE_BOLD, RE_CURSOR,
        RE_REVERSE,
    },
    character_color::{
        CharacterColor, COLOR_SPACE_DEFAULT, DEFAULT_BACK_COLOR, DEFAULT_FORE_COLOR,
    },
    history::{HistoryScroll, HistoryScrollNone, HistoryScrollWrapper, HistoryType},
    system_ffi::wcwidth,
    terminal_character_decoder::{PlainTextDecoder, TerminalCharacterDecoder},
    text_stream::TextStream,
};
use bitvec::vec::BitVec;
use std::{cell::RefCell, rc::Rc};
use tmui::{graphics::figure::Rect, prelude::*, tlib::object::{ObjectSubclass, ObjectImpl}};
use wchar::{wch, wchar_t};

const MODE_ORIGIN: usize = 0;
const MODE_WRAP: usize = 1;
const MODE_INSERT: usize = 2;
const MODE_SCREEN: usize = 3;
const MODE_CURSOR: usize = 4;
const MODE_NEWLINE: usize = 5;

pub const MODES_SCREEN: usize = 6;

const MAX_CHARS: usize = 1024;

static BS_CLEARS: bool = false;

#[inline]
pub fn bound<T: Ord>(min: T, val: T, max: T) -> T {
    assert!(max >= min);
    min.max(max.min(val))
}

#[derive(Debug, Default)]
pub struct SavedState {
    cursor_column: i32,
    cursor_line: i32,
    rendition: u8,
    foreground: CharacterColor,
    background: CharacterColor,
}
impl SavedState {
    pub fn new() -> Self {
        Self {
            cursor_column: 0,
            cursor_line: 0,
            rendition: 0,
            foreground: CharacterColor::empty(),
            background: CharacterColor::empty(),
        }
    }
}

pub type ImageLine = Vec<Character>;

/// An image of characters with associated attributes.
///
/// The terminal emulation ( Emulation ) receives a serial stream of
/// characters from the program currently running in the terminal.
/// From this stream `Screen` creates an image of characters which is ultimately
/// rendered by the display widget ( TerminalView ).  Some types of emulation
/// may have more than one screen image.
///
/// get_image() is used to retrieve the currently visible image
/// which is then used by the display widget to draw the output from the terminal.
///
/// The number of lines of output history which are kept in addition to the
/// current screen image depends on the history scroll being used to store the
/// output. The scroll is specified using set_scroll() The output history can be retrieved using write_to_stream()
///
/// The screen image has a selection associated with it, specified using
/// set_selection_start() and set_selection_end().  The selected text can be
/// retrieved using selected_text().  When get_image() is used to retrieve the
/// visible image, characters which are part of the selection have their colours inverted.
#[extends_object]
pub struct Screen {
    lines: i32,
    columns: i32,

    ////// [lines[column]]
    screen_lines: Box<Vec<ImageLine>>,

    scrolled_lines: i32,
    last_scolled_region: Rect,

    dropped_lines: i32,

    line_properties: Box<Vec<LineProperty>>,

    ////// History buffer.
    history: Rc<Box<dyn HistoryScrollWrapper>>,

    ////// Cursor location.
    cursor_x: i32,
    cursor_y: i32,

    ////// Cursor color and rendition info.
    cursor_foreground: CharacterColor,
    cursor_background: CharacterColor,
    cursor_rendition: u8,

    ////// Margins
    top_margin: i32,
    bottom_margin: i32,

    ////// States
    current_modes: [bool; MODES_SCREEN],
    saved_modes: [bool; MODES_SCREEN],

    tab_stops: BitVec,

    ////// Selections
    // The first location selected.
    select_begin: i32,
    // Top left location.
    select_top_left: i32,
    // Bottom right location.
    select_bottom_right: i32,
    // Column selection mode.
    block_selection_mode: bool,

    ////// Effective colors and rendition
    effective_foreground: CharacterColor,
    effective_background: CharacterColor,
    effective_rendition: u8,

    saved_state: Box<SavedState>,

    // Last position where we added a character.
    last_pos: i32,

    // Used in repeating char.
    last_drawn_char: u16,

    character_buffer: RefCell<[Character; MAX_CHARS]>,
}
impl ObjectSubclass for Screen {
    const NAME: &'static str = "Screen";

    type Type = Screen;

    type ParentType = Object;
}
impl ObjectImpl for Screen {}

impl Default for Screen {
    fn default() -> Self {
        Self {
            object: Object::default(),
            lines: 0,
            columns: 0,
            screen_lines: Box::default(),
            scrolled_lines: 0,
            last_scolled_region: Rect::new(0, 0, 0, 0),
            dropped_lines: 0,
            line_properties: Box::default(),
            history: Rc::new(HistoryScrollNone::new().wrap()),
            cursor_x: 0,
            cursor_y: 0,
            cursor_foreground: CharacterColor::empty(),
            cursor_background: CharacterColor::empty(),
            cursor_rendition: 0,
            top_margin: 0,
            bottom_margin: 0,
            current_modes: [false; 6],
            saved_modes: [false; 6],
            tab_stops: BitVec::new(),
            select_begin: 0,
            select_top_left: 0,
            select_bottom_right: 0,
            block_selection_mode: false,
            effective_foreground: CharacterColor::empty(),
            effective_background: CharacterColor::empty(),
            effective_rendition: 0,
            saved_state: Box::new(SavedState::new()),
            last_pos: -1,
            last_drawn_char: 0,
            character_buffer: RefCell::new([Character::default(); MAX_CHARS]),
        }
    }
}

impl Screen {
    pub fn new(lines: i32, columns: i32) -> Self {
        let mut screen = Self::default();
        screen.lines = lines;
        screen.columns = columns;
        screen.screen_lines = Box::new(vec![vec![]; lines as usize + 1]);
        screen.line_properties = Box::new(vec![0u8; lines as usize + 1]);

        for i in 0..screen.lines as usize + 1 {
            screen.line_properties[i] = LINE_DEFAULT;
        }

        screen.init_tab_stops();
        screen.clear_selection();
        screen.reset(None);

        screen
    }

    /// Fills the buffer @p dest with @p count instances of the default (ie. blank) Character style.
    pub fn fill_with_default_char(character: &mut [Character], count: i32) {
        for i in 0..count as usize {
            character[i] = Character::default()
        }
    }

    #[inline]
    pub fn loc(&self, x: i32, y: i32) -> i32 {
        y * self.columns + x
    }

    ////////////////// VT100/2, XTerm operations.

    ///////////////////////////////////////////////////////////////////////////////////////////
    ////////////////// Cursor movement.
    ///////////////////////////////////////////////////////////////////////////////////////////

    /// Move the cursor up by @p n lines.
    /// The cursor will stop at the top margin.
    pub fn cursor_up(&mut self, n: i32) {
        let n = if n == 0 { 1 } else { n };
        let stop = if self.cursor_y < self.top_margin {
            0
        } else {
            self.top_margin
        };
        self.cursor_x = (self.columns - 1).min(self.cursor_x);
        self.cursor_y = stop.max(self.cursor_y - n);
    }

    /// Move the cursor down by @p n lines.
    /// The cursor will stop at the bottom margin.
    pub fn cursor_down(&mut self, n: i32) {
        let n = if n == 0 { 1 } else { n };
        let stop = if self.cursor_y > self.bottom_margin {
            self.lines - 1
        } else {
            self.bottom_margin
        };
        self.cursor_x = (self.columns - 1).min(self.cursor_x);
        self.cursor_y = stop.min(self.cursor_y + n);
    }

    /// Move the cursor to the left by @p n columns.
    /// The cursor will stop at the first column.
    pub fn cursor_left(&mut self, n: i32) {
        let n = if n == 0 { 1 } else { n };
        self.cursor_x = (self.columns - 1).min(self.cursor_x);
        self.cursor_x = 0.max(self.cursor_x - n);
    }

    /// Move the cursor to the left by @p n columns.
    /// The cursor will stop at the first column.
    pub fn cursor_right(&mut self, n: i32) {
        let n = if n == 0 { 1 } else { n };
        self.cursor_x = (self.columns - 1).min(self.cursor_x + n);
    }

    /// Moves cursor to beginning of the line by @p n lines down.
    /// The cursor will stop at the beginning of the line.
    pub fn cursor_next_line(&mut self, n: i32) {
        let mut n = if n == 0 { 1 } else { n };
        self.cursor_x = 0;
        while n > 0 {
            if self.cursor_y < self.lines - 1 {
                self.cursor_y += 1;
            }
            n -= 1;
        }
    }

    /// Moves cursor to beginning of the line by @p n lines up.
    /// The cursor will stop at the beginning of the line.
    pub fn cursor_previous_line(&mut self, n: i32) {
        let mut n = if n == 0 { 1 } else { n };
        self.cursor_x = 0;
        while n > 0 {
            if self.cursor_y > 0 {
                self.cursor_y -= 1;
            }
            n -= 1;
        }
    }

    /// Position the cursor on line @p y.
    pub fn set_cursor_y(&mut self, y: i32) {
        let mut y = if y == 0 { 1 } else { y };
        y -= 1;
        self.cursor_y = 0.max((self.lines - 1).min(
            y + if self.get_mode(MODE_ORIGIN) {
                self.top_margin
            } else {
                0
            },
        ));
    }

    /// Position the cursor at column @p x.
    pub fn set_cursor_x(&mut self, x: i32) {
        let mut x = if x == 0 { 1 } else { x };
        x -= 1;
        self.cursor_x = 0.max((self.columns - 1).min(x));
    }

    /// Position the cursor at line @p y, column @p x.
    pub fn set_cursor_yx(&mut self, y: i32, x: i32) {
        self.set_cursor_y(y);
        self.set_cursor_x(x);
    }

    /// Sets the margins for scrolling the screen.
    ///
    /// @param topLine The top line of the new scrolling margin.
    /// @param bottomLine The bottom line of the new scrolling margin.
    pub fn set_margins(&mut self, top_line: i32, bottom_line: i32) {
        let mut top = if top_line == 0 { 1 } else { top_line };
        let mut bottom = if bottom_line == 0 {
            self.lines
        } else {
            bottom_line
        };
        top -= 1;
        bottom -= 1;
        if !(0 <= top && top < bottom && bottom < self.lines) {
            return;
        }

        self.top_margin = top;
        self.bottom_margin = bottom;
        self.cursor_x = 0;
        self.cursor_y = if self.get_mode(MODE_ORIGIN) { top } else { 0 };
    }

    /// Returns the top line of the scrolling region.
    pub fn top_margin(&self) -> i32 {
        self.top_margin
    }

    /// Returns the bottom line of the scrolling region.
    pub fn bottom_margin(&self) -> i32 {
        self.bottom_margin
    }

    /// Resets the scrolling margins back to the top and bottom lines of the screen.
    pub fn set_default_margins(&mut self) {
        self.top_margin = 0;
        self.bottom_margin = self.lines - 1;
    }

    /// Moves the cursor down one line, if the MODE_NewLine mode
    /// flag is enabled then the cursor is returned to the leftmost column first.
    ///
    /// Equivalent to NextLine() if the MODE_NewLine flag is set or index() otherwise.
    pub fn new_line(&mut self) {
        if self.get_mode(MODE_NEWLINE) {
            self.to_start_of_line();
        }
        self.index();
    }

    /// Moves the cursor down one line and positions it at the beginning
    /// of the line.  Equivalent to calling Return() followed by index()
    pub fn next_line(&mut self) {
        self.to_start_of_line();
        self.index();
    }

    /// Moves the cursor down one line, if the MODE_NewLine mode
    /// flag is enabled then the cursor is returned to the leftmost column first.
    ///
    /// Equivalent to NextLine() if the MODE_NewLine flag is set or index() otherwise.
    pub fn index(&mut self) {
        if self.cursor_y == self.bottom_margin {
            self.scroll_up(1);
        } else if self.cursor_y < self.lines - 1 {
            self.cursor_y += 1;
        }
    }

    /// Move the cursor up one line.  If the cursor is on the top line
    /// of the scrolling region (as returned by topMargin()) the scrolling
    /// region is scrolled down by one line instead.
    pub fn reverse_index(&mut self) {
        if self.cursor_y == self.top_margin {
            self.inner_scroll_down(self.top_margin, 1);
        } else if self.cursor_y > 0 {
            self.cursor_y -= 1;
        }
    }

    /// Scroll the scrolling region of the screen up by @p n lines.
    /// The scrolling region is initially the whole screen, but can be changed using setMargins().
    pub fn scroll_up(&mut self, n: i32) {
        let n = if n == 0 { 1 } else { n };
        if self.top_margin == 0 {
            self.add_history_line();
        }
        self.inner_scroll_up(self.top_margin, n);
    }

    /// Scroll the scrolling region of the screen down by @p n lines.
    /// The scrolling region is initially the whole screen, but can be changed using setMargins().
    pub fn scroll_down(&mut self, n: i32) {
        let n = if n == 0 { 1 } else { n };
        self.inner_scroll_down(self.top_margin, n);
    }

    /// Moves the cursor to the beginning of the current line.
    /// Equivalent to setCursorX(0).
    pub fn to_start_of_line(&mut self) {
        self.cursor_x = 0;
    }

    ///  Moves the cursor one column to the left and erases the character at the new cursor position.
    pub fn backspace(&mut self) {
        self.cursor_x = (self.columns - 1).min(self.cursor_x);
        self.cursor_x = 0.max(self.cursor_x - 1);

        if self.screen_lines[self.cursor_y as usize].len() < self.cursor_x as usize + 1 {
            self.screen_lines[self.cursor_y as usize]
                .resize(self.cursor_x as usize, Character::default());
        }

        if BS_CLEARS {
            self.screen_lines[self.cursor_y as usize][self.cursor_x as usize]
                .character_union
                .set_data(wch!(' '))
        }
    }

    /// Moves the cursor @p n tab-stops to the right.
    pub fn tab(&mut self, n: i32) {
        let mut n = if n == 0 { 1 } else { n };
        while n > 0 && self.cursor_x < self.columns - 1 {
            self.cursor_right(1);
            while self.cursor_x < self.columns - 1 && !self.tab_stops[self.cursor_x as usize] {
                self.cursor_right(1)
            }
            n -= 1;
        }
    }

    /// Moves the cursor @p n tab-stops to the left.
    pub fn back_tab(&mut self, n: i32) {
        let mut n = if n == 0 { 1 } else { n };
        while n > 0 && self.cursor_x > 0 {
            self.cursor_left(1);
            while self.cursor_x > 0 && !self.tab_stops[self.cursor_x as usize] {
                self.cursor_left(1);
            }
            n -= 1;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    ////////////////// Editing.
    ///////////////////////////////////////////////////////////////////////////////////////////

    /// Erase @p n characters beginning from the current cursor position.
    /// This is equivalent to over-writing @p n characters starting with the
    /// current cursor position with spaces. If @p n is 0 then one character is erased.
    pub fn erase_chars(&mut self, n: i32) {
        let n = if n == 0 { 1 } else { n };
        let p = 0.max((self.cursor_x + n - 1).min(self.columns - 1));
        self.clear_image(
            self.loc(self.cursor_x, self.cursor_y),
            self.loc(p, self.cursor_y),
            b' ',
        );
    }

    /// Delete @p n characters beginning from the current cursor position.
    /// If @p n is 0 then one character is deleted.
    pub fn delete_chars(&mut self, n: i32) {
        let mut n = if n == 0 { 1 } else { n };

        // if cursor is beyond the end of the line there is nothing to do
        if self.cursor_x >= self.screen_lines[self.cursor_y as usize].len() as i32 {
            return;
        };

        if self.cursor_x + n > self.screen_lines[self.cursor_y as usize].len() as i32 {
            n = self.screen_lines[self.cursor_y as usize].len() as i32 - self.cursor_x;
        }

        assert!(n >= 0);
        assert!(self.cursor_x + n <= self.screen_lines[self.cursor_y as usize].len() as i32);

        for _ in 0..n {
            self.screen_lines[self.cursor_y as usize].remove(self.cursor_x as usize);
        }
    }

    /// Insert @p n blank characters beginning from the current cursor position.
    /// The position of the cursor is not altered.
    /// If @p n is 0 then one character is inserted.
    pub fn insert_chars(&mut self, n: i32) {
        let n = if n == 0 { 1 } else { n };

        if self.screen_lines[self.cursor_y as usize].len() < self.cursor_x as usize {
            self.screen_lines[self.cursor_y as usize]
                .resize(self.cursor_x as usize, Character::default())
        }

        for _ in 0..n {
            self.screen_lines[self.cursor_y as usize]
                .insert(self.cursor_x as usize, Character::default());
        }

        if self.screen_lines[self.cursor_y as usize].len() > self.columns as usize {
            self.screen_lines[self.cursor_y as usize]
                .resize(self.columns as usize, Character::default());
        }
    }

    /// Repeat the preceeding graphic character @count times, including SPACE.
    /// If @count is 0 then the character is repeated once.
    pub fn repeat_chars(&mut self, count: i32) {
        let count = if count == 0 { 1 } else { count };

        // From ECMA-48 version 5, section 8.3.103
        // If the character preceding REP is a control function or part of a
        // control function, the effect of REP is not defined by this Standard.
        //
        // So, a "normal" program should always use REP immediately after a visible
        // character (those other than escape sequences). So, lastDrawnChar can be safely used.
        for _ in 0..count {
            self.display_character(self.last_drawn_char)
        }
    }

    /// Removes @p n lines beginning from the current cursor position.
    /// The position of the cursor is not altered.
    /// If @p n is 0 then one line is removed.
    pub fn delete_lines(&mut self, n: i32) {
        let n = if n == 0 { 1 } else { n };
        self.inner_scroll_up(self.cursor_y, n);
    }

    /// Inserts @p lines beginning from the current cursor position.
    /// The position of the cursor is not altered.
    /// If @p n is 0 then one line is inserted.
    pub fn insert_lines(&mut self, n: i32) {
        let n = if n == 0 { 1 } else { n };
        self.inner_scroll_down(self.cursor_y, n);
    }

    /// Clears all the tab stops.
    pub fn clear_tab_stops(&mut self) {
        for i in 0..self.columns as usize {
            self.tab_stops.set(i, false);
        }
    }

    /// Sets or removes a tab stop at the cursor's current column.
    pub fn change_tab_stop(&mut self, set: bool) {
        if self.cursor_x >= self.columns {
            return;
        }
        self.tab_stops.set(self.cursor_x as usize, set);
    }

    /// Resets (clears) the specified screen @p mode.
    pub fn reset_mode(&mut self, mode: usize) {
        self.current_modes[mode] = false;
        match mode {
            MODE_ORIGIN => {
                self.cursor_x = 0;
                self.cursor_y = 0;
            }
            _ => {}
        }
    }

    /// Sets (enables) the specified screen @p mode.
    pub fn set_mode(&mut self, mode: usize) {
        self.current_modes[mode] = true;
        match mode {
            MODE_ORIGIN => {
                self.cursor_x = 0;
                self.cursor_y = self.top_margin;
            }
            _ => {}
        }
    }

    /// Saves the state of the specified screen @p mode.  It can be restored using restoreMode()
    pub fn save_mode(&mut self, mode: usize) {
        self.saved_modes[mode] = self.current_modes[mode];
    }

    /// Restores the state of a screen @p mode saved by calling saveMode()
    pub fn restore_mode(&mut self, mode: usize) {
        self.current_modes[mode] = self.saved_modes[mode];
    }

    /// Returns whether the specified screen @p mode is enabled or not.
    pub fn get_mode(&self, mode: usize) -> bool {
        self.current_modes[mode]
    }

    /// Saves the current position and appearance (text color and style) of the
    /// cursor. It can be restored by calling restoreCursor()
    pub fn save_cursor(&mut self) {
        self.saved_state.cursor_column = self.cursor_x;
        self.saved_state.cursor_line = self.cursor_y;
        self.saved_state.rendition = self.cursor_rendition;
        self.saved_state.foreground = self.cursor_foreground;
        self.saved_state.background = self.cursor_background;
    }

    /// Restores the position and appearance of the cursor.  
    /// @See saveCursor()
    pub fn resotre_cursor(&mut self) {
        self.cursor_x = self.saved_state.cursor_column.min(self.columns - 1);
        self.cursor_y = self.saved_state.cursor_line.min(self.lines - 1);
        self.cursor_rendition = self.saved_state.rendition;
        self.cursor_foreground = self.saved_state.foreground;
        self.cursor_background = self.saved_state.background;
        self.update_effective_rendition();
    }

    ///  Clear the whole screen, moving the current screen contents into the history first.
    pub fn clear_entire_screen(&mut self) {
        // Add entire screen to history
        for _ in 0..self.lines - 1 {
            self.add_history_line();
            self.inner_scroll_up(0, 1);
        }

        self.clear_image(
            self.loc(0, 0),
            self.loc(self.columns - 1, self.lines - 1),
            b' ',
        );
    }

    /// Clear the area of the screen from the current cursor position to the end of the screen.
    pub fn clear_to_end_of_screen(&mut self) {
        self.clear_image(
            self.loc(self.cursor_x, self.cursor_y),
            self.loc(self.columns - 1, self.lines - 1),
            b' ',
        );
    }

    ///  Clear the area of the screen from the current cursor position to the start of the screen.
    pub fn clear_to_begin_of_screen(&mut self) {
        self.clear_image(self.loc(0, 0), self.loc(self.cursor_x, self.cursor_y), b' ');
    }

    /// Clears the whole of the line on which the cursor is currently positioned.
    pub fn clear_entire_line(&mut self) {
        self.clear_image(
            self.loc(0, self.cursor_y),
            self.loc(self.columns - 1, self.cursor_y),
            b' ',
        );
    }

    /// Clears from the current cursor position to the end of the line.
    pub fn clear_to_end_of_line(&mut self) {
        self.clear_image(
            self.loc(self.cursor_x, self.cursor_y),
            self.loc(self.columns - 1, self.cursor_y),
            b' ',
        );
    }

    /// Clears from the current cursor position to the beginning of the line.
    pub fn clear_to_begin_of_line(&mut self) {
        self.clear_image(
            self.loc(0, self.cursor_y),
            self.loc(self.cursor_x, self.cursor_y),
            b' ',
        );
    }

    /// Fills the entire screen with the letter 'E'.
    pub fn help_align(&mut self) {
        self.clear_image(
            self.loc(0, 0),
            self.loc(self.columns - 1, self.lines - 1),
            b'E',
        );
    }

    /// Enables the given @p rendition flag.  Rendition flags control the
    /// appearance of characters on the screen.
    ///
    /// @see Character::rendition
    pub fn set_rendition(&mut self, rendition: u8) {
        self.cursor_rendition |= rendition;
        self.update_effective_rendition();
    }

    /// Disables the given @p rendition flag.  Rendition flags control the
    /// appearance of characters on the screen.
    ///
    /// @see Character::rendition
    pub fn reset_rendition(&mut self, rendition: u8) {
        self.cursor_rendition &= !rendition;
        self.update_effective_rendition();
    }

    /// Sets the cursor's foreground color.
    /// @param space The color space used by the @p color argument
    /// @param color The new foreground color.  The meaning of this depends on the color @p space used.
    ///
    /// @see CharacterColor
    pub fn set_foreground_color(&mut self, space: u8, color: u32) {
        self.cursor_foreground = CharacterColor::new(space, color);

        if self.cursor_foreground.is_valid() {
            self.update_effective_rendition();
        } else {
            self.set_foreground_color(COLOR_SPACE_DEFAULT, DEFAULT_FORE_COLOR);
        }
    }

    /// Sets the cursor's background color.
    /// @param space The color space used by the @p color argumnet.
    /// @param color The new background color.  The meaning of this depends on the color @p space used.
    ///
    /// @see CharacterColor
    pub fn set_background_color(&mut self, space: u8, color: u32) {
        self.cursor_background = CharacterColor::new(space, color);

        if self.cursor_background.is_valid() {
            self.update_effective_rendition();
        } else {
            self.set_background_color(COLOR_SPACE_DEFAULT, DEFAULT_BACK_COLOR);
        }
    }

    /// Resets the cursor's color back to the default and sets the
    /// character's rendition flags back to the default settings.
    pub fn set_default_rendition(&mut self) {
        self.set_foreground_color(COLOR_SPACE_DEFAULT, DEFAULT_FORE_COLOR);
        self.set_background_color(COLOR_SPACE_DEFAULT, DEFAULT_BACK_COLOR);
        self.cursor_rendition = DEFAULT_RENDITION;
        self.update_effective_rendition();
    }

    /// Returns the column which the cursor is positioned at.
    pub fn get_cursor_x(&self) -> i32 {
        self.cursor_x
    }

    /// Returns the line which the cursor is positioned on.
    pub fn get_cursor_y(&self) -> i32 {
        self.cursor_y
    }

    ///  Clear the entire screen and move the cursor to the home position.
    /// Equivalent to calling clearEntireScreen() followed by home().
    pub fn clear(&mut self) {
        self.clear_entire_screen();
        self.home();
    }

    /// Sets the position of the cursor to the 'home' position at the top-left
    /// corner of the screen (0,0)
    pub fn home(&mut self) {
        self.cursor_x = 0;
        self.cursor_y = 0;
    }

    /// Resets the state of the screen.  This resets the various screen modes
    /// back to their default states.  The cursor style and colors are reset
    /// (as if setDefaultRendition() had been called)
    ///
    /// <ul>
    /// <li>Line wrapping is enabled.</li>
    /// <li>Origin mode is disabled.</li>
    /// <li>Insert mode is disabled.</li>
    /// <li>Cursor mode is enabled.</li>
    /// <li>Screen mode is disabled.</li>
    /// <li>New line mode is disabled.</li>
    /// </ul>
    ///
    /// If @p clearScreen is true then the screen contents are erased entirely,
    /// otherwise they are unaltered.
    pub fn reset(&mut self, clear_screen: Option<bool>) {
        let clear_screen = if clear_screen.is_none() {
            true
        } else {
            clear_screen.unwrap()
        };
        self.set_mode(MODE_WRAP);
        self.save_mode(MODE_WRAP);

        self.reset_mode(MODE_ORIGIN);
        self.save_mode(MODE_ORIGIN);

        self.reset_mode(MODE_INSERT);
        self.save_mode(MODE_INSERT);

        self.set_mode(MODE_CURSOR);
        self.reset_mode(MODE_SCREEN);
        self.reset_mode(MODE_NEWLINE);

        self.top_margin = 0;
        self.bottom_margin = self.lines - 1;

        self.set_default_margins();
        self.save_cursor();

        if clear_screen {
            self.clear();
        }
    }

    /// Displays a new character at the current cursor position.
    ///
    /// If the cursor is currently positioned at the right-edge of the screen and
    /// line wrapping is enabled then the character is added at the start of a new
    /// line below the current one.
    ///
    /// If the MODE_Insert screen mode is currently enabled then the character
    /// is inserted at the current cursor position, otherwise it will replace the
    /// character already at the current cursor position.
    pub fn display_character(&mut self, c: wchar_t) {
        let mut w = wcwidth(c);
        if w <= 0 {
            return;
        }

        if self.cursor_x + w > self.columns {
            if self.get_mode(MODE_WRAP) {
                self.line_properties[self.cursor_y as usize] =
                    self.line_properties[self.cursor_y as usize] | LINE_WRAPPED;
                self.next_line();
            } else {
                self.cursor_x = self.columns - w;
            }
        }

        // ensure current line vector has enough elements.
        let size = self.screen_lines[self.cursor_y as usize].len();
        if size < self.cursor_x as usize + w as usize {
            self.screen_lines[self.cursor_y as usize]
                .resize(self.cursor_x as usize + w as usize, Character::default());
        }

        if self.get_mode(MODE_INSERT) {
            self.insert_chars(w);
        }

        let last_pos = self.loc(self.cursor_x, self.cursor_y);

        // check if selection is still valid.
        self.check_selection(last_pos, last_pos);

        let current_char = &mut self.screen_lines[self.cursor_y as usize][self.cursor_x as usize];

        current_char.character_union.set_data(c);
        current_char.foreground_color = self.effective_foreground;
        current_char.background_color = self.effective_background;
        current_char.rendition = self.effective_rendition;

        self.last_drawn_char = c;

        let mut i = 0;
        let new_cursor_x = self.cursor_x + w;
        w -= 1;
        while w > 0 {
            i += 1;

            if self.screen_lines[self.cursor_y as usize].len() < self.cursor_x as usize + i + 1 {
                self.screen_lines[self.cursor_y as usize]
                    .resize(self.cursor_x as usize + i + 1, Character::default());
            }

            let ch = &mut self.screen_lines[self.cursor_y as usize][self.cursor_x as usize + i];
            ch.character_union.set_data(0);
            ch.foreground_color = self.effective_foreground;
            ch.background_color = self.effective_background;
            ch.rendition = self.effective_rendition;

            w -= 1;
        }
        self.cursor_x = new_cursor_x;
    }

    /// Do composition with last shown character.
    pub fn compose(&mut self, _compose: String) {}

    /// Resizes the image to a new fixed size of @p new_lines by @p new_columns.
    /// In the case that @p new_columns is smaller than the current number of
    /// columns, existing lines are not truncated.  This prevents characters from
    /// being lost if the terminal display is resized smaller and then larger again.
    ///
    /// The top and bottom margins are reset to the top and bottom of the new
    /// screen size.  Tab stops are also reset and the current selection is cleared.
    pub fn resize_image(&mut self, new_lines: i32, new_columns: i32) {
        if new_lines == self.lines && new_columns == self.columns {
            return;
        }

        if self.cursor_y > new_lines - 1 {
            self.bottom_margin = self.lines - 1;
            for _ in 0..self.cursor_y - (new_lines - 1) {
                self.add_history_line();
                self.inner_scroll_up(0, 1);
            }
        }

        let mut new_screen_lines = vec![vec![]; new_lines as usize + 1];
        for i in 0..self.lines.min(new_lines + 1) as usize {
            new_screen_lines[i] = self.screen_lines[i].clone();
        }
        for i in 0..(new_lines + 1) as usize {
            new_screen_lines[i].resize(new_columns as usize, Character::default());
        }

        self.line_properties.resize(new_lines as usize + 1, 0);
        for i in 0..(new_lines + 1) as usize {
            self.line_properties[i] = LINE_DEFAULT;
        }

        self.clear_selection();
        self.screen_lines = Box::new(new_screen_lines);

        self.lines = new_lines;
        self.columns = new_columns;
        self.cursor_x = self.cursor_x.min(self.columns - 1);
        self.cursor_y = self.cursor_y.min(self.lines - 1);

        self.top_margin = 0;
        self.bottom_margin = self.lines - 1;
        self.init_tab_stops();
        self.clear_selection();
    }

    /// Returns the current screen image.
    /// The result is an array of Characters of size [getLines()][getColumns()] which must be freed by the caller after use.
    ///
    /// @param dest Buffer to copy the characters into
    /// @param size Size of @p dest in Characters
    /// @param startLine Index of first line to copy
    /// @param endLine Index of last line to copy
    pub fn get_image(&self, dest: &mut [Character], size: i32, start_line: i32, end_line: i32) {
        assert!(start_line >= 0);
        assert!(end_line >= start_line && end_line < self.history.get_lines() + self.lines);

        let merged_lines = end_line - start_line + 1;

        assert!(size >= merged_lines * self.columns);

        let lines_in_history_buffer = bound(0, self.history.get_lines() - start_line, merged_lines);
        let lines_in_screen_buffer = merged_lines - lines_in_history_buffer;

        // Copy lines from history buffer.
        if lines_in_history_buffer > 0 {
            self.copy_from_history(dest, start_line, lines_in_history_buffer);
        }

        // Copy lines from screen buffer.
        if lines_in_screen_buffer > 0 {
            self.copy_from_screen(
                &mut dest[lines_in_history_buffer as usize * self.columns as usize..],
                start_line + lines_in_history_buffer - self.history.get_lines(),
                lines_in_screen_buffer,
            )
        }

        // Invert display when in screen mode.
        if self.get_mode(MODE_SCREEN) {
            for i in 0..merged_lines as usize * self.columns as usize {
                // For reverse display
                self.reverse_rendition(&mut dest[i]);
            }
        }

        // Mark the character at the current cursor position.
        let cursor_index = self.loc(self.cursor_x, self.cursor_y + lines_in_history_buffer);
        if self.get_mode(MODE_CURSOR) && cursor_index < self.columns * merged_lines {
            dest[cursor_index as usize].rendition |= RE_CURSOR;
        }
    }

    /// Returns the additional attributes associated with lines in the image.
    /// The most important attribute is LINE_WRAPPED which specifies that the line is wrapped,
    /// other attributes control the size of characters in the line.
    pub fn get_line_properties(&self, start_line: i32, end_line: i32) -> Vec<LineProperty> {
        assert!(start_line >= 0);
        assert!(end_line >= start_line && end_line < self.history.get_lines() + self.lines);

        let merged_lines = end_line - start_line + 1;
        let lines_in_history = bound(0, self.history.get_lines() - start_line, merged_lines);
        let lines_in_screen = merged_lines - lines_in_history;

        let mut result = vec![0u8; merged_lines as usize];
        let mut index = 0;

        // Copy properties for lines in history.
        for line in start_line..start_line + lines_in_history {
            if self.history.is_wrapped_line(line) {
                result[index] = result[index] | LINE_WRAPPED;
            }
            index += 1;
        }

        // Copy properties for lines in screen buffer.
        let first_screen_line = start_line + lines_in_history - self.history.get_lines();
        for line in first_screen_line..first_screen_line + lines_in_screen {
            result[index] = self.line_properties[line as usize];
            index += 1;
        }

        result
    }

    /// Return the number of lines.
    pub fn get_lines(&self) -> i32 {
        self.lines
    }

    /// Return the number of columns.
    pub fn get_columns(&self) -> i32 {
        self.columns
    }

    /// Return the number of lines in the history buffer.
    pub fn get_history_lines(&self) -> i32 {
        self.history.get_lines()
    }

    /// Sets the type of storage used to keep lines in the history.
    /// If @p copyPreviousScroll is true then the contents of the previous
    /// history buffer are copied into the new scroll.
    pub fn set_scroll(
        &mut self,
        history_type: Rc<dyn HistoryType>,
        copy_previous_scroll: Option<bool>,
    ) {
        let copy_previous_scroll = if copy_previous_scroll.is_none() {
            true
        } else {
            copy_previous_scroll.unwrap()
        };

        if copy_previous_scroll {
            self.history = history_type.scroll(Some(self.history.clone()));
        } else {
            self.history = history_type.scroll(None);
        }
    }

    /// Returns the type of storage used to keep lines in the history.
    pub fn get_scroll(&self) -> Rc<dyn HistoryType> {
        self.history.get_type()
    }

    /// Returns true if this screen keeps lines that are scrolled off the screen in a history buffer.
    pub fn has_scroll(&self) -> bool {
        self.history.has_scroll()
    }

    /// Sets the start of the selection.
    ///
    /// @param column The column index of the first character in the selection.
    /// @param line The line index of the first character in the selection.
    /// @param blockSelectionMode True if the selection is in column mode.
    pub fn set_selection_start(&mut self, column: i32, line: i32, block_selection_mode: bool) {
        self.select_begin = self.loc(column, line);
        if column == self.columns {
            self.select_begin -= 1;
        }

        self.select_bottom_right = self.select_begin;
        self.select_top_left = self.select_begin;
        self.block_selection_mode = block_selection_mode;
    }

    /// Sets the end of the current selection.
    ///
    /// @param column The column index of the last character in the selection.
    /// @param line The line index of the last character in the selection.
    pub fn set_selection_end(&mut self, column: i32, line: i32) {
        if self.select_begin == -1 {
            return;
        }

        let mut end_pos = self.loc(column, line);

        if end_pos < self.select_begin {
            self.select_top_left = end_pos;
            self.select_bottom_right = self.select_begin;
        } else {
            if column == self.columns {
                end_pos -= 1;
            }

            self.select_top_left = self.select_begin;
            self.select_bottom_right = end_pos;
        }

        // Normalize the selection in column mode.
        if self.block_selection_mode {
            let top_row = self.select_top_left / self.columns;
            let top_column = self.select_top_left % self.columns;
            let bottom_row = self.select_bottom_right / self.columns;
            let bottom_column = self.select_bottom_right % self.columns;

            self.select_top_left = self.loc(top_column.min(bottom_column), top_row);
            self.select_bottom_right = self.loc(top_column.max(bottom_column), bottom_row);
        }
    }

    /// Retrieves the start of the selection or the cursor position if there
    /// is no selection.
    pub fn get_selection_start(&self, column: &mut i32, line: &mut i32) {
        if self.select_top_left != -1 {
            *column = self.select_top_left % self.columns;
            *line = self.select_top_left / self.columns;
        } else {
            *column = self.cursor_x + self.get_history_lines();
            *line = self.cursor_y + self.get_history_lines();
        }
    }

    /// Retrieves the end of the selection or the cursor position if there
    /// is no selection.
    pub fn get_selection_end(&self, column: &mut i32, line: &mut i32) {
        if self.select_bottom_right != -1 {
            *column = self.select_bottom_right % self.columns;
            *line = self.select_bottom_right / self.columns;
        } else {
            *column = self.cursor_x + self.get_history_lines();
            *line = self.cursor_y + self.get_history_lines();
        }
    }

    /// Clears the current selection
    pub fn clear_selection(&mut self) {
        self.select_bottom_right = -1;
        self.select_top_left = -1;
        self.select_begin = -1;
    }

    /// Returns true if the character at (@p column, @p line) is part of the
    /// current selection.
    pub fn is_selected(&self, column: i32, line: i32) -> bool {
        let mut column_in_selection = true;
        if self.block_selection_mode {
            column_in_selection = column >= (self.select_top_left % self.columns)
                && column <= (self.select_bottom_right % self.columns);
        }

        let pos = self.loc(column, line);
        pos >= self.select_top_left && pos <= self.select_bottom_right && column_in_selection
    }

    /// Returns the currently selected text.
    ///
    /// @param preserveLineBreaks Specifies whether new line characters should
    /// be inserted into the returned text at the end of each terminal line.
    pub fn selected_text(&self, preserve_line_breaks: bool) -> String {
        let mut result = String::new();
        let mut stream = TextStream::new(&mut result);

        let mut decoder = PlainTextDecoder::new();
        decoder.begin(&mut stream);
        self.write_selection_to_stream(&mut decoder, preserve_line_breaks);
        decoder.end();

        result
    }

    /// Copies part of the output to a stream.
    ///
    /// @param decoder A decoder which converts terminal characters into text <br>
    /// @param fromLine The first line in the history to retrieve<br>
    /// @param toLine The last line in the history to retrieve
    pub fn write_lines_to_stream(
        &mut self,
        decoder: &mut dyn TerminalCharacterDecoder,
        from_line: i32,
        to_line: i32,
    ) {
        self.write_to_stream(
            decoder,
            self.loc(0, from_line),
            self.loc(self.columns - 1, to_line),
            None,
        );
    }

    /// Copies the selected characters, set using @see setSelBeginXY and @see
    /// setSelExtentXY into a stream.
    ///
    /// @param `decoder` A decoder which converts terminal characters into text.
    ///        PlainTextDecoder is the most commonly used decoder which converts
    ///        characters into plain text with no formatting.<br>
    /// @param `preserveLineBreaks` Specifies whether new line characters should
    ///        be inserted into the returned text at the end of each terminal line.
    pub fn write_selection_to_stream(
        &self,
        decoder: &mut dyn TerminalCharacterDecoder,
        preserve_line_breaks: bool,
    ) {
        if !self.is_selection_valid() {
            return;
        }
        self.write_to_stream(
            decoder,
            self.select_top_left,
            self.select_bottom_right,
            Some(preserve_line_breaks),
        )
    }

    ///  Checks if the text between from and to is inside the current
    /// selection. If this is the case, the selection is cleared. The
    /// from and to are coordinates in the current viewable window.
    /// The loc(x,y) macro can be used to generate these values from a column,line pair.
    ///
    /// @param from The start of the area to check.<br>
    /// @param to The end of the area to check
    pub fn check_selection(&mut self, from: i32, to: i32) {
        if self.select_begin == -1 {
            return;
        }
        let scr_tl = self.loc(0, self.history.get_lines());
        // Clear entire selection if it overlaps region [from, to]
        if self.select_bottom_right >= from + scr_tl && self.select_top_left <= to + scr_tl {
            self.clear()
        }
    }

    /// Sets or clears an attribute of the current line.
    ///
    /// @param property The attribute to set or clear Possible properties are:<br>
    /// LINE_WRAPPED:     Specifies that the line is wrapped.<br>
    /// LINE_DOUBLEWIDTH: Specifies that the characters in the current line
    ///                   should be double the normal width.<br>
    /// LINE_DOUBLEHEIGHT:Specifies that the characters in the current line
    ///                   should be double the normal height.
    ///                   Double-height lines are formed of two lines containing<br>
    /// the same characters, with both having the LINE_DOUBLEHEIGHT attribute. This
    /// allows other parts of the code to work on the assumption that all lines are the same height.
    ///
    /// @param enable true to apply the attribute to the current line or false to remove it
    pub fn set_line_property(&mut self, property: LineProperty, enable: bool) {
        if enable {
            self.line_properties[self.cursor_y as usize] =
                self.line_properties[self.cursor_y as usize] | property;
        } else {
            self.line_properties[self.cursor_y as usize] =
                self.line_properties[self.cursor_y as usize] & !property;
        }
    }

    /// Returns the number of lines that the image has been scrolled up or down by,
    /// since the last call to resetScrolledLines().
    ///
    /// a positive return value indicates that the image has been scrolled up,
    /// a negative return value indicates that the image has been scrolled down.
    pub fn scrolled_lines(&self) -> i32 {
        self.scrolled_lines
    }

    /// Returns the region of the image which was last scrolled.
    ///
    /// This is the area of the image from the top margin to the bottom margin when the last scroll occurred.
    pub fn last_scrolled_region(&self) -> &Rect {
        &self.last_scolled_region
    }

    /// Resets the count of the number of lines that the image has been scrolled up
    /// or down by, see scrolledLines()
    pub fn reset_scrolled_lines(&mut self) {
        self.scrolled_lines = 0
    }

    /// Returns the number of lines of output which have been
    /// dropped from the history since the last call to resetDroppedLines()
    ///
    /// If the history is not unlimited then it will drop
    /// the oldest lines of output if new lines are added when it is full.
    pub fn dropped_lines(&self) -> i32 {
        self.dropped_lines
    }

    /// Resets the count of the number of lines dropped from the history.
    pub fn reset_dropped_lines(&mut self) {
        self.dropped_lines = 0
    }

    /// copies a line of text from the screen or history into a stream using a
    /// specified character decoder.  Returns the number of lines actually copied,
    /// which may be less than 'count' if (start+count) is more than the number of
    /// characters on the line
    ///
    /// line - the line number to copy, from 0 (the earliest line in the history)
    /// up to
    ///         history->getLines() + lines - 1
    ///
    /// start - the first column on the line to copy<br>
    /// count - the number of characters on the line to copy<br>
    /// decoder - a decoder which converts terminal characters (an Character array)
    /// into text appendNewLine - if true a new line character (\n) is appended to the end of the line
    fn copy_line_to_stream(
        &self,
        line: i32,
        start: i32,
        count: i32,
        decoder: &mut dyn TerminalCharacterDecoder,
        append_new_line: bool,
        preserve_line_breaks: bool,
    ) -> i32 {
        let mut count = count;
        let mut start = start;
        assert!(count < MAX_CHARS as i32);

        let mut current_line_properties = 0u8;

        // determine if the line is in the history buffer or the screen image
        if line < self.history.get_lines() {
            let line_length = self.history.get_line_len(line);

            // ensure that start position is before end of line
            start = start.min(0.max(line_length - 1));

            // retrieve line from history buffer.  It is assumed
            // that the history buffer does not store trailing white space
            // at the end of the line, so it does not need to be trimmed here
            count = if count == -1 {
                line_length - start
            } else {
                (start + count).min(line_length) - start
            };

            assert!(start >= 0);
            assert!(count >= 0);
            assert!(start + count <= self.history.get_line_len(line));

            self.history.get_cells(
                line,
                start,
                count,
                self.character_buffer.borrow_mut().as_mut(),
            );

            if self.history.is_wrapped_line(line) {
                current_line_properties |= LINE_WRAPPED;
            }
        } else {
            if count == -1 {
                count = self.columns - start
            }

            assert!(count >= 0);

            let screen_line = line - self.history.get_lines();

            let data = &self.screen_lines[screen_line as usize];
            let length = self.screen_lines[screen_line as usize].len();

            // retrieve line from screen image.
            for i in start as usize..(start as usize + count as usize).min(length) {
                self.character_buffer.borrow_mut()[i - start as usize] = data[i];
            }

            // count cannot be any greater than length
            count = bound(0, count, (length as i32 - start).max(0));

            assert!(screen_line < self.line_properties.len() as i32);
            current_line_properties |= self.line_properties[screen_line as usize];
        }

        // Add new line character at end.
        let omit_line_break = (current_line_properties & LINE_WRAPPED > 0) || !preserve_line_breaks;

        if !omit_line_break && append_new_line && (count + 1 < MAX_CHARS as i32) {
            self.character_buffer.borrow_mut()[count as usize]
                .character_union
                .set_data(wch!('\n'));
            count += 1;
        }

        // Decode line and write to text stream.
        decoder.decode_line(
            self.character_buffer.borrow().as_ref(),
            count,
            current_line_properties,
        );

        count
    }

    /// fills a section of the screen image with the character 'c'
    /// the parameters are specified as offsets from the start of the screen image.
    /// the loc(x,y) macro can be used to generate these values from a column,line pair.
    fn clear_image(&mut self, loca: i32, loce: i32, c: u8) {
        let scr_tl = self.loc(0, self.history.get_lines());

        // Clear entire selection if it overlaps region to be moved...
        if self.select_bottom_right > loca + scr_tl && self.select_top_left < loce + scr_tl {
            self.clear_selection()
        }

        let top_line = loca / self.columns;
        let bottom_line = loce / self.columns;

        let clear_ch = Character::new(
            c as u16,
            self.cursor_foreground,
            self.cursor_background,
            DEFAULT_RENDITION,
        );

        // if the character being used to clear the area is the same as the
        // default character, the affected lines can simply be shrunk.
        let is_default_ch = clear_ch == Character::default();

        for y in top_line..bottom_line {
            self.line_properties[y as usize] = 0;

            let end_col = if y == bottom_line {
                loce % self.columns
            } else {
                self.columns - 1
            };
            let start_col = if y == top_line {
                loca % self.columns
            } else {
                0
            };

            let line = &mut self.screen_lines[y as usize];

            if is_default_ch && end_col == self.columns - 1 {
                line.resize(start_col as usize, Character::default());
            } else {
                if line.len() < end_col as usize + 1 {
                    line.resize(end_col as usize + 1, clear_ch);
                }

                for i in start_col..end_col {
                    line[i as usize] = clear_ch;
                }
            }
        }
    }

    /// move screen image between 'sourceBegin' and 'sourceEnd' to 'dest'.
    /// the parameters are specified as offsets from the start of the screen image.
    /// the loc(x,y) macro can be used to generate these values from a column,line pair.
    ///
    /// NOTE: moveImage() can only move whole lines
    fn move_image(&mut self, dest: usize, source_begin: usize, source_end: usize) {
        assert!(source_begin <= source_end);

        let lines = (source_end - source_begin) / self.columns as usize;

        // move screen image and line properties:
        // the source and destination areas of the image may overlap,
        // so it matters that we do the copy in the right order - forwards if dest < sourceBegin or backwards otherwise.
        //(search the web for 'memmove implementation' for details)
        if dest < source_begin {
            for i in 0..lines {
                self.screen_lines[(dest / self.columns as usize) + i] =
                    self.screen_lines[(source_begin / self.columns as usize) + i].clone();
                self.line_properties[(dest / self.columns as usize) + i] =
                    self.line_properties[(source_begin / self.columns as usize) + i];
            }
        } else {
            let mut i = lines;
            loop {
                if i > lines {
                    break;
                }
                self.screen_lines[(dest / self.columns as usize) + i] =
                    self.screen_lines[(source_begin / self.columns as usize) + i].clone();
                self.line_properties[(dest / self.columns as usize) + i] =
                    self.line_properties[(source_begin / self.columns as usize) + i];
                i -= 1;
            }
        }

        if self.last_pos != -1 {
            let diff = dest - source_begin;
            self.last_pos += diff as i32;
            if self.last_pos < 0 || self.last_pos >= (lines as i32 * self.columns) {
                self.last_pos = -1;
            }
        }

        // Adjust selection to follow scroll.
        if self.select_begin != -1 {
            let begin_is_tl = self.select_begin == self.select_top_left;
            let diff = (dest - source_begin) as i32;
            let scr_tl = self.loc(0, self.history.get_lines());
            let srca = source_begin as i32 + scr_tl;
            let srce = source_end as i32 + scr_tl;
            let desta = srca + diff;
            let deste = srce + diff;

            if self.select_top_left >= srca && self.select_top_left <= srce {
                self.select_top_left += diff;
            } else if self.select_top_left >= desta && self.select_top_left <= deste {
                // TODO: Check that.
                self.select_bottom_right = -1;
            }

            if self.select_bottom_right >= srca && self.select_bottom_right <= srce {
                self.select_bottom_right += diff;
            } else if self.select_bottom_right >= desta && self.select_bottom_right <= deste {
                self.select_bottom_right = -1;
            }

            if self.select_bottom_right < 0 {
                self.clear_selection();
            } else {
                if self.select_top_left < 0 {
                    self.select_top_left = 0;
                }
            }

            if begin_is_tl {
                self.select_begin = self.select_top_left;
            } else {
                self.select_begin = self.select_bottom_right;
            }
        }
    }

    /// scroll up 'n' lines in current region, clearing the bottom 'n' lines.
    fn inner_scroll_up(&mut self, from: i32, n: i32) {
        let mut n = n;
        if n <= 0 {
            return;
        }
        if from > self.bottom_margin {
            return;
        }
        if from + n > self.bottom_margin {
            n = self.bottom_margin + 1 - from;
        }

        self.scrolled_lines -= n;
        self.last_scolled_region = Rect::new(
            0,
            self.top_margin,
            self.columns - 1,
            self.bottom_margin - self.top_margin,
        );

        self.move_image(
            self.loc(0, from) as usize,
            self.loc(0, from + n) as usize,
            self.loc(self.columns, self.bottom_margin) as usize,
        );
        self.clear_image(
            self.loc(0, self.bottom_margin - n + 1),
            self.loc(self.columns - 1, self.bottom_margin),
            b' ',
        );
    }

    /// scroll down 'n' lines in current region, clearing the top 'n' lines.
    fn inner_scroll_down(&mut self, from: i32, n: i32) {
        let mut n = n;

        if n <= 0 {
            return;
        }
        if from > self.bottom_margin {
            return;
        }
        if from + n > self.bottom_margin {
            n = self.bottom_margin - from;
        }

        self.move_image(
            self.loc(0, from + n) as usize,
            self.loc(0, from) as usize,
            self.loc(self.columns - 1, self.bottom_margin - n) as usize,
        );
        self.clear_image(
            self.loc(0, from),
            self.loc(self.columns - 1, from + n - 1),
            b' ',
        );
    }

    /// Add line to history buffer
    fn add_history_line(&mut self) {
        if self.has_scroll() {
            let old_history_lines = self.history.get_lines();

            self.history.add_cells_list(self.screen_lines[0].clone());
            self.history
                .add_line(self.line_properties[0] & LINE_WRAPPED > 0);

            let new_history_lines = self.history.get_lines();
            let begin_is_tl = self.select_begin == self.select_top_left;

            // If the history is full, increment the count of dropped lines
            if new_history_lines == old_history_lines {
                self.dropped_lines += 1;
            }

            // Adjust selection for the new point of reference
            if new_history_lines > old_history_lines {
                if self.select_begin != -1 {
                    self.select_top_left += self.columns;
                    self.select_bottom_right += self.columns;
                }
            }

            if self.select_begin != -1 {
                // Scroll selection in history up.
                let top_br = self.loc(0, 1 + new_history_lines);

                if self.select_top_left < top_br {
                    self.select_top_left -= self.columns;
                }

                if self.select_bottom_right < top_br {
                    self.select_bottom_right -= self.columns;
                }

                if self.select_bottom_right < 0 {
                    self.clear_selection();
                } else if self.select_top_left < 0 {
                    self.select_top_left = 0;
                }

                if begin_is_tl {
                    self.select_begin = self.select_top_left;
                } else {
                    self.select_begin = self.select_bottom_right;
                }
            }
        }
    }

    fn init_tab_stops(&mut self) {
        self.tab_stops.resize(self.columns as usize, false);
        for i in 0..self.columns as usize {
            self.tab_stops.set(i, i % 8 == 0 && i != 0);
        }
    }

    fn update_effective_rendition(&mut self) {
        self.effective_rendition = self.cursor_rendition;
        if self.cursor_rendition & RE_REVERSE > 0 {
            self.effective_foreground = self.cursor_background;
            self.effective_background = self.cursor_foreground;
        } else {
            self.effective_foreground = self.cursor_foreground;
            self.effective_background = self.cursor_background;
        }

        if self.cursor_rendition & RE_BOLD > 0 {
            self.effective_foreground.set_intensive();
        }
    }

    /// Clarifying rendition here and in the display.
    ///
    /// currently, the display's color table is <br>
    /// 0       1       2 .. 9    10 .. 17 <br>
    /// dft_fg, dft_bg, dim 0..7, intensive 0..7
    ///
    /// currentForeground, currentBackground contain values 0..8; <br>
    /// - 0    = default color <br>
    /// - 1..8 = ansi specified color
    ///
    /// re_fg, re_bg contain values 0..17 <br>
    /// due to the TerminalDisplay's color table
    ///
    ///rendition attributes are
    ///
    /// attr           widget screen <br>
    /// -------------- ------ ------ <br>
    /// RE_UNDERLINE     XX     XX    affects foreground only <br>
    /// RE_BLINK         XX     XX    affects foreground only <br>
    /// RE_BOLD          XX     XX    affects foreground only <br>
    /// RE_REVERSE       --     XX                            <br>
    /// RE_TRANSPARENT   XX     --    affects background only <br>
    /// RE_INTENSIVE     XX     --    affects foreground only
    ///
    /// Note that RE_BOLD is used in both widget and screen rendition. Since xterm/vt102
    /// is to poor to distinguish between bold (which is a font attribute) and intensive
    /// (which is a color attribute), we translate this and RE_BOLD in falls eventually appart
    /// into RE_BOLD and RE_INTENSIVE.
    fn reverse_rendition(&self, p: &mut Character) {
        // TODO: Make selection background to fixed??
        let f = p.foreground_color;
        let b = p.background_color;

        p.foreground_color = b;
        p.background_color = f;
    }

    fn is_selection_valid(&self) -> bool {
        self.select_top_left >= 0 && self.select_bottom_right >= 0
    }

    /// copies text from 'startIndex' to 'endIndex' to a stream
    /// startIndex and endIndex are positions generated using the loc(x,y) macro
    fn write_to_stream(
        &self,
        decoder: &mut dyn TerminalCharacterDecoder,
        start_index: i32,
        end_index: i32,
        preserve_line_breaks: Option<bool>,
    ) {
        let preserve_line_breaks = if preserve_line_breaks.is_none() {
            true
        } else {
            preserve_line_breaks.unwrap()
        };
        let top = start_index / self.columns;
        let left = start_index % self.columns;

        let bottom = end_index / self.columns;
        let right = end_index % self.columns;

        assert!(top >= 0 && left >= 0 && bottom >= 0 && right >= 0);

        for y in top..bottom {
            let mut start = 0;
            if y == top || self.block_selection_mode {
                start = left;
            }

            let mut count = -1;
            if y == bottom || self.block_selection_mode {
                count = right - start + 1;
            }

            let append_new_line = y != bottom;
            let copied = self.copy_line_to_stream(
                y,
                start,
                count,
                decoder,
                append_new_line,
                preserve_line_breaks,
            );

            if y == bottom && copied < count {
                let new_line_char = Character::new(
                    wch!('\n'),
                    CharacterColor::default_foreground(),
                    CharacterColor::default_background(),
                    DEFAULT_RENDITION,
                );
                decoder.decode_line(&[new_line_char], 1, 0);
            }
        }
    }

    /// copies 'count' lines from the screen buffer into 'dest',
    // starting from 'startLine', where 0 is the first line in the screen buffer
    fn copy_from_screen(&self, dest: &mut [Character], start_line: i32, count: i32) {
        assert!(start_line >= 0 && count > 0 && start_line + count <= self.lines);

        for line in start_line..start_line + count {
            let src_line_start_index = line * self.columns;
            let dest_line_start_index = (line - start_line) * self.columns;

            for column in 0..self.columns {
                let src_index = (src_line_start_index + column) as usize;
                let dest_index = (dest_line_start_index + column) as usize;

                dest[dest_index as usize] = self.screen_lines[src_index / self.columns as usize]
                    [src_index % self.columns as usize];

                // Invert selected text
                if self.select_begin != -1
                    && self.is_selected(column, line + self.history.get_lines())
                {
                    self.reverse_rendition(&mut dest[dest_index]);
                }
            }
        }
    }

    /// copies 'count' lines from the history buffer into 'dest',
    /// starting from 'startLine', where 0 is the first line in the history
    fn copy_from_history(&self, dest: &mut [Character], start_line: i32, count: i32) {
        assert!(start_line >= 0 && count > 0 && start_line + count <= self.history.get_lines());

        for line in start_line..start_line + count {
            let length = self.columns.min(self.history.get_line_len(line));
            let dest_line_offset = (line - start_line) * self.columns;

            self.history
                .get_cells(line, 0, length, &mut dest[dest_line_offset as usize..]);

            for column in length..self.columns {
                dest[(dest_line_offset + column) as usize] = Character::default();
            }

            // Invert selected tex.
            if self.select_begin != -1 {
                for column in 0..self.columns {
                    if self.is_selected(column, line) {
                        self.reverse_rendition(&mut dest[(dest_line_offset + column) as usize]);
                    }
                }
            }
        }
    }
}
