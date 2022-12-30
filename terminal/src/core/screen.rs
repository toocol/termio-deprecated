#![allow(dead_code)]
use crate::tools::{
    character::{Character, LineProperty},
    character_color::CharacterColor,
    history::{HistoryScroll, HistoryScrollNone, HistoryScrollWrapper},
};
use bitvec::vec::BitVec;
use tmui::graphics::figure::Rect;

const MODE_ORIGIN: usize = 0;
const MODE_WRAP: usize = 1;
const MODE_INSERT: usize = 2;
const MODE_SCREEN: usize = 3;
const MODE_CURSOR: usize = 4;
const MODE_NEWLINE: usize = 5;

const MODES_SCREEN: usize = 6;

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

/// The terminal emulation ( Emulation ) receives a serial stream of
/// characters from the program currently running in the terminal.
/// From this stream it creates an image of characters which is ultimately
/// rendered by the display widget ( TerminalDisplay ).  Some types of emulation
/// may have more than one screen image.
///
/// getImage() is used to retrieve the currently visible image
/// which is then used by the display widget to draw the output from the terminal.
///
/// The number of lines of output history which are kept in addition to the
/// current screen image depends on the history scroll being used to store the
/// output. The scroll is specified using setScroll() The output history can be retrieved using writeToStream()
///
/// The screen image has a selection associated with it, specified using
/// setSelectionStart() and setSelectionEnd().  The selected text can be
/// retrieved using selectedText().  When getImage() is used to retrieve the
/// visible image, characters which are part of the selection have their colours inverted.
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
    history: Box<dyn HistoryScrollWrapper>,

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
}

impl Screen {
    pub fn new(lines: i32, columns: i32) -> Self {
        Self {
            lines: lines,
            columns: columns,
            screen_lines: Box::new(vec![vec![]; lines as usize + 1]),
            scrolled_lines: 0,
            last_scolled_region: Rect::new(0, 0, 0, 0),
            dropped_lines: 0,
            line_properties: Box::new(vec![0u8; lines as usize + 1]),
            history: HistoryScrollNone::new().wrap(),
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
        }
    }

    ////////////////// VT100/2, XTerm operations.
    ////////////////// Cursor movement.

    /// Move the cursor up by @p n lines.
    /// The cursor will stop at the top margin.
    pub fn cursor_up(&mut self, n: i32) {
        todo!()
    }

    /// Move the cursor down by @p n lines.
    /// The cursor will stop at the bottom margin.
    pub fn cursor_down(&mut self, n: i32) {
        todo!()
    }

    /// Move the cursor to the left by @p n columns.
    /// The cursor will stop at the first column.
    pub fn cursor_left(&mut self, n: i32) {
        todo!()
    }

    /// Move the cursor to the left by @p n columns.
    /// The cursor will stop at the first column.
    pub fn cursor_right(&mut self, n: i32) {
        todo!()
    }

    /// Moves cursor to beginning of the line by @p n lines down.
    /// The cursor will stop at the beginning of the line.
    pub fn cursor_next_line(&mut self, n: i32) {
        todo!()
    }

    /// Moves cursor to beginning of the line by @p n lines up.
    /// The cursor will stop at the beginning of the line.
    pub fn cursor_previous_line(&mut self, n: i32) {
        todo!()
    }

    /// Position the cursor on line @p y.
    pub fn set_cursor_y(&mut self, y: i32) {
        todo!()
    }

    /// Position the cursor at column @p x.
    pub fn set_cursor_x(&mut self, x: i32) {
        todo!()
    }

    /// Position the cursor at line @p y, column @p x.
    pub fn set_cursor_yx(&mut self, y: i32, x: i32) {
        todo!()
    }

    /// Sets the margins for scrolling the screen.
    ///
    /// @param topLine The top line of the new scrolling margin.
    /// @param bottomLine The bottom line of the new scrolling margin.
    pub fn set_margins(&mut self, top_line: i32, bottom_line: i32) {
        todo!()
    }

    /// Returns the top line of the scrolling region.
    pub fn top_margin(&self) -> i32 {
        todo!()
    }

    /// Returns the bottom line of the scrolling region.
    pub fn bottom_margin(&self) -> i32 {
        todo!()
    }

    /// Resets the scrolling margins back to the top and bottom lines of the screen.
    pub fn set_default_margins(&mut self) {
        todo!()
    }

    /// Moves the cursor down one line, if the MODE_NewLine mode
    /// flag is enabled then the cursor is returned to the leftmost column first.
    ///
    /// Equivalent to NextLine() if the MODE_NewLine flag is set or index() otherwise.
    pub fn new_line(&mut self) {
        todo!()
    }

    /// Moves the cursor down one line and positions it at the beginning
    /// of the line.  Equivalent to calling Return() followed by index()
    pub fn next_line(&mut self) {
        todo!()
    }

    /// Moves the cursor down one line, if the MODE_NewLine mode
    /// flag is enabled then the cursor is returned to the leftmost column first.
    ///
    /// Equivalent to NextLine() if the MODE_NewLine flag is set or index() otherwise.
    pub fn index(&mut self) {
        todo!()
    }

    /// Move the cursor up one line.  If the cursor is on the top line
    /// of the scrolling region (as returned by topMargin()) the scrolling
    /// region is scrolled down by one line instead.
    pub fn reverse_index(&mut self) {
        todo!()
    }

    /// Scroll the scrolling region of the screen up by @p n lines.
    /// The scrolling region is initially the whole screen, but can be changed using setMargins().
    pub fn scroll_up(&mut self, n: i32) {
        todo!()
    }

    /// Scroll the scrolling region of the screen down by @p n lines.
    /// The scrolling region is initially the whole screen, but can be changed using setMargins().
    pub fn scroll_down(&mut self, n: i32) {
        todo!()
    }

    /// Moves the cursor to the beginning of the current line.
    /// Equivalent to setCursorX(0).
    pub fn to_start_of_line(&mut self) {
        todo!()
    }

    ///  Moves the cursor one column to the left and erases the character at the new cursor position.
    pub fn backspace(&mut self) {
        todo!()
    }

    /// Moves the cursor @p n tab-stops to the right.
    pub fn tab(&mut self, n: i32) {
        todo!()
    }

    /// Moves the cursor @p n tab-stops to the left.
    pub fn back_tab(&mut self, n: i32) {
        todo!()
    }

    ////////////////// Editing.
}
