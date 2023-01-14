#![allow(dead_code)]
use super::screen_window::ScreenWindow;
use crate::tools::{
    character::{Character, LineProperty},
    character_color::{ColorEntry, TABLE_COLORS},
    filter::{FilterChainImpl, TerminalImageFilterChain},
};
use std::{
    ptr::NonNull,
    sync::atomic::{AtomicBool, AtomicI32, Ordering},
};
use tmui::{
    graphics::{
        figure::{Color, Size},
        painter::Painter,
    },
    prelude::*,
    tlib::{
        object::{ObjectImpl, ObjectSubclass},
        signals,
    },
    widget::WidgetImpl,
    Font,
};
use LineEncode::*;

#[extends_widget]
#[derive(Default)]
pub struct TerminalView {
    screen_window: Option<NonNull<ScreenWindow>>,

    allow_bell: bool,
    // Whether intense colors should be bold.
    bold_intense: bool,
    // Whether is test mode.
    draw_text_test_flag: bool,

    // whether has fixed pitch.
    fixed_font: bool,
    font_height: i32,
    font_width: i32,
    font_ascend: i32,
    draw_text_addition_height: i32,

    left_margin: i32,
    top_margin: i32,
    left_base_margin: i32,
    top_base_margin: i32,

    // The total number of lines that can be displayed in the view.
    lines: i32,
    // The total number of columns that can be displayed in the view.
    columns: i32,

    image: Vec<Character>,
    image_size: i32,

    line_properties: Vec<LineProperty>,

    color_table: [ColorEntry; TABLE_COLORS],
    random_seed: u32,

    resizing: bool,
    terminal_size_hint: bool,
    terminal_size_start_up: bool,
    bidi_enable: bool,
    mouse_marks: bool,
    bracketed_paste_mode: bool,
    disable_bracketed_paste_mode: bool,

    // initial selection point.
    i_pnt_sel: Point,
    // current selection point.
    pnt_sel: Point,
    //  help avoid flicker.
    triple_sel_begin: Point,
    // selection state
    act_sel: i32,
    word_selection_mode: bool,
    line_selection_mode: bool,
    preserve_line_breaks: bool,
    column_selection_mode: bool,

    // TODO: Add clipboard.
    // TODO: Add ScrollBar.
    scroll_bar_location: ScrollBarPosition,
    word_characters: String,
    bell_mode: i32,

    // hide text in paint event.
    blinking: bool,
    // has character to blink.
    has_blinker: bool,
    // hide cursor in paint event.
    cursor_blinking: bool,
    // has bliking cursor enable.
    has_blinking_cursor: bool,
    // allow text to blink.
    allow_blinking_text: bool,
    // require Ctrl key for drag.
    ctrl_drag: bool,
    // columns/lines are locked.
    is_fixed_size: bool,
    // set in mouseDoubleClickEvent and delete after double_click_interval() delay.
    possible_triple_click: bool,
    triple_click_mode: TripleClickMode,
    // TODO: add blink_timer
    // TODO: add blink_cursor_timer

    // true during visual bell.
    colors_inverted: bool,

    // TODO: add resize label
    // TODO: add resize Timer

    // TODO: add output_suspend_label
    line_spacing: u32,
    opacity: f32,
    size: Size,

    // Add background_image Pixmap
    background_mode: BackgroundMode,

    filter_chain: Box<TerminalImageFilterChain>,
    mouse_over_hotspot_area: Rect,

    cursor_shape: KeyboardCursorShape,
    cursor_color: Color,

    input_method_data: InputMethodData,

    draw_line_chars: bool,
}

#[derive(Default)]
struct InputMethodData {
    preedit_string: String,
    previous_preedit_rect: Rect,
}

#[derive(Default)]
struct DragInfo {
    state: DragState,
    start: Point,
    // TODO: add `drag object`
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////
/// Widget Implements
//////////////////////////////////////////////////////////////////////////////////////////////////////////
impl ObjectSubclass for TerminalView {
    const NAME: &'static str = "TerminalView";

    type Type = TerminalView;

    type ParentType = Widget;
}

impl ObjectImpl for TerminalView {}

impl WidgetImpl for TerminalView {
    fn paint(&mut self, mut painter: Painter) {
        painter.set_antialiasing();
    }

    fn size_hint(&mut self, size: Size) {}
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////
/// TerminalView Singals
//////////////////////////////////////////////////////////////////////////////////////////////////////////
pub trait TerminalViewSingals: ActionExt {
    signals!(
       /// Emitted when the user presses a key whilst the terminal widget has focus.
       key_pressed_signal();

       /// A mouse event occurred.
       /// @param [`button`] The mouse button (0 for left button, 1 for middle button, 2
       /// for right button, 3 for release) <br>
       /// @param [`column`] The character column where the event occurred <br>
       /// @param [`line`] The character row where the event occurred <br>
       /// @param [`event_type`] The type of event.  0 for a mouse press / release or 1 for
       /// mouse motion
       mouse_signal();

       changed_font_metrics_signal();
       changed_content_size_signal();

       /// Emitted when the user right clicks on the display, or right-clicks with the
       /// Shift key held down if [`uses_mouse()`] is true.
       ///
       /// This can be used to display a context menu.
       configure_request();

       /// When a shortcut which is also a valid terminal key sequence is pressed
       /// while the terminal widget  has focus, this signal is emitted to allow the
       /// host to decide whether the shortcut should be overridden. When the shortcut
       /// is overridden, the key sequence will be sent to the terminal emulation
       /// instead and the action associated with the shortcut will not be triggered.
       ///
       /// @p [`override`] is set to false by default and the shortcut will be triggered
       /// as normal.
       override_shortcut_check();

       is_busy_selecting();
       send_string_to_emu();

       copy_avaliable();
       term_get_focus();
       term_lost_focus();

       notify_bell();
       uses_mouse_changed();
    );
}
impl TerminalViewSingals for TerminalView {}

//////////////////////////////////////////////////////////////////////////////////////////////////////////
/// TerminalView Implements
//////////////////////////////////////////////////////////////////////////////////////////////////////////
impl TerminalView {
    /// Specified whether anti-aliasing of text in the terminal view
    /// is enabled or not.  Defaults to enabled.
    pub fn set_antialiasing(antialias: bool) {
        ANTIALIAS_TEXT.store(antialias, Ordering::SeqCst)
    }
    /// Returns true if anti-aliasing of text in the terminal is enabled.
    pub fn antialias() -> bool {
        ANTIALIAS_TEXT.load(Ordering::SeqCst)
    }

    #[inline]
    pub fn loc(&self, x: i32, y: i32) -> i32 {
        y * self.columns + x
    }

    /// Returns the terminal color palette used by the view.
    pub fn get_color_table(&self) -> &[ColorEntry] {
        todo!()
    }
    /// Sets the terminal color palette used by the view.
    pub fn set_color_table(&mut self, table: &[ColorEntry]) {
        todo!()
    }

    /// Sets the seed used to generate random colors for the view
    /// (in color schemes that support them).
    pub fn set_random_seed(&mut self, seed: u32) {
        todo!()
    }
    /// Returns the seed used to generate random colors for the view
    /// (in color schemes that support them).
    pub fn random_seed(&self) -> u32 {
        todo!()
    }

    /// Sets the opacity of the terminal view.
    pub fn set_opacity(&mut self, opacity: f32) {
        todo!()
    }

    /// Sets the background image of the terminal view.
    pub fn set_background_image(&mut self, image: &str) {
        todo!()
    }
    /// Sets the background image mode of the terminal view.
    pub fn set_background_mode(&mut self, mode: BackgroundMode) {
        todo!()
    }

    /// Specifies whether the terminal display has a vertical scroll bar, and if so
    /// whether it is shown on the left or right side of the view.
    pub fn set_scroll_bar_position(&mut self, position: ScrollBarPosition) {
        todo!()
    }
    /// Setting the current position and range of the display scroll bar.
    pub fn set_scroll(&mut self, cursor: i32, lines: i32) {
        todo!()
    }
    /// Scroll to the bottom of the terminal (reset scrolling).
    pub fn scroll_to_end(&mut self) {
        todo!()
    }

    /// Returns the display's filter chain.  When the image for the display is
    /// updated, the text is passed through each filter in the chain.  Each filter
    /// can define hotspots which correspond to certain strings (such as URLs or
    /// particular words). Depending on the type of the hotspots created by the
    /// filter ( returned by Filter::Hotspot::type() ) the view will draw visual
    /// cues such as underlines on mouse-over for links or translucent rectangles
    /// for markers.
    ///
    /// To add a new filter to the view, call:
    ///      view->filter_chain()->add_filter( filterObject );
    pub fn filter_chain(&self) -> &Box<dyn FilterChainImpl> {
        todo!()
    }

    /// Updates the filters in the display's filter chain.  This will cause
    /// the hotspots to be updated to match the current image.
    ///
    /// TODO: This function can be expensive depending on the
    /// image size and number of filters in the filterChain()
    pub fn process_filters(&self) {
        todo!()
    }

    /// Returns a list of menu actions created by the filters for the content
    /// at the given @p position.
    pub fn filter_actions(&self, position: Point) -> Vec<Action> {
        todo!()
    }

    /// Returns true if the cursor is set to blink or false otherwise.
    #[inline]
    pub fn blinking_cursor(&self) -> bool {
        self.has_blinking_cursor
    }

    /// Specifies whether or not the cursor blinks.
    #[inline]
    pub fn set_blinking_cursor(&mut self, blink: bool) {
        self.has_blinking_cursor = blink;
    }

    /// Specifies whether or not text can blink.
    #[inline]
    pub fn set_blinking_text_enable(&mut self, blink: bool) {
        self.allow_blinking_text = blink
    }

    #[inline]
    pub fn set_ctrl_drag(&mut self, enable: bool) {
        self.ctrl_drag = enable
    }
    #[inline]
    pub fn ctrl_drag(&self) -> bool {
        self.ctrl_drag
    }

    /// Sets how the text is selected when the user triple clicks within the view.
    #[inline]
    pub fn set_triple_click_mode(&mut self, mode: TripleClickMode) {
        self.triple_click_mode = mode
    }
    #[inline]
    pub fn get_triple_click_mode(&self) -> TripleClickMode {
        self.triple_click_mode
    }

    pub fn set_line_spacing(&mut self, spacing: u32) {
        todo!()
    }
    pub fn line_spacing(&self) -> u32 {
        todo!()
    }

    pub fn set_margin(&mut self, margin: i32) {
        todo!()
    }
    pub fn margin(&mut self) -> i32 {
        todo!()
    }

    pub fn emit_selection(&self, use_x_selection: bool, append_return: bool) {
        todo!()
    }

    /// change and wrap text corresponding to paste mode.
    pub fn bracket_text(&self, text: &str) {}

    /// Sets the shape of the keyboard cursor. This is the cursor drawn
    /// at the position in the terminal where keyboard input will appear.
    ///
    /// In addition the terminal display widget also has a cursor for
    /// the mouse pointer, which can be set using the QWidget::setCursor() method.
    ///
    /// Defaults to BlockCursor
    pub fn set_keyboard_cursor_shape(&mut self, shape: KeyboardCursorShape) {
        todo!()
    }
    /// Returns the shape of the keyboard cursor.
    pub fn keyboard_cursor_shape(&self) -> KeyboardCursorShape {
        todo!()
    }
    /// Sets the color used to draw the keyboard cursor.
    ///
    /// The keyboard cursor defaults to using the foreground color of the character
    /// underneath it.
    ///
    /// @param [`use_foreground_color`] If true, the cursor color will change to match
    /// the foreground color of the character underneath it as it is moved, in this
    /// case, the @p color parameter is ignored and the color of the character
    /// under the cursor is inverted to ensure that it is still readable.
    ///
    /// @param [`color`] The color to use to draw the cursor.  This is only taken into
    /// account if @p [`use_foreground_color`] is false.
    pub fn set_keyboard_cursor_color(&self, use_foreground_color: bool, color: Color) {
        todo!()
    }
    /// Returns the color of the keyboard cursor, or an invalid color if the
    /// keyboard cursor color is set to change according to the foreground color of
    /// the character underneath it.
    pub fn keyboard_cursor_color(&self) -> Color {
        todo!()
    }

    /// Returns the number of lines of text which can be displayed in the widget.
    ///
    /// This will depend upon the height of the widget and the current font.
    /// See [`font_height()`]
    #[inline]
    pub fn lines(&self) -> i32 {
        self.lines
    }
    /// Returns the number of characters of text which can be displayed on
    /// each line in the widget.
    ///
    /// This will depend upon the width of the widget and the current font.
    /// See [`font_width()`]
    #[inline]
    pub fn columns(&self) -> i32 {
        self.columns
    }

    /// Returns the height of the characters in the font used to draw the text in
    /// the view.
    #[inline]
    pub fn font_height(&self) -> i32 {
        self.font_height
    }
    /// Returns the width of the characters in the view.
    /// This assumes the use of a fixed-width font.
    #[inline]
    pub fn font_width(&self) -> i32 {
        self.font_width
    }

    pub fn set_size(&mut self, cols: i32, lins: i32) {
        todo!()
    }
    pub fn set_fixed_size(&mut self, cols: i32, lins: i32) {
        todo!()
    }

    /// Sets which characters, in addition to letters and numbers,
    /// are regarded as being part of a word for the purposes
    /// of selecting words in the display by double clicking on them.
    ///
    /// The word boundaries occur at the first and last characters which
    /// are either a letter, number, or a character in @p [`wc`]
    ///
    /// @param [`wc`] An array of characters which are to be considered parts
    /// of a word ( in addition to letters and numbers ).
    pub fn set_word_characters(&mut self, wc: String) {
        todo!()
    }
    /// Returns the characters which are considered part of a word for the
    /// purpose of selecting words in the display with the mouse.
    #[inline]
    pub fn word_characters(&self) -> &str {
        &self.word_characters
    }

    /// Sets the type of effect used to alert the user when a 'bell' occurs in the
    /// terminal session.
    ///
    /// The terminal session can trigger the bell effect by calling bell() with
    /// the alert message.
    pub fn set_bell_mode(&mut self, mode: i32) {
        todo!()
    }
    /// Returns the type of effect used to alert the user when a 'bell' occurs in
    /// the terminal session.
    #[inline]
    pub fn bell_mode(&self) -> i32 {
        self.bell_mode
    }

    pub fn set_selection(&mut self, t: String) {
        todo!()
    }

    /// Returns the font used to draw characters in the view.
    pub fn get_vt_font(&self) -> Font {
        todo!()
    }
    /// Sets the font used to draw the display.  Has no effect if @p [`font`]
    /// is larger than the size of the display itself.
    pub fn set_vt_font(&self, font: Font) {
        todo!()
    }

    /// Specify whether line chars should be drawn by ourselves or left to
    /// underlying font rendering libraries.
    #[inline]
    pub fn set_draw_line_chars(&mut self, draw_line_chars: bool) {
        self.draw_line_chars = draw_line_chars
    }

    /// Specifies whether characters with intense colors should be rendered
    /// as bold. Defaults to true.
    #[inline]
    pub fn set_bold_intense(&mut self, bold_intense: bool) {
        self.bold_intense = bold_intense
    }
    /// Returns true if characters with intense colors are rendered in bold.
    #[inline]
    pub fn get_bold_intense(&self) -> bool {
        self.bold_intense
    }

    /// Sets whether or not the current height and width of the terminal in lines
    /// and columns is displayed whilst the widget is being resized.
    #[inline]
    pub fn set_terminal_size_hint(&mut self, on: bool) {
        self.terminal_size_hint = on
    }
    /// Returns whether or not the current height and width of the terminal in lines
    /// and columns is displayed whilst the widget is being resized.
    #[inline]
    pub fn terminal_size_hint(&self) -> bool {
        self.terminal_size_hint
    }

    ///  Sets whether the terminal size display is shown briefly
    /// after the widget is first shown.
    ///
    /// See [`set_terminal_size_hint()`] , [`is_terminal_size_hint()`]
    #[inline]
    pub fn set_terminal_size_startup(&mut self, on: bool) {
        self.terminal_size_start_up = on
    }

    /// Sets the status of the BiDi rendering inside the terminal view.
    /// Defaults to disabled.
    #[inline]
    pub fn set_bidi_enable(&mut self, enable: bool) {
        self.bidi_enable = enable
    }
    /// Returns the status of the BiDi rendering in this widget.
    #[inline]
    pub fn is_bidi_enable(&mut self) -> bool {
        self.bidi_enable
    }

    /// Sets the terminal screen section which is displayed in this widget.
    /// When [`update_image()`] is called, the view fetches the latest character
    /// image from the the associated terminal screen window.
    ///
    /// In terms of the model-view paradigm, the ScreenWindow is the model which is
    /// rendered by the TerminalView.
    #[inline]
    pub fn set_screen_window(&mut self, window: &ScreenWindow) {
        todo!()
    }
    /// Returns the terminal screen section which is displayed in this widget.
    /// See [`set_screen_window()`]
    #[inline]
    pub fn get_screen_window(&self) -> Option<&ScreenWindow> {
        match self.screen_window.as_ref() {
            Some(window) => unsafe { Some(window.as_ref()) },
            None => None,
        }
    }

    pub fn set_motion_after_pasting(&mut self, action: MotionAfterPasting) {
        todo!()
    }
    pub fn motion_after_pasting(&self) -> MotionAfterPasting {
        todo!()
    }
    pub fn set_confirm_multiline_paste(&mut self, confirm_multiline_paste: bool) {
        todo!()
    }
    pub fn set_trim_pasted_trailing_new_lines(&mut self, trim_pasted_trailing_new_lines: bool) {
        todo!()
    }

    /// maps a point on the widget to the position ( ie. line and column )
    /// of the character at that point.
    pub fn get_character_position(&self, widget_point: Point) -> (i32, i32) {
        todo!()
    }

    #[inline]
    pub fn disable_bracketed_paste_mode(&mut self, disable: bool) {
        self.disable_bracketed_paste_mode = disable
    }
    #[inline]
    pub fn is_disable_bracketed_paste_mode(&self) -> bool {
        self.disable_bracketed_paste_mode
    }

    #[inline]
    pub fn set_bracketed_paste_mode(&mut self, bracketed_paste_mode: bool) {
        self.bracketed_paste_mode = bracketed_paste_mode
    }
    #[inline]
    pub fn bracketed_paste_mode(&mut self) -> bool {
        self.bracketed_paste_mode
    }

    ////////////////////////////////////// Slots. //////////////////////////////////////
    /// Causes the terminal view to fetch the latest character image from the
    /// associated terminal screen ( see [`set_screen_window()`] ) and redraw the view.
    pub fn update_image(&mut self) {
        todo!()
    }

    /// Essentially calls [`process_filters()`].
    pub fn update_filters(&mut self) {
        todo!()
    }

    /// Causes the terminal view to fetch the latest line status flags from the
    /// associated terminal screen ( see [`set_screen_window()`] ).
    pub fn update_line_properties(&mut self) {
        todo!()
    }

    /// Copies the selected text to the clipboard.
    pub fn copy_clipboard(&mut self) {
        todo!()
    }

    /// Pastes the content of the clipboard into the view.
    pub fn paste_clipboard(&mut self) {
        todo!()
    }

    /// Pastes the content of the selection into the view.
    pub fn paste_selection(&mut self) {
        todo!()
    }

    /// Causes the widget to display or hide a message informing the user that
    /// terminal output has been suspended (by using the flow control key
    /// combination Ctrl+S)
    ///
    /// @param [`suspended`] True if terminal output has been suspended and the warning
    /// message should be shown or false to indicate that terminal output has been
    /// resumed and that the warning message should disappear.
    pub fn output_suspended(&mut self, suspended: bool) {
        todo!()
    }

    /// Sets whether the program whose output is being displayed in the view
    /// is interested in mouse events.
    ///
    /// If this is set to true, mouse signals will be emitted by the view when the
    /// user clicks, drags or otherwise moves the mouse inside the view. The user
    /// interaction needed to create selections will also change, and the user will
    /// be required to hold down the shift key to create a selection or perform
    /// other mouse activities inside the view area - since the program running in
    /// the terminal is being allowed to handle normal mouse events itself.
    ///
    /// @param [`uses_mouse`] Set to true if the program running in the terminal is
    /// interested in mouse events or false otherwise.
    pub fn set_uses_mouse(&mut self, uses_mouse: bool) {
        todo!()
    }

    /// See [`set_uses_mouse()`]
    pub fn uses_mouse(&mut self) {
        todo!()
    }

    /// Shows a notification that a bell event has occurred in the terminal.
    pub fn bell(&mut self, message: &str) {
        todo!()
    }

    /// Sets the background of the view to the specified color.
    /// @see [`set_color_table()`], [`set_foreground_color()`]
    pub fn set_background_color(&mut self, color: Color) {
        todo!()
    }

    /// Sets the text of the view to the specified color.
    /// @see [`set_color_table()`], [`set_background_color()`]
    pub fn set_foreground_color(&mut self, color: Color) {
        todo!()
    }

    pub fn selection_changed(&mut self) {
        todo!()
    }

    fn scroll_bar_position_changed(&mut self, value: i32) {
        todo!()
    }

    fn blink_event(&mut self) {
        todo!()
    }

    fn blink_cursor_event(&mut self) {
        todo!()
    }

    /// Renables bell noises and visuals.  Used to disable further bells for a
    /// short period of time after emitting the first in a sequence of bell events.
    fn enable_bell(&mut self) {
        todo!()
    }

    fn swap_color_table(&mut self) {
        todo!()
    }

    fn triple_click_timeout(&mut self) {
        todo!()
    }

    ////////////////////////////////////// Private functions. //////////////////////////////////////
    fn font_change(&mut self, font: Font) {
        todo!()
    }

    fn extend_selection(&mut self, pos: Point) {
        todo!()
    }

    fn do_drag(&mut self) {
        todo!()
    }

    /// classifies the 'ch' into one of three categories
    /// and returns a character to indicate which category it is in
    ///
    ///     - A space (returns ' ')
    ///     - Part of a word (returns 'a')
    ///     - Other characters (returns the input character)
    fn char_class(&mut self, ch: u8) -> u8 {
        todo!()
    }

    fn clear_image(&mut self) {
        todo!()
    }

    /// TODO: add MouseEvent
    fn mouse_triple_click_event(&mut self) {
        todo!()
    }

    /// determine the width of this text.
    fn text_width(&mut self, start_column: i32, length: i32, line: i32) -> i32 {
        todo!()
    }
    /// determine the area that encloses this series of characters.
    fn calculate_text_area(
        &self,
        top_left_x: i32,
        top_left_y: i32,
        start_column: i32,
        line: i32,
        length: i32,
    ) -> Rect {
        todo!()
    }

    /// divides the part of the display specified by 'rect' into
    /// fragments according to their colors and styles and calls
    /// drawTextFragment() to draw the fragments
    fn draw_contents(&mut self, painter: &mut Painter, rect: Rect) {
        todo!()
    }
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////
/// Predefine
//////////////////////////////////////////////////////////////////////////////////////////////////////////
static ANTIALIAS_TEXT: AtomicBool = AtomicBool::new(true);
static HAVE_TRANSPARENCY: AtomicBool = AtomicBool::new(true);
static TEXT_BLINK_DELAY: AtomicI32 = AtomicI32::new(500);

const REPCHAR: &'static str = "
ABCDEFGHIJKLMNOPQRSTUVWXYZ
abcdefgjijklmnopqrstuvwxyz
0123456789./+@
";

const LTR_OVERRIDE_CHAR: u16 = 0x202D;

//////////////////////////////////////////////////////////////////////////////////////////////////////////
/// Display Operations
//////////////////////////////////////////////////////////////////////////////////////////////////////////
#[repr(u32)]
enum LineEncode {
    TopL = 1 << 1,
    TopC = 1 << 2,
    TopR = 1 << 3,

    LeftT = 1 << 5,
    Int11 = 1 << 6,
    Int12 = 1 << 7,
    Int13 = 1 << 8,
    RightT = 1 << 9,

    LeftC = 1 << 10,
    Int21 = 1 << 11,
    Int22 = 1 << 12,
    Int23 = 1 << 13,
    RightC = 1 << 14,

    LeftB = 1 << 15,
    Int31 = 1 << 16,
    Int32 = 1 << 17,
    Int33 = 1 << 18,
    RightB = 1 << 19,

    BotL = 1 << 21,
    BotC = 1 << 22,
    BotR = 1 << 23,
}

const LINE_CHARS: [u32; 128] = [
    0x00007c00, 0x000fffe0, 0x00421084, 0x00e739ce, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
    0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00427000, 0x004e7380, 0x00e77800, 0x00ef7bc0,
    0x00421c00, 0x00439ce0, 0x00e73c00, 0x00e7bde0, 0x00007084, 0x000e7384, 0x000079ce, 0x000f7bce,
    0x00001c84, 0x00039ce4, 0x00003dce, 0x0007bdee, 0x00427084, 0x004e7384, 0x004279ce, 0x00e77884,
    0x00e779ce, 0x004f7bce, 0x00ef7bc4, 0x00ef7bce, 0x00421c84, 0x00439ce4, 0x00423dce, 0x00e73c84,
    0x00e73dce, 0x0047bdee, 0x00e7bde4, 0x00e7bdee, 0x00427c00, 0x0043fce0, 0x004e7f80, 0x004fffe0,
    0x004fffe0, 0x00e7fde0, 0x006f7fc0, 0x00efffe0, 0x00007c84, 0x0003fce4, 0x000e7f84, 0x000fffe4,
    0x00007dce, 0x0007fdee, 0x000f7fce, 0x000fffee, 0x00427c84, 0x0043fce4, 0x004e7f84, 0x004fffe4,
    0x00427dce, 0x00e77c84, 0x00e77dce, 0x0047fdee, 0x004e7fce, 0x00e7fde4, 0x00ef7f84, 0x004fffee,
    0x00efffe4, 0x00e7fdee, 0x00ef7fce, 0x00efffee, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
    0x000f83e0, 0x00a5294a, 0x004e1380, 0x00a57800, 0x00ad0bc0, 0x004390e0, 0x00a53c00, 0x00a5a1e0,
    0x000e1384, 0x0000794a, 0x000f0b4a, 0x000390e4, 0x00003d4a, 0x0007a16a, 0x004e1384, 0x00a5694a,
    0x00ad2b4a, 0x004390e4, 0x00a52d4a, 0x00a5a16a, 0x004f83e0, 0x00a57c00, 0x00ad83e0, 0x000f83e4,
    0x00007d4a, 0x000f836a, 0x004f93e4, 0x00a57d4a, 0x00ad836a, 0x00000000, 0x00000000, 0x00000000,
    0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00001c00, 0x00001084, 0x00007000, 0x00421000,
    0x00039ce0, 0x000039ce, 0x000e7380, 0x00e73800, 0x000e7f80, 0x00e73884, 0x0003fce0, 0x004239ce,
];

fn draw_line_char(painter: &mut Painter, x: i32, y: i32, w: i32, h: i32, code: u8) {
    // Calculate cell midpoints, end points.
    let cx = x + w / 2;
    let cy = y + h / 2;
    let ex = x + w - 1;
    let ey = y + h - 1;

    let to_draw = LINE_CHARS[code as usize];

    // Top lines:
    if to_draw & TopL as u32 > 0 {
        painter.draw_line(cx - 1, y, cx - 1, cy - 2);
    }
    if to_draw & TopC as u32 > 0 {
        painter.draw_line(cx, y, cx, cy - 2);
    }
    if to_draw & TopR as u32 > 0 {
        painter.draw_line(cx + 1, y, cx + 1, cy - 2);
    }

    // Bot lines:
    if to_draw & BotL as u32 > 0 {
        painter.draw_line(cx - 1, cy + 2, cx - 1, ey);
    }
    if to_draw & BotC as u32 > 0 {
        painter.draw_line(cx, cy + 2, cx, ey);
    }
    if to_draw & BotR as u32 > 0 {
        painter.draw_line(cx + 1, cy + 2, cx + 1, ey);
    }

    // Left lines:
    if to_draw & LeftT as u32 > 0 {
        painter.draw_line(x, cy - 1, cx - 2, cy - 1);
    }
    if to_draw & LeftC as u32 > 0 {
        painter.draw_line(x, cy, cx - 2, cy);
    }
    if to_draw & LeftB as u32 > 0 {
        painter.draw_line(x, cy + 1, cx - 2, cy + 1);
    }

    // Right lines:
    if to_draw & RightT as u32 > 0 {
        painter.draw_line(cx + 2, cy - 1, ex, cy - 1);
    }
    if to_draw & RightC as u32 > 0 {
        painter.draw_line(cx + 2, cy, ex, cy);
    }
    if to_draw & RightB as u32 > 0 {
        painter.draw_line(cx + 2, cy + 1, ex, cy + 1);
    }

    // Intersection points.
    if to_draw & Int11 as u32 > 0 {
        painter.draw_point(cx - 1, cy - 1);
    }
    if to_draw & Int12 as u32 > 0 {
        painter.draw_point(cx, cy - 1);
    }
    if to_draw & Int13 as u32 > 0 {
        painter.draw_point(cx + 1, cy - 1);
    }

    if to_draw & Int21 as u32 > 0 {
        painter.draw_point(cx - 1, cy);
    }
    if to_draw & Int22 as u32 > 0 {
        painter.draw_point(cx, cy);
    }
    if to_draw & Int23 as u32 > 0 {
        painter.draw_point(cx + 1, cy);
    }

    if to_draw & Int31 as u32 > 0 {
        painter.draw_point(cx - 1, cy + 1);
    }
    if to_draw & Int32 as u32 > 0 {
        painter.draw_point(cx, cy + 1);
    }
    if to_draw & Int33 as u32 > 0 {
        painter.draw_point(cx + 1, cy + 1);
    }
}

fn draw_other_char(painter: &mut Painter, x: i32, y: i32, w: i32, h: i32, code: u8) {
    // Calculate cell midpoints, end points.
    let cx = x + w / 2;
    let cy = y + h / 2;
    let ex = x + w - 1;
    let ey = y + h - 1;

    // Double dashes
    if 0x4C <= code && code <= 0x4F {
        let x_half_gap = 1.max(w / 15);
        let y_half_gap = 1.max(h / 15);

        match code {
            0x4D => {
                // BOX DRAWINGS HEAVY DOUBLE DASH HORIZONTAL
                painter.draw_line(x, cy - 1, cx - x_half_gap - 1, cy - 1);
                painter.draw_line(x, cy + 1, cx - x_half_gap - 1, cy + 1);
                painter.draw_line(cx + x_half_gap, cy - 1, ex, cy - 1);
                painter.draw_line(cx + x_half_gap, cy + 1, ex, cy + 1);
            }
            0x4C => {
                // BOX DRAWINGS LIGHT DOUBLE DASH HORIZONTAL
                painter.draw_line(x, cy, cx - x_half_gap - 1, cy);
                painter.draw_line(cx + x_half_gap, cy, ex, cy);
            }
            0x4F => {
                // BOX DRAWINGS HEAVY DOUBLE DASH VERTICAL
                painter.draw_line(cx - 1, y, cx - 1, cy - y_half_gap - 1);
                painter.draw_line(cx + 1, y, cx + 1, cy - y_half_gap - 1);
                painter.draw_line(cx - 1, cy + y_half_gap, cx - 1, ey);
                painter.draw_line(cx + 1, cy + y_half_gap, cx + 1, ey);
            }
            0x4E => {
                // BOX DRAWINGS LIGHT DOUBLE DASH VERTICAL
                painter.draw_line(cx, y, cx, cy - y_half_gap - 1);
                painter.draw_line(cx, cy + y_half_gap, cx, ey);
            }
            _ => {}
        }

    // Rounded corner characters
    } else if 0x6D <= code && code <= 0x70 {
        let r = w * 3 / 8;
        let d = 2 * r;

        match code {
            0x6D => {
                // BOX DRAWINGS LIGHT ARC DOWN AND RIGHT
                painter.draw_line(cx, cy + r, cx, ey);
                painter.draw_line(cx + r, cy, ex, cy);
                painter.draw_arc(cx, cy, d, d, 90 * 16, 90 * 16);
            }
            0x6E => {
                // BOX DRAWINGS LIGHT ARC DOWN AND LEFT
                painter.draw_line(cx, cy + r, cx, ey);
                painter.draw_line(x, cy, cx - r, cy);
                painter.draw_arc(cx - d, cy, d, d, 0 * 16, 90 * 16);
            }
            0x6F => {
                // BOX DRAWINGS LIGHT ARC UP AND LEFT
                painter.draw_line(cx, y, cx, cy - r);
                painter.draw_line(x, cy, cx - r, cy);
                painter.draw_arc(cx - d, cy - d, d, d, 270 * 16, 90 * 16);
            }
            0x70 => {
                // BOX DRAWINGS LIGHT ARC UP AND RIGHT
                painter.draw_line(cx, y, cx, cy - r);
                painter.draw_line(cx + r, cy, ex, cy);
                painter.draw_arc(cx, cy - d, d, d, 180 * 16, 90 * 16);
            }
            _ => {}
        }

    // Diagonals
    } else if 0x71 <= code && code <= 0x73 {
        match code {
            0x71 => {
                // BOX DRAWINGS LIGHT DIAGONAL UPPER RIGHT TO LOWER LEFT
                painter.draw_line(ex, y, x, ey);
            }
            0x72 => {
                // BOX DRAWINGS LIGHT DIAGONAL UPPER LEFT TO LOWER RIGHT
                painter.draw_line(x, y, ex, ey);
            }
            0x73 => {
                // BOX DRAWINGS LIGHT DIAGONAL CROSS
                painter.draw_line(ex, y, x, ey);
                painter.draw_line(x, y, ex, ey);
            }
            _ => {}
        }
    }
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////
/// Enums
//////////////////////////////////////////////////////////////////////////////////////////////////////////
#[repr(u8)]
#[derive(Debug, Default, PartialEq, Eq, Clone, Copy)]
pub enum ScrollBarPosition {
    #[default]
    NoScrollBar = 0,
    ScrollBarLeft,
    ScrollBarRight,
}
impl From<u8> for ScrollBarPosition {
    fn from(x: u8) -> Self {
        match x {
            0 => Self::NoScrollBar,
            1 => Self::ScrollBarLeft,
            2 => Self::ScrollBarRight,
            _ => unimplemented!(),
        }
    }
}

#[repr(u8)]
#[derive(Debug, Default, PartialEq, Eq, Clone, Copy)]
pub enum TripleClickMode {
    #[default]
    SelectWholeLine = 0,
    SelectForwardsFromCursor,
}
impl From<u8> for TripleClickMode {
    fn from(x: u8) -> Self {
        match x {
            0 => Self::SelectWholeLine,
            1 => Self::SelectForwardsFromCursor,
            _ => unimplemented!(),
        }
    }
}

#[repr(u8)]
#[derive(Debug, Default, PartialEq, Eq, Clone, Copy)]
pub enum MotionAfterPasting {
    #[default]
    NoMoveScreenWindow = 0,
    MoveStartScreenWindow,
    MoveEndScreenWindow,
}
impl From<u8> for MotionAfterPasting {
    fn from(x: u8) -> Self {
        match x {
            0 => Self::NoMoveScreenWindow,
            1 => Self::MoveStartScreenWindow,
            2 => Self::MoveEndScreenWindow,
            _ => unimplemented!(),
        }
    }
}

#[repr(u8)]
#[derive(Debug, Default, PartialEq, Eq, Clone, Copy)]
pub enum KeyboardCursorShape {
    #[default]
    BlockCursor = 0,
    UnderlineCursor,
    IBeamCursor,
}
impl From<u8> for KeyboardCursorShape {
    fn from(x: u8) -> Self {
        match x {
            0 => Self::BlockCursor,
            1 => Self::UnderlineCursor,
            2 => Self::IBeamCursor,
            _ => unimplemented!(),
        }
    }
}

#[repr(u8)]
#[derive(Debug, Default, PartialEq, Eq, Clone, Copy)]
pub enum BackgroundMode {
    #[default]
    None = 0,
    Stretch,
    Zoom,
    Fit,
    Center,
}
impl From<u8> for BackgroundMode {
    fn from(x: u8) -> Self {
        match x {
            0 => Self::None,
            1 => Self::Stretch,
            2 => Self::Zoom,
            3 => Self::Fit,
            4 => Self::Center,
            _ => unimplemented!(),
        }
    }
}

#[repr(u8)]
#[derive(Debug, Default, PartialEq, Eq, Clone, Copy)]
pub enum BellMode {
    #[default]
    SystemBeepBell = 0,
    NotifyBell,
    VisualBell,
    NoBell,
}
impl From<u8> for BellMode {
    fn from(x: u8) -> Self {
        match x {
            0 => Self::SystemBeepBell,
            1 => Self::NotifyBell,
            2 => Self::VisualBell,
            3 => Self::NoBell,
            _ => unimplemented!(),
        }
    }
}

#[repr(u8)]
#[derive(Debug, Default, PartialEq, Eq, Clone, Copy)]
pub enum DragState {
    #[default]
    DiNone = 0,
    DiPending,
    DiDragging,
}
impl From<u8> for DragState {
    fn from(x: u8) -> Self {
        match x {
            0 => Self::DiNone,
            1 => Self::DiPending,
            2 => Self::DiDragging,
            _ => unimplemented!(),
        }
    }
}
