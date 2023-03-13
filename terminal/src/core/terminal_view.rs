#![allow(dead_code)]
use super::screen_window::{ScreenWindow, ScreenWindowSignals};
use crate::tools::{
    character::{
        Character, ExtendedCharTable, LineProperty, DEFAULT_RENDITION, LINE_DOUBLE_HEIGHT,
        LINE_DOUBLE_WIDTH, LINE_WRAPPED, RE_BLINK, RE_BOLD, RE_CONCEAL, RE_CURSOR, RE_EXTEND_CHAR,
        RE_ITALIC, RE_OVERLINE, RE_STRIKEOUT, RE_UNDERLINE,
    },
    character_color::{
        CharacterColor, ColorEntry, DEFAULT_BACK_COLOR, DEFAULT_FORE_COLOR, TABLE_COLORS,
    },
    filter::{FilterChainImpl, TerminalImageFilterChain},
    system_ffi::string_width,
};
use log::warn;
use regex::Regex;
use std::{
    mem::size_of,
    ptr::NonNull,
    sync::atomic::{AtomicBool, AtomicU64, Ordering},
    time::Duration,
};
use tmui::{
    graphics::{
        figure::{Color, FRect, FontTypeface, Size, Transform},
        painter::Painter,
    },
    label::Label,
    prelude::*,
    scroll_bar::ScrollBar,
    tlib::{
        connect, disconnect, emit,
        events::{KeyEvent, MouseEvent},
        object::{ObjectImpl, ObjectSubclass},
        signals,
        timer::Timer,
    },
    widget::WidgetImpl, system::System, clipboard::ClipboardLevel,
};
use wchar::{wch, wchar_t};
use widestring::U16String;
use LineEncode::*;
use lazy_static::lazy_static;

lazy_static! {
    pub static ref REGULAR_EXPRESSION: Regex = Regex::new("\\r+$").unwrap();
}

#[extends_widget]
#[derive(Default)]
pub struct TerminalView {
    extended_char_table: ExtendedCharTable,

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

    used_lines: i32,
    used_columns: i32,

    content_height: i32,
    content_width: i32,

    image: Option<Vec<Character>>,
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
    scroll_bar: ScrollBar,
    scroll_bar_location: ScrollBarPosition,
    word_characters: String,
    bell_mode: BellMode,

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
    blink_timer: Timer,
    blink_cursor_timer: Timer,

    // true during visual bell.
    colors_inverted: bool,

    resize_widget: Label,
    resize_timer: Timer,

    output_suspend_label: Label,

    line_spacing: u32,
    opacity: f64,
    size: Size,

    // Add background_image Pixmap
    background_mode: BackgroundMode,

    filter_chain: Box<TerminalImageFilterChain>,
    mouse_over_hotspot_area: Rect,

    cursor_shape: KeyboardCursorShape,
    cursor_color: Color,

    motion_after_pasting: MotionAfterPasting,
    confirm_multiline_paste: bool,
    trim_pasted_trailing_new_lines: bool,

    input_method_data: InputMethodData,

    draw_line_chars: bool,
}

#[derive(Default)]
struct InputMethodData {
    preedit_string: U16String,
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

impl ObjectImpl for TerminalView {
    fn initialize(&mut self) {
        self.extended_char_table.initialize();
    }
}

impl WidgetImpl for TerminalView {
    fn paint(&mut self, mut painter: Painter) {
        painter.set_antialiasing();
        let _cr = self.contents_rect(Some(Coordinate::Widget));

        // TODO: Process the background image.

        if self.draw_text_test_flag {
            self.cal_draw_text_addition_height(&mut painter);
        }

        // TODO: Specified the region to redraw
        let rect = self.contents_rect(Some(Coordinate::Widget));
        // TODO: Multiple rect
        self.draw_background(&mut painter, rect, self.background(), true);
        self.draw_contents(&mut painter, rect);

        // self.draw_input_method_preedit_string(&mut painter, &self.preddit_rect());
        self.paint_filters(&mut painter);
    }
}

impl TerminalView {
    pub fn new() -> Self {
        todo!()
    }
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

    //////////////////////////////////////////////// Drawing functions start.  ////////////////////////////////////////////////
    /// divides the part of the display specified by 'rect' into
    /// fragments according to their colors and styles and calls
    /// drawTextFragment() to draw the fragments
    fn draw_contents(&mut self, painter: &mut Painter, rect: Rect) {
        let tl = self.contents_rect(Some(Coordinate::Widget));

        let tlx = tl.x();
        let tly = tl.y();

        let lux = (self.used_columns - 1)
            .min(0.max((rect.left() - tlx - self.left_margin) / self.font_width));
        let luy = (self.used_lines - 1)
            .min(0.max((rect.top() - tly - self.top_margin) / self.font_height));
        let rlx = (self.used_columns - 1)
            .min(0.max((rect.right() - tlx - self.left_margin) / self.font_width));
        let rly = (self.used_lines - 1)
            .min(0.max((rect.bottom() - tly - self.top_margin) / self.font_height));

        let buffer_size = self.used_columns as usize;
        let mut unistr = vec![0u16; buffer_size];

        let mut y = luy;
        while y <= rly {
            let mut c = self.image.as_ref().unwrap()[self.loc(lux, y) as usize]
                .character_union
                .data();
            let mut x = lux;
            if !c != 0 && x != 0 {
                // Search for start of multi-column character
                x -= 1;
            }
            while x <= rlx {
                let mut len = 1;
                let mut p = 0;

                x += 1;

                // reset buffer to the maximal size
                unistr.resize(buffer_size, 0);

                // is this a single character or a sequence of characters ?
                if self.image.as_ref().unwrap()[self.loc(x, y) as usize].rendition & RE_EXTEND_CHAR
                    != 0
                {
                    // sequence of characters
                    let mut extended_char_length = 0u16;
                    let chars = ExtendedCharTable::instance()
                        .lookup_extended_char(
                            self.image.as_ref().unwrap()[self.loc(x, y) as usize]
                                .character_union
                                .data(),
                            &mut extended_char_length,
                        )
                        .unwrap();
                    for index in 0..extended_char_length as usize {
                        assert!(p < buffer_size);
                        unistr[p] = chars[index];
                    }
                } else {
                    c = self.image.as_ref().unwrap()[self.loc(x, y) as usize]
                        .character_union
                        .data();
                    if c != 0 {
                        assert!(p < buffer_size);
                        unistr[p] = c;
                    }
                }

                let line_draw = self.is_line_char(c);
                let double_width = self.image.as_ref().unwrap()
                    [self.image_size.min(self.loc(x, y) + 1) as usize]
                    .character_union
                    .data()
                    == 0;

                let img = &self.image.as_ref().unwrap()[self.loc(x, y) as usize];
                let current_foreground = img.foreground_color;
                let current_background = img.background_color;
                let current_rendition = img.rendition;

                let mut img = &self.image.as_ref().unwrap()[self.loc(x + len, y) as usize];
                while x + len <= rlx
                    && img.foreground_color == current_foreground
                    && img.background_color == current_background
                    && img.rendition == current_rendition
                    && (self.image.as_ref().unwrap()
                        [self.image_size.min(self.loc(x + len, y) + 1) as usize]
                        .character_union
                        .data()
                        == 0)
                        == double_width
                    && self.is_line_char(img.character_union.data()) == line_draw
                {
                    c = img.character_union.data();
                    if c != 0 {
                        unistr[p] = c;
                        p += 1;
                    }

                    if double_width {
                        len += 1;
                    }
                    len += 1;

                    img = &self.image.as_ref().unwrap()[self.loc(x + len, y) as usize];
                }

                if x + len < self.used_columns
                    && !self.image.as_ref().unwrap()[self.loc(x + len, y) as usize]
                        .character_union
                        .data()
                        != 0
                {
                    len += 1;
                }

                let save_fixed_font = self.fixed_font;
                if line_draw {
                    self.fixed_font = false;
                }
                unistr.resize(p as usize, 0);

                // Create a text scaling matrix for double width and double height lines.
                let mut text_scale = Transform::new();

                if y < self.line_properties.len() as i32 {
                    if self.line_properties[y as usize] & LINE_DOUBLE_WIDTH != 0 {
                        text_scale.scale(2., 1.);
                    }
                    if self.line_properties[y as usize] & LINE_DOUBLE_HEIGHT != 0 {
                        text_scale.scale(1., 2.);
                    }
                }

                // calculate the area in which the text will be drawn
                let mut text_area = self.calculate_text_area(tlx, tly, x, y, len);

                // move the calculated area to take account of scaling applied to the
                // painter. the position of the area from the origin (0,0) is scaled by
                // the opposite of whatever transformation has been applied to the
                // painter. this ensures that painting does actually start from
                // textArea.topLeft()
                //(instead of textArea.topLeft() * painter-scale)
                text_area.move_top_left(&text_scale.inverted().map_point(&text_area.top_left()));

                // Apply text scaling matrix.
                painter.set_transform(text_scale, true);

                // paint text fragment
                let style = self.image.as_ref().unwrap()[self.loc(x, y) as usize];
                self.draw_text_fragment(
                    painter,
                    text_area,
                    U16String::from_vec(unistr.clone()),
                    &style,
                );

                self.fixed_font = save_fixed_font;

                // reset back to single-width, single-height lines.
                painter.set_transform(text_scale.inverted(), true);

                if y < self.line_properties.len() as i32 - 1 {
                    // double-height lines are represented by two adjacent lines
                    // containing the same characters
                    // both lines will have the LINE_DOUBLEHEIGHT attribute.
                    // If the current line has the LINE_DOUBLEHEIGHT attribute,
                    // we can therefore skip the next line
                    y += 1;
                }

                x += len - 1;
            }
            y += 1;
        }
    }
    /// draws a section of text, all the text in this section
    /// has a common color and style
    fn draw_text_fragment(
        &mut self,
        painter: &mut Painter,
        rect: Rect,
        text: U16String,
        style: &Character,
    ) {
        painter.save();

        let foreground_color = style.foreground_color.color(&self.color_table);
        let background_color = style.background_color.color(&self.color_table);

        if background_color != self.background() {
            self.draw_background(painter, rect, background_color, false);
        }

        let mut invert_character_color = false;

        // draw text
        self.draw_characters(painter, rect, &text, style, invert_character_color);

        if style.rendition & RE_CURSOR != 0 {
            self.draw_cursor(painter, rect, foreground_color, &mut invert_character_color);
        }

        painter.restore();
    }
    /// draws the background for a text fragment
    /// if useOpacitySetting is true then the color's alpha value will be set to
    /// the display's transparency (set with setOpacity()), otherwise the
    /// background will be drawn fully opaque
    fn draw_background(
        &mut self,
        painter: &mut Painter,
        rect: Rect,
        color: Color,
        use_opacity_setting: bool,
    ) {
        // TODO: Return if there is a background image
        // TODO: Set the opacity
        painter.save();
        painter.fill_rect(rect, color);
        painter.restore();
    }
    /// draws the cursor character.
    fn draw_cursor(
        &mut self,
        painter: &mut Painter,
        rect: Rect,
        foreground_color: Color,
        invert_colors: &mut bool,
    ) {
        let mut cursor_rect: FRect = rect.into();
        cursor_rect.set_height(self.font_height as f32 - self.line_spacing as f32 - 1.);

        if !self.cursor_blinking {
            if self.cursor_color.valid {
                painter.set_color(self.cursor_color);
            } else {
                painter.set_color(foreground_color);
            }

            if self.cursor_shape == KeyboardCursorShape::BlockCursor {
                // draw the cursor outline, adjusting the area so that
                // it is draw entirely inside 'rect'
                let line_width = painter.line_width().max(1.);

                painter.draw_rect(cursor_rect.adjusted(
                    line_width / 2.,
                    line_width / 2.,
                    -line_width / 2.,
                    -line_width / 2.,
                ));

                if self.is_focus() {
                    painter.fill_rect(
                        rect,
                        if self.cursor_color.valid {
                            self.cursor_color
                        } else {
                            foreground_color
                        },
                    );

                    if !self.cursor_color.valid {
                        // invert the colour used to draw the text to ensure that the
                        // character at the cursor position is readable
                        *invert_colors = true;
                    }
                }
            } else if self.cursor_shape == KeyboardCursorShape::UnderlineCursor {
                painter.draw_line_f(
                    cursor_rect.left(),
                    cursor_rect.bottom(),
                    cursor_rect.right(),
                    cursor_rect.bottom(),
                )
            } else if self.cursor_shape == KeyboardCursorShape::IBeamCursor {
                painter.draw_line_f(
                    cursor_rect.left(),
                    cursor_rect.top(),
                    cursor_rect.left(),
                    cursor_rect.bottom(),
                )
            }
        }
    }
    /// draws the characters or line graphics in a text fragment.
    fn draw_characters(
        &mut self,
        painter: &mut Painter,
        rect: Rect,
        text: &U16String,
        style: &Character,
        invert_character_color: bool,
    ) {
        // Don't draw text which is currently blinking.
        if self.blinking && style.rendition & RE_BLINK != 0 {
            return;
        }

        // Don't draw concealed characters.
        if style.rendition & RE_CONCEAL != 0 {
            return;
        }

        // Setup bold, underline, intalic, strkeout and overline
        let use_bold = style.rendition & RE_BOLD != 0 && self.bold_intense;
        let use_underline = style.rendition & RE_UNDERLINE != 0;
        let use_italic = style.rendition & RE_ITALIC != 0;
        let use_strike_out = style.rendition & RE_STRIKEOUT != 0;
        let use_overline = style.rendition & RE_OVERLINE != 0;

        let mut font = self.font();
        let typeface = FontTypeface::builder()
            .bold(use_bold)
            .italic(use_italic)
            .build();
        font.set_typeface(typeface);
        painter.set_font(font);

        let text_color = if invert_character_color {
            style.background_color
        } else {
            style.foreground_color
        };
        let color = text_color.color(&self.color_table);
        painter.set_color(color);

        // Draw text
        if self.is_line_char_string(text) {
            self.draw_line_char_string(painter, rect.x(), rect.y(), text, style);
        } else {
            let text = text
                .to_string()
                .expect("`draw_characters()` transfer u16 text to utf-8 failed.");

            if self.bidi_enable {
                painter.fill_rect(rect, style.background_color.color(&self.color_table));
                painter.draw_text(
                    &text,
                    (
                        rect.x(),
                        rect.y() + self.font_ascend + self.line_spacing as i32,
                    ),
                );
            } else {
                let mut draw_rect = Rect::new(rect.x(), rect.y(), rect.width(), rect.height());
                draw_rect.set_height(rect.height() + self.draw_text_addition_height);
                painter.fill_rect(draw_rect, style.background_color.color(&self.color_table));
                // Draw the text start at the left-bottom.
                painter.draw_text(&text, (rect.left(), rect.bottom()));

                if use_underline {
                    let y = rect.bottom() as f32 - 0.5;
                    painter.draw_line_f(rect.left() as f32, y, rect.right() as f32, y)
                }

                if use_strike_out {
                    let y = (rect.top() as f32 + rect.bottom() as f32) / 2.;
                    painter.draw_line_f(rect.left() as f32, y, rect.right() as f32, y)
                }

                if use_overline {
                    let y = rect.top() as f32 + 0.5;
                    painter.draw_line_f(rect.left() as f32, y, rect.right() as f32, y)
                }
            }
        }
    }
    /// draws a string of line graphics.
    fn draw_line_char_string(
        &mut self,
        painter: &mut Painter,
        x: i32,
        y: i32,
        str: &U16String,
        attributes: &Character,
    ) {
        //TODO: Save the pen status: Color, Font, line width etc...

        if attributes.rendition & RE_BOLD != 0 && self.bold_intense {
            painter.set_line_width(3.);
        }

        let u16_bytes = str.as_vec();
        for i in 0..u16_bytes.len() {
            let code = (u16_bytes[i] & 0xff) as u8;
            if LINE_CHARS[code as usize] != 0 {
                draw_line_char(
                    painter,
                    x + (self.font_width * i as i32),
                    y,
                    self.font_width,
                    self.font_height,
                    code,
                )
            } else {
                draw_other_char(
                    painter,
                    x + (self.font_width * i as i32),
                    y,
                    self.font_width,
                    self.font_height,
                    code,
                )
            }
        }

        //TODO: Restore the pen status
    }
    /// draws the preedit string for input methods.
    fn draw_input_method_preedit_string(&mut self, painter: &mut Painter, rect: &Rect) {
        todo!()
    }

    fn paint_filters(&mut self, painter: &mut Painter) {
        todo!()
    }

    fn cal_draw_text_addition_height(&mut self, painter: &mut Painter) {
        todo!()
    }
    //////////////////////////////////////////////// Drawing functions end.  ////////////////////////////////////////////////

    /// Returns the terminal color palette used by the view.
    #[inline]
    pub fn get_color_table(&self) -> &[ColorEntry] {
        &self.color_table
    }
    /// Sets the terminal color palette used by the view.
    #[inline]
    pub fn set_color_table(&mut self, table: &[ColorEntry]) {
        for i in 0..TABLE_COLORS {
            self.color_table[i] = table[i];
        }

        self.set_background_color(self.color_table[DEFAULT_BACK_COLOR as usize].color)
    }

    /// Sets the seed used to generate random colors for the view
    /// (in color schemes that support them).
    #[inline]
    pub fn set_random_seed(&mut self, seed: u32) {
        self.random_seed = seed
    }
    /// Returns the seed used to generate random colors for the view
    /// (in color schemes that support them).
    #[inline]
    pub fn random_seed(&self) -> u32 {
        self.random_seed
    }

    /// Sets the opacity of the terminal view.
    #[inline]
    pub fn set_opacity(&mut self, opacity: f64) {
        self.opacity = bound(0., opacity, 1.);
    }

    /// Sets the background image of the terminal view.
    pub fn set_background_image(&mut self, image: &str) {
        if !image.is_empty() {
            // TODO: load background image to Pixmap
        } else {
            // TODO: create a empty Pixmap
        }
    }
    /// Sets the background image mode of the terminal view.
    #[inline]
    pub fn set_background_mode(&mut self, mode: BackgroundMode) {
        self.background_mode = mode
    }

    /// Specifies whether the terminal display has a vertical scroll bar, and if so
    /// whether it is shown on the left or right side of the view.
    pub fn set_scroll_bar_position(&mut self, position: ScrollBarPosition) {
        if self.scroll_bar_location == position {
            return;
        }

        if position == ScrollBarPosition::NoScrollBar {
            // TODO: Hide scroll bar
        } else {
            // TODO: Show scroll bar
        }

        self.top_margin = 1;
        self.left_margin = 1;
        self.scroll_bar_location = position;

        self.propagate_size();
        self.update();
    }
    /// Setting the current position and range of the display scroll bar.
    pub fn set_scroll(&mut self, cursor: i32, lines: i32) {
        if self.scroll_bar.minimum() == 0
            && self.scroll_bar.maximum() == (lines - self.lines)
            && self.scroll_bar.value() == cursor
        {
            return;
        }
        disconnect!(self.scroll_bar, value_changed(), self, null);
        self.scroll_bar.set_range(0, lines - self.lines);
        self.scroll_bar.set_single_step(1);
        self.scroll_bar.set_page_step(lines);
        self.scroll_bar.set_value(cursor);
        connect!(
            self.scroll_bar,
            value_changed(),
            self,
            scroll_bar_position_changed(i32)
        );
    }
    /// Scroll to the bottom of the terminal (reset scrolling).
    pub fn scroll_to_end(&mut self) {
        disconnect!(self.scroll_bar, value_changed(), self, null);
        self.scroll_bar.set_value(self.scroll_bar.maximum());
        connect!(
            self.scroll_bar,
            value_changed(),
            self,
            scroll_bar_position_changed(i32)
        );

        let screen_window = unsafe { self.screen_window.as_mut().unwrap().as_mut() };
        screen_window.scroll_to(self.scroll_bar.value() + 1);
        screen_window.set_track_output(screen_window.at_end_of_output());
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
    pub fn filter_chain(&self) -> &impl FilterChainImpl {
        self.filter_chain.as_ref()
    }

    /// Updates the filters in the display's filter chain.  This will cause
    /// the hotspots to be updated to match the current image.
    ///
    /// TODO: This function can be expensive depending on the
    /// image size and number of filters in the filterChain()
    pub fn process_filters(&mut self) {
        if self.screen_window.is_none() {
            return;
        }
        let screen_window = unsafe { self.screen_window.as_mut().unwrap().as_mut() };

        let _pre_update_hotspots = self.hotspot_region();

        // use [`ScreenWindow::get_image()`] here rather than `image` because
        // other classes may call process_filters() when this view's
        // ScreenWindow emits a scrolled() signal - which will happen before
        // update_image() is called on the display and therefore _image is
        // out of date at this point
        let window_lines = screen_window.window_lines();
        let window_columns = screen_window.window_columns();
        let line_properties = &screen_window.get_line_properties();
        let image = screen_window.get_image();
        self.filter_chain
            .set_image(image, window_lines, window_columns, line_properties);
        self.filter_chain.process();

        let _post_update_hotspots = self.hotspot_region();

        // Should only update the region in pre_update_hotspots|post_update_hotspots
        self.update();
    }

    /// Returns a list of menu actions created by the filters for the content
    /// at the given @p position.
    pub fn filter_actions(&self, _position: Point) -> Vec<Action> {
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

    #[inline]
    pub fn set_line_spacing(&mut self, spacing: u32) {
        self.line_spacing = spacing;
        self.set_vt_font(self.font())
    }
    #[inline]
    pub fn line_spacing(&self) -> u32 {
        self.line_spacing
    }

    #[inline]
    pub fn set_margin(&mut self, margin: i32) {
        self.top_base_margin = margin;
        self.left_base_margin = margin;
    }
    #[inline]
    pub fn margin(&mut self) -> i32 {
        self.top_base_margin
    }

    /// @param [`use_x_selection`] Store and retrieve data from global mouse selection. 
    /// Support for selection is only available on systems with global mouse selection (such as X11).
    pub fn emit_selection(&mut self, _use_x_selection: bool, append_return: bool) {
        if self.screen_window.is_none() {
            return;
        }

        // Paste Clipboard by simulating keypress events
        let text = System::clipboard().text(ClipboardLevel::Os);
        if let Some(mut text) = text {
            if text.is_empty() {
                return
            }

            text = text.replace("\r\n", "\n").replace("\n", "\r");

            if self.trim_pasted_trailing_new_lines {
                text = REGULAR_EXPRESSION.replace(&text, "").to_string();
            }

            if self.confirm_multiline_paste && text.contains('\r') {
                if !self.multiline_confirmation(&text) {
                    return
                }
            }

            self.bracket_text(&mut text);

            // appendReturn is intentionally handled _after_ enclosing texts with
            // brackets as that feature is used to allow execution of commands
            // immediately after paste. Ref: https://bugs.kde.org/show_bug.cgi?id=16179
            if append_return {
                text.push('\r');
            }

            // TODO: 
            todo!()
        }
    }

    /// change and wrap text corresponding to paste mode.
    #[inline]
    pub fn bracket_text(&self, text: &mut String) {
        if self.bracketed_paste_mode() && !self.disable_bracketed_paste_mode {
            text.insert_str(0, "\u{001b}[200~");
            text.push_str("\u{001b}[201~");
        }
    }

    /// Sets the shape of the keyboard cursor. This is the cursor drawn
    /// at the position in the terminal where keyboard input will appear.
    ///
    /// In addition the terminal display widget also has a cursor for
    /// the mouse pointer, which can be set using the QWidget::setCursor() method.
    ///
    /// Defaults to BlockCursor
    #[inline]
    pub fn set_keyboard_cursor_shape(&mut self, shape: KeyboardCursorShape) {
        self.cursor_shape = shape
    }
    /// Returns the shape of the keyboard cursor.
    #[inline]
    pub fn keyboard_cursor_shape(&self) -> KeyboardCursorShape {
        self.cursor_shape
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
    #[inline]
    pub fn set_keyboard_cursor_color(&mut self, use_foreground_color: bool, color: Color) {
        if use_foreground_color {
            self.cursor_color = Color::new();
        } else {
            self.cursor_color = color
        }
    }
    /// Returns the color of the keyboard cursor, or an invalid color if the
    /// keyboard cursor color is set to change according to the foreground color of
    /// the character underneath it.
    #[inline]
    pub fn keyboard_cursor_color(&self) -> Color {
        self.cursor_color
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

    pub fn set_size(&mut self, _cols: i32, _lins: i32) {
        let scroll_bar_width = if !self.scroll_bar.visible() {
            0
        } else {
            self.scroll_bar.size_hint().width() as i32
        };

        let horizontal_margin = 2 * self.left_base_margin;
        let vertical_margin = 2 * self.top_base_margin;

        let new_size = Size::new(
            horizontal_margin + scroll_bar_width + (self.columns * self.font_width),
            vertical_margin + (self.lines * self.font_height),
        );

        if new_size != self.size() {
            self.size = new_size;
            // TODO: updateGeometry()
        }
    }
    pub fn set_fixed_size(&mut self, cols: i32, lins: i32) {
        self.is_fixed_size = true;

        // ensure that display is at least one line by one column in size.
        self.columns = 1.max(cols);
        self.lines = 1.max(lins);
        self.used_columns = self.used_columns.min(self.columns);
        self.used_lines = self.used_lines.min(self.lines);

        if self.image.is_some() {
            self.make_image();
        }
        self.set_size(cols, lins);
        // TODO: Set the fixed size to widget?
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
    #[inline]
    pub fn set_word_characters(&mut self, wc: String) {
        self.word_characters = wc
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
    #[inline]
    pub fn set_bell_mode(&mut self, mode: BellMode) {
        self.bell_mode = mode
    }
    /// Returns the type of effect used to alert the user when a 'bell' occurs in
    /// the terminal session.
    #[inline]
    pub fn bell_mode(&self) -> BellMode {
        self.bell_mode
    }

    pub fn set_selection(&mut self, t: String) {
        // TODO: set selection to clipboard
    }

    /// Returns the font used to draw characters in the view.
    pub fn get_vt_font(&self) -> Font {
        self.font()
    }
    /// Sets the font used to draw the display.  Has no effect if @p [`font`]
    /// is larger than the size of the display itself.
    pub fn set_vt_font(&mut self, mut font: Font) {
        if let Some(typeface) = font.typeface() {
            if !typeface.is_fixed_pitch() {
                warn!(
                    "Using a variable-width font in the terminal.  This may cause 
performance degradation and display/alignment errors."
                )
            }
        }

        // hint that text should be drawn with/without anti-aliasing.
        // depending on the user's font configuration, this may not be respected
        if ANTIALIAS_TEXT.load(Ordering::SeqCst) {
            font.set_edging(tmui::skia_safe::font::Edging::AntiAlias);
        } else {
            font.set_edging(tmui::skia_safe::font::Edging::Alias);
        }

        self.set_font(font);
        self.font_change();
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
    pub fn set_screen_window(&mut self, window: &mut ScreenWindow) {
        // The old ScreenWindow will be disconnected in emulation
        self.screen_window = NonNull::new(window);

        if self.screen_window.is_some() {
            connect!(window, output_changed(), self, update_line_properties());
            connect!(window, output_changed(), self, update_image());
            connect!(window, output_changed(), self, update_filters());
            connect!(window, scrolled(), self, update_filters());
            connect!(window, scroll_to_end(), self, scroll_to_end());
            window.set_window_lines(self.lines);
        }
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

    #[inline]
    pub fn set_motion_after_pasting(&mut self, action: MotionAfterPasting) {
        self.motion_after_pasting = action
    }
    #[inline]
    pub fn motion_after_pasting(&self) -> MotionAfterPasting {
        self.motion_after_pasting
    }
    #[inline]
    pub fn set_confirm_multiline_paste(&mut self, confirm_multiline_paste: bool) {
        self.confirm_multiline_paste = confirm_multiline_paste
    }
    #[inline]
    pub fn set_trim_pasted_trailing_new_lines(&mut self, trim_pasted_trailing_new_lines: bool) {
        self.trim_pasted_trailing_new_lines = trim_pasted_trailing_new_lines
    }

    /// maps a point on the widget to the position ( ie. line and column )
    /// of the character at that point.
    pub fn get_character_position(&self, widget_point: Point) -> (i32, i32) {
        let content_rect = self.contents_rect(Some(Coordinate::Widget));
        let mut line = (widget_point.y() - content_rect.top() - self.top_margin) / self.font_height;
        let mut column;
        if line < 0 {
            line = 0;
        }
        if line >= self.used_lines {
            line = self.used_lines - 1;
        }

        let x = widget_point.x() + self.font_width / 2 - content_rect.left() - self.left_margin;
        if self.fixed_font {
            column = x / self.font_width;
        } else {
            column = 0;
            while column + 1 < self.used_columns && x > self.text_width(0, column + 1, line) {
                column += 1;
            }
        }

        if column < 0 {
            column = 0;
        }

        // the column value returned can be equal to _usedColumns, which
        // is the position just after the last character displayed in a line.
        //
        // this is required so that the user can select characters in the right-most
        // column (or left-most for right-to-left input)
        if column > self.used_columns {
            column = self.used_columns;
        }
        (line, column)
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
    pub fn bracketed_paste_mode(&self) -> bool {
        self.bracketed_paste_mode
    }

    ////////////////////////////////////// Slots. //////////////////////////////////////
    /// Causes the terminal view to fetch the latest character image from the
    /// associated terminal screen ( see [`set_screen_window()`] ) and redraw the view.
    pub fn update_image(&mut self) {
        if self.screen_window.is_none() {
            return;
        }
        let screen_window = unsafe { self.screen_window.as_mut().unwrap().as_mut() };

        // optimization - scroll the existing image where possible and
        // avoid expensive text drawing for parts of the image that
        // can simply be moved up or down
        self.scroll_image(screen_window.scroll_count(), &screen_window.scroll_region());
        screen_window.reset_scroll_count();

        if self.image.is_none() {
            // Create _image.
            // The emitted changedContentSizeSignal also leads to getImage being
            // recreated, so do this first.
            self.update_image_size()
        }

        let lines = screen_window.window_lines();
        let columns = screen_window.window_columns();

        self.set_scroll(screen_window.current_line(), screen_window.line_count());

        assert!(self.used_lines <= self.lines);
        assert!(self.used_columns <= self.columns);

        let tl = self.contents_rect(Some(Coordinate::Widget)).top_left();
        let tlx = tl.x();
        let tly = tl.y();
        self.has_blinker = false;

        let image = self.image.as_ref().unwrap();
        let new_img = screen_window.get_image();

        let mut len;

        let mut cf = CharacterColor::default();
        let mut clipboard;
        let mut cr;

        let lines_to_update = self.lines.min(0.max(lines));
        let columns_to_update = self.columns.min(0.max(columns));

        let mut disstr_u = vec![0u16; columns_to_update as usize];
        // The dirty mask indicates which characters need repainting. We also
        // mark surrounding neighbours dirty, in case the character exceeds
        // its cell boundaries
        let mut dirty_mask = vec![false; columns_to_update as usize + 2];
        let mut dirty_region = Rect::default();

        // debugging variable, this records the number of lines that are found to
        // be 'dirty' ( ie. have changed from the old _image to the new _image ) and
        // which therefore need to be repainted
        let mut _dirty_line_count = 0;

        for y in 0..lines_to_update {
            let current_line = &image[(y * self.columns) as usize..];
            let new_line = &mut new_img[(y * columns) as usize..];

            let mut update_line = false;

            for x in 0..columns_to_update as usize {
                if new_line[x] != current_line[x] {
                    dirty_mask[x] = true;
                }
            }

            if !self.resizing {
                let mut x = 0usize;
                while x < columns_to_update as usize {
                    self.has_blinker = self.has_blinker || (new_line[x].rendition & RE_BLINK != 0);

                    // Start drawing if this character or the next one differs.
                    // We also take the next one into account to handle the situation
                    // where characters exceed their cell width.
                    if dirty_mask[x] {
                        let c = new_line[x + 0].character_union.data();
                        if c == 0 {
                            continue;
                        }

                        let mut p = 0;
                        disstr_u[p] = c;
                        p += 1;
                        let line_draw = self.is_line_char(c);
                        let double_width = if x + 1 == columns_to_update as usize {
                            false
                        } else {
                            new_line[x + 1].character_union.data() == 0
                        };
                        cr = new_line[x].rendition;
                        clipboard = new_line[x].background_color;

                        if new_line[x].foreground_color != cf {
                            cf = new_line[x].foreground_color;
                        }

                        let lln = columns_to_update as usize - x;
                        len = 1;
                        while len < lln {
                            let ch = new_line[x + len];
                            if ch.character_union.data() == 0 {
                                continue;
                            }

                            let next_is_double_width = if x + len + 1 == columns_to_update as usize
                            {
                                false
                            } else {
                                new_line[x + len + 1].character_union.data() == 0
                            };

                            if ch.foreground_color != cf
                                || ch.background_color != clipboard
                                || ch.rendition != cr
                                || !dirty_mask[x + len]
                                || self.is_line_char(c) != line_draw
                                || next_is_double_width != double_width
                            {
                                break;
                            }

                            disstr_u[p] = c;
                            p += 1;
                            len += 1;
                        }

                        // let unistr = U16String::from_vec(disstr_u[0..p].to_vec());

                        let save_fixed_font = self.fixed_font;
                        if line_draw {
                            self.fixed_font = false;
                        }
                        if double_width {
                            self.fixed_font = false;
                        }

                        update_line = true;

                        self.fixed_font = save_fixed_font;
                        x += len - 1;
                    }
                    x += 1;
                }
            }

            // both the top and bottom halves of double height _lines must always be
            // redrawn although both top and bottom halves contain the same characters,
            // only the top one is actually drawn.
            if self.line_properties.len() > y as usize {
                update_line =
                    update_line || (self.line_properties[y as usize] & LINE_DOUBLE_HEIGHT != 0);
            }

            // if the characters on the line are different in the old and the new _image
            // then this line must be repainted.
            if update_line {
                _dirty_line_count += 1;
                let dirty_rect = Rect::new(
                    self.left_margin + tlx,
                    self.top_margin + tly + self.font_height * y,
                    self.font_width * columns_to_update,
                    self.font_height,
                );

                dirty_region.or(&dirty_rect);
            }

            new_line[0..columns_to_update as usize]
                .copy_from_slice(&current_line[0..columns_to_update as usize]);
        }

        // if the new _image is smaller than the previous _image, then ensure that the
        // area outside the new _image is cleared
        if lines_to_update < self.used_lines {
            let rect = Rect::new(
                self.left_margin + tlx + columns_to_update * self.font_width,
                self.top_margin + tly,
                self.font_width * (self.used_columns - columns_to_update),
                self.font_height * self.lines,
            );
            dirty_region.or(&rect);
        }
        self.used_lines = lines_to_update;

        if columns_to_update < self.used_columns {
            let rect = Rect::new(
                self.left_margin + tlx + columns_to_update * self.font_width,
                self.top_margin + tly,
                self.font_width * (self.used_columns - columns_to_update),
                self.font_height * self.lines,
            );
            dirty_region.or(&rect);
        }
        self.used_columns = columns_to_update;

        dirty_region.or(&self.input_method_data.previous_preedit_rect);

        // update the parts of the display which have changed
        // TODO: Just update the dirty region
        self.update();

        if self.has_blinker && !self.blink_timer.is_active() {
            self.blink_timer.start(Duration::from_millis(
                TEXT_BLINK_DELAY.load(Ordering::SeqCst),
            ));
        }
        if !self.has_blinker && self.blink_timer.is_active() {
            self.blink_timer.stop();
            self.blinking = false;
        }
    }

    /// Essentially calls [`process_filters()`].
    pub fn update_filters(&mut self) {
        if self.screen_window.is_none() {
            return;
        }

        self.process_filters();
    }

    /// Causes the terminal view to fetch the latest line status flags from the
    /// associated terminal screen ( see [`set_screen_window()`] ).
    pub fn update_line_properties(&mut self) {
        if self.screen_window.is_none() {
            return;
        }

        self.line_properties = unsafe {
            self.screen_window
                .as_ref()
                .unwrap()
                .as_ref()
                .get_line_properties()
        };
    }

    /// Copies the selected text to the clipboard.
    pub fn copy_clipboard(&mut self) {
        if self.screen_window.is_none() {
            return;
        }

        let text = unsafe {
            self.screen_window
                .as_ref()
                .unwrap()
                .as_ref()
                .selected_text(self.preserve_line_breaks);
        };
        // TODO: copy text to clipboard.
    }

    /// Pastes the content of the clipboard into the view.
    pub fn paste_clipboard(&mut self) {
        self.emit_selection(false, false)
    }

    /// Pastes the content of the selection into the view.
    pub fn paste_selection(&mut self) {
        self.emit_selection(true, false)
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
        if self.mouse_marks != uses_mouse {
            self.mouse_marks = uses_mouse;
            self.set_cursor_shape(if self.mouse_marks {
                SystemCursorShape::IBeamCursor
            } else {
                SystemCursorShape::ArrowCursor
            });
            emit!(self.uses_mouse_changed());
        }
    }

    /// See [`set_uses_mouse()`]
    pub fn uses_mouse(&mut self) -> bool {
        self.mouse_marks
    }

    /// Shows a notification that a bell event has occurred in the terminal.
    pub fn bell(&mut self, message: &str) {
        if self.bell_mode == BellMode::NoBell {
            return;
        }

        // limit the rate at which bells can occur
        //...mainly for sound effects where rapid bells in sequence
        // produce a horrible noise
        if self.allow_bell {
            self.allow_bell = false;
            let mut timer = Timer::once();
            connect!(timer, timeout(), self, enable_bell());
            timer.start(Duration::from_millis(500));

            match self.bell_mode {
                BellMode::SystemBeepBell => {
                    // TODO: system beep
                    todo!()
                }
                BellMode::NotifyBell => {
                    emit!(self.notify_bell(), message)
                }
                BellMode::VisualBell => {
                    self.swap_color_table();
                    let mut timer = Timer::once();
                    connect!(timer, timeout(), self, swap_color_table());
                    timer.start(Duration::from_millis(200));
                }
                _ => {}
            }
        }
    }

    /// Sets the background of the view to the specified color.
    /// @see [`set_color_table()`], [`set_foreground_color()`]
    pub fn set_background_color(&mut self, color: Color) {
        self.color_table[DEFAULT_BACK_COLOR as usize].color = color;

        self.set_background(color);
        self.scroll_bar.set_background(color);

        self.update();
    }

    /// Sets the text of the view to the specified color.
    /// @see [`set_color_table()`], [`set_background_color()`]
    pub fn set_foreground_color(&mut self, color: Color) {
        self.color_table[DEFAULT_FORE_COLOR as usize].color = color;
        self.update();
    }

    pub fn selection_changed(&mut self) {
        if self.screen_window.is_none() {
            return;
        }
        emit!(self.copy_avaliable(), unsafe {
            self.screen_window
                .as_ref()
                .unwrap()
                .as_ref()
                .selected_text(false)
                .is_empty()
                == false
        });
    }

    fn scroll_bar_position_changed(&mut self, _value: i32) {
        if self.screen_window.is_none() {
            return;
        }

        let screen_window = unsafe { self.screen_window.as_mut().unwrap().as_mut() };
        screen_window.scroll_to(self.scroll_bar.value());

        // if the thumb has been moved to the bottom of the _scrollBar then set
        // the display to automatically track new output,
        // that is, scroll down automatically to how new _lines as they are added.
        let at_end_of_output = self.scroll_bar.value() == self.scroll_bar.maximum();
        screen_window.set_track_output(at_end_of_output);

        self.update_image();
    }

    fn blink_event(&mut self) {
        if !self.allow_blinking_text {
            return;
        }

        self.blinking = !self.blinking;

        // TODO:  Optimize to only repaint the areas of the widget
        // where there is blinking text
        // rather than repainting the whole widget.
        self.update();
    }

    fn blink_cursor_event(&mut self) {
        self.cursor_blinking = !self.cursor_blinking;
        self.update_cursor();
    }

    /// Renables bell noises and visuals.  Used to disable further bells for a
    /// short period of time after emitting the first in a sequence of bell events.
    fn enable_bell(&mut self) {
        self.allow_bell = true;
    }

    fn swap_color_table(&mut self) {
        let color = self.color_table[1];
        self.color_table[1] = self.color_table[0];
        self.color_table[0] = color;
        self.colors_inverted = !self.colors_inverted;
        self.update();
    }

    fn triple_click_timeout(&mut self) {
        self.possible_triple_click = false;
    }

    ////////////////////////////////////// Private functions. //////////////////////////////////////
    fn font_change(&mut self) {
        let font = self.font();
        let (_, fm) = font.metrics();
        self.font_height = fm.x_height as i32 + self.line_spacing as i32;

        // "Base character width on widest ASCII character. This prevents too wide
        // characters in the presence of double wide (e.g. Japanese) characters."
        // Get the width from representative normal width characters
        let wstring = U16String::from_str(REPCHAR);
        let u16_repchar = wstring.as_slice();
        let mut widths = vec![0f32; u16_repchar.len()];
        font.get_widths(u16_repchar, &mut widths);
        let sum_width: f32 = widths.iter().sum();
        self.font_width = round(sum_width as f64 / u16_repchar.len() as f64);

        self.fixed_font = true;

        let fw = widths[0];
        for i in 1..widths.len() {
            if fw != widths[i] {
                self.fixed_font = false;
                break;
            }
        }

        if self.font_width < 1 {
            self.font_width = 1;
        }

        self.font_ascend = fm.ascent as i32;

        emit!(
            self.changed_font_metrics_signal(),
            (self.font_height, self.font_width)
        );
        self.propagate_size();

        // We will run paint event testing procedure.
        // Although this operation will destroy the original content,
        // the content will be drawn again after the test.
        self.draw_text_test_flag = true;
        self.update();
    }

    fn extend_selection(&mut self, mut pos: Point) {
        if self.screen_window.is_none() {
            return;
        }

        let tl = self.contents_rect(Some(Coordinate::Widget)).top_left();
        let tlx = tl.x();
        let tly = tl.y();
        let scroll = self.scroll_bar.value();

        // we're in the process of moving the mouse with the left button pressed
        // the mouse cursor will kept caught within the bounds of the text in this widget.
        let mut lines_beyond_widget;

        let text_bounds = Rect::new(
            tlx + self.left_margin,
            tly + self.top_margin,
            self.used_columns * self.font_width - 1,
            self.used_lines * self.font_height - 1,
        );

        // Adjust position within text area bounds.
        let old_pos = pos;

        pos.set_x(tmui::tlib::global::bound(
            text_bounds.left(),
            pos.x(),
            text_bounds.right(),
        ));
        pos.set_y(tmui::tlib::global::bound(
            text_bounds.top(),
            pos.y(),
            text_bounds.bottom(),
        ));

        if old_pos.y() > text_bounds.bottom() {
            lines_beyond_widget = (old_pos.y() - text_bounds.bottom()) / self.font_height;
            // Scroll forward
            self.scroll_bar
                .set_value(self.scroll_bar.value() + lines_beyond_widget + 1);
        }
        if old_pos.y() < text_bounds.top() {
            lines_beyond_widget = (text_bounds.top() - old_pos.y()) / self.font_height;
            self.scroll_bar
                .set_value(self.scroll_bar.value() - lines_beyond_widget - 1);
        }

        let (char_line, char_column) = self.get_character_position(pos);

        let mut here = Point::new(char_column, char_line);
        let mut ohere = Point::default();
        let mut i_pnt_sel_corr = self.i_pnt_sel;
        i_pnt_sel_corr.set_y(i_pnt_sel_corr.y() - self.scroll_bar.value());
        let mut pnt_sel_corr = self.pnt_sel;
        pnt_sel_corr.set_y(pnt_sel_corr.y() - self.scroll_bar.value());
        let mut swapping = false;

        if self.word_selection_mode {
            // Extend to word boundaries.
            let mut i;
            let mut sel_class;

            let left_not_right = here.y() < i_pnt_sel_corr.y()
                || (here.y() == i_pnt_sel_corr.y() && here.x() < i_pnt_sel_corr.x());
            let old_left_not_right = pnt_sel_corr.y() < i_pnt_sel_corr.y()
                || (pnt_sel_corr.y() == i_pnt_sel_corr.y()
                    && pnt_sel_corr.x() < i_pnt_sel_corr.x());
            swapping = left_not_right != old_left_not_right;

            // Find left (left_not_right ? from here : from start)
            let mut left = if left_not_right { here } else { i_pnt_sel_corr };
            i = self.loc(left.x(), left.y());
            if i >= 0 && i <= self.image_size {
                sel_class = self.char_class(
                    self.image.as_ref().unwrap()[i as usize]
                        .character_union
                        .data(),
                );

                while (left.x() > 0
                    || (left.y() > 0
                        && self.line_properties[left.y() as usize - 1] & LINE_WRAPPED != 0))
                    && self.char_class(
                        self.image.as_ref().unwrap()[i as usize - 1]
                            .character_union
                            .data(),
                    ) == sel_class
                {
                    i -= 1;
                    if left.x() > 0 {
                        left.set_x(left.x() - 1)
                    } else {
                        left.set_x(self.used_columns - 1);
                        left.set_y(left.y() - 1);
                    }
                }
            }

            // Find right (left_not_right ? from start : from here)
            let mut right = if left_not_right { i_pnt_sel_corr } else { here };
            i = self.loc(right.x(), right.y());
            if i >= 0 && i <= self.image_size {
                sel_class = self.char_class(
                    self.image.as_ref().unwrap()[i as usize]
                        .character_union
                        .data(),
                );
                while (right.x() < self.used_columns - 1
                    || (right.y() < self.used_lines - 1
                        && self.line_properties[right.y() as usize] & LINE_WRAPPED != 0))
                    && self.char_class(
                        self.image.as_ref().unwrap()[i as usize + 1]
                            .character_union
                            .data(),
                    ) == sel_class
                {
                    i += 1;
                    if right.x() < self.used_columns - 1 {
                        right.set_x(right.x() + 1);
                    } else {
                        right.set_x(0);
                        right.set_y(right.y() + 1);
                    }
                }
            }

            // Pick which is start (ohere) and which is extension (here).
            ohere.set_x(ohere.x() + 1);
        }

        if self.line_selection_mode {
            // Extend to complete line.
            let above_not_below = here.y() < i_pnt_sel_corr.y();

            let mut above = if above_not_below {
                here
            } else {
                i_pnt_sel_corr
            };
            let mut below = if above_not_below {
                i_pnt_sel_corr
            } else {
                here
            };

            while above.y() > 0 && self.line_properties[above.y() as usize - 1] & LINE_WRAPPED != 0
            {
                above.set_y(above.y() - 1);
            }
            while below.y() < self.used_lines - 1
                && self.line_properties[below.y() as usize] & LINE_WRAPPED != 0
            {
                below.set_y(below.y() + 1);
            }

            above.set_x(0);
            below.set_x(self.used_columns - 1);

            // Pick which is start (ohere) and which is extension (here)
            if above_not_below {
                here = above;
                ohere = below;
            } else {
                here = below;
                ohere = above;
            }

            let new_sel_begin = Point::new(ohere.x(), ohere.y());
            swapping = !(self.triple_sel_begin == new_sel_begin);
            self.triple_sel_begin = new_sel_begin;

            ohere.set_x(ohere.x() + 1);
        }

        let mut offset = 0;
        if !self.word_selection_mode && !self.line_selection_mode {
            let i;
            let _sel_class;

            let left_not_right = here.y() < i_pnt_sel_corr.y()
                || (here.y() == i_pnt_sel_corr.y() && here.x() < i_pnt_sel_corr.x());
            let old_left_not_right = pnt_sel_corr.y() < i_pnt_sel_corr.y()
                || (pnt_sel_corr.y() == i_pnt_sel_corr.y()
                    && pnt_sel_corr.x() < i_pnt_sel_corr.x());
            swapping = left_not_right != old_left_not_right;

            // Find left (left_not_right ? from here : from start)
            let left = if left_not_right { here } else { i_pnt_sel_corr };

            // Find right (left_not_right ? from start : from here)
            let right = if left_not_right { i_pnt_sel_corr } else { here };

            if right.x() > 0 && !self.column_selection_mode {
                i = self.loc(right.x(), right.y());
                if i >= 0 && i <= self.image_size {
                    _sel_class = self.char_class(
                        self.image.as_ref().unwrap()[i as usize - 1]
                            .character_union
                            .data(),
                    );
                }
            }

            // Pick which is start (ohere) and which is extension (here)
            if left_not_right {
                here = left;
                ohere = right;
                offset = 0;
            } else {
                here = right;
                ohere = left;
                offset = -1;
            }
        }

        if here == pnt_sel_corr && scroll == self.scroll_bar.value() {
            // Not moved.
            return;
        }

        if here == ohere {
            // It's not left, it's not right.
            return;
        }

        let screen_window = unsafe { self.screen_window.as_mut().unwrap().as_mut() };
        if self.act_sel < 2 || swapping {
            if self.column_selection_mode && !self.line_selection_mode && !self.word_selection_mode
            {
                screen_window.set_selection_start(ohere.x(), ohere.y(), true);
            } else {
                screen_window.set_selection_start(ohere.x() - 1 - offset, ohere.y(), false);
            }
        }

        self.act_sel = 2;
        self.pnt_sel = here;
        self.pnt_sel
            .set_y(self.pnt_sel.y() + self.scroll_bar.value());

        if self.column_selection_mode && !self.line_selection_mode && !self.word_selection_mode {
            screen_window.set_selection_end(here.x(), here.y());
        } else {
            screen_window.set_selection_end(here.x() + offset, here.y());
        }
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
    fn char_class(&mut self, ch: u16) -> u16 {
        if ch == b' ' as u16 {
            return b' ' as u16;
        }

        if (ch >= b'0' as u16 && ch <= b'9' as u16)
            || (ch >= b'a' as u16 && ch <= b'z' as u16)
            || (ch >= b'A' as u16 && ch <= b'Z' as u16
                || self.word_characters.contains(ch as u8 as char))
        {
            return b'a' as u16;
        }

        ch
    }

    fn clear_image(&mut self) {
        if self.image.is_none() {
            return;
        }
        // We initialize image[image_size] too. See make_image()
        for i in 0..self.image_size as usize {
            let image = self.image.as_mut().unwrap();
            image[i].character_union.set_data(wch!(' '));
            image[i].foreground_color = CharacterColor::default_foreground();
            image[i].background_color = CharacterColor::default_background();
            image[i].rendition = DEFAULT_RENDITION;
        }
    }

    fn mouse_triple_click_event(&mut self, ev: MouseEvent) {
        if self.screen_window.is_none() {
            return;
        }
        let screen_window = unsafe { self.screen_window.as_mut().unwrap().as_mut() };

        let (char_line, char_column) = self.get_character_position(ev.position().into());
        self.pnt_sel = Point::new(char_column, char_line);

        screen_window.clear_selection();

        self.line_selection_mode = true;
        self.word_selection_mode = false;

        self.act_sel = 2;
        emit!(self.is_busy_selecting(), true);

        while self.pnt_sel.y() > 0
            && self.line_properties[self.pnt_sel.y() as usize - 1] & LINE_WRAPPED != 0
        {
            self.pnt_sel.set_y(self.pnt_sel.y() - 1);
        }

        if self.triple_click_mode == TripleClickMode::SelectForwardsFromCursor {
            // find word boundary start
            let mut i = self.loc(self.pnt_sel.x(), self.pnt_sel.y());
            let sel_class = self.char_class(
                self.image.as_ref().unwrap()[i as usize]
                    .character_union
                    .data(),
            );

            let mut x = self.pnt_sel.x();

            while (x > 0
                || (self.pnt_sel.y() > 0
                    && self.line_properties[self.pnt_sel.y() as usize - 1] & LINE_WRAPPED != 0))
                && self.char_class(
                    self.image.as_ref().unwrap()[i as usize - 1]
                        .character_union
                        .data(),
                ) == sel_class
            {
                i -= 1;
                if x > 0 {
                    x -= 1;
                } else {
                    x = self.columns - 1;
                    self.pnt_sel.set_y(self.pnt_sel.y() - 1);
                }
            }

            screen_window.set_selection_start(x, self.pnt_sel.y(), false);
            self.triple_sel_begin = Point::new(x, self.pnt_sel.y());
        } else if self.triple_click_mode == TripleClickMode::SelectWholeLine {
            screen_window.set_selection_start(0, self.pnt_sel.y(), false);
            self.triple_sel_begin = Point::new(0, self.pnt_sel.y());
        }

        while self.pnt_sel.y() < self.lines - 1
            && self.line_properties[self.pnt_sel.y() as usize] & LINE_WRAPPED != 0
        {
            self.pnt_sel.set_y(self.pnt_sel.y() + 1);
        }

        screen_window.set_selection_end(self.columns - 1, self.pnt_sel.y());

        self.set_selection(screen_window.selected_text(self.preserve_line_breaks));

        self.i_pnt_sel
            .set_y(self.i_pnt_sel.y() + self.scroll_bar.value());
    }

    /// determine the width of this text.
    fn text_width(&self, start_column: i32, length: i32, line: i32) -> i32 {
        if self.image.is_none() {
            return 0;
        }
        let image = self.image.as_ref().unwrap();
        let font = self.font();
        let mut result = 0;
        let mut widths = vec![];
        for column in 0..length {
            font.get_widths(
                &[image[self.loc(start_column + column, line) as usize]
                    .character_union
                    .data()],
                &mut widths,
            );
            let width: f32 = widths.iter().sum();
            result += width as i32;
        }
        result
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
        let left = if self.fixed_font {
            self.font_width * start_column
        } else {
            self.text_width(0, start_column, line)
        };
        let top = self.font_height * line;
        let width = if self.fixed_font {
            self.font_width * length
        } else {
            self.text_width(start_column, length, line)
        };

        Rect::new(
            self.left_margin + top_left_x + left,
            self.top_margin + top_left_y + top,
            width,
            self.font_height,
        )
    }

    /// maps an area in the character image to an area on the widget.
    fn image_to_widget(&self, image_area: &Rect) -> Rect {
        let mut result = Rect::default();
        result.set_left(self.left_margin + self.font_width * image_area.left());
        result.set_top(self.top_margin + self.font_height * image_area.top());
        result.set_width(self.font_width * image_area.width());
        result.set_height(self.font_height * image_area.height());

        result
    }

    /// the area where the preedit string for input methods will be draw.
    fn preedit_rect(&mut self) -> Rect {
        let preedit_length = string_width(self.input_method_data.preedit_string.as_slice());

        if preedit_length == 0 {
            return Rect::default();
        }

        Rect::new(
            self.left_margin + self.font_width * self.cursor_position().x(),
            self.top_margin + self.font_height * self.cursor_position().y(),
            self.font_width * preedit_length,
            self.font_height,
        )
    }

    /// shows a notification window in the middle of the widget indicating the
    /// terminal's current size in columns and lines
    fn show_resize_notification(&self) {
        todo!()
    }

    /// scrolls the image by a number of lines.
    /// 'lines' may be positive ( to scroll the image down )
    /// or negative ( to scroll the image up )
    /// 'region' is the part of the image to scroll - currently only
    /// the top, bottom and height of 'region' are taken into account,
    /// the left and right are ignored.
    fn scroll_image(&mut self, lines: i32, screen_window_region: &Rect) {
        // if the flow control warning is enabled this will interfere with the
        // scrolling optimizations and cause artifacts.  the simple solution here
        // is to just disable the optimization whilst it is visible
        if self.output_suspend_label.visible() {
            return;
        }

        // constrain the region to the display
        // the bottom of the region is capped to the number of lines in the display's
        // internal image - 2, so that the height of 'region' is strictly less
        // than the height of the internal image.
        let mut region = *screen_window_region;
        region.set_bottom(region.bottom().min(self.lines - 2));

        // return if there is nothing to do
        if lines == 0
            || self.image.is_none()
            || region.is_valid()
            || region.top() + lines.abs() >= region.bottom()
            || self.lines <= region.height()
        {
            return;
        }

        // hide terminal size label to prevent it being scrolled.
        if self.resize_widget.visible() {
            self.resize_widget.hide()
        }

        let scroll_bar_width = if self.scroll_bar.visible() {
            self.scroll_bar.size().width()
        } else {
            0
        };
        let scrollbar_content_gap = if scroll_bar_width == 0 { 0 } else { 1 };
        let mut scroll_rect = Rect::default();
        if self.scroll_bar_location == ScrollBarPosition::ScrollBarLeft {
            scroll_rect.set_left(scroll_bar_width + scrollbar_content_gap);
            scroll_rect.set_right(self.size().width());
        } else {
            scroll_rect.set_left(0);
            scroll_rect.set_right(self.size().width() - scroll_bar_width - scrollbar_content_gap);
        }

        let first_char_pos = &self.image.as_ref().unwrap()[(region.top() * self.columns) as usize];
        let last_char_pos =
            &self.image.as_ref().unwrap()[((region.top() + lines.abs()) * self.columns) as usize];

        let top = self.top_margin + (region.top() * self.font_height);
        let lines_to_move = region.height() - lines.abs();
        let bytes_to_move = lines_to_move * self.columns * size_of::<Character>() as i32;

        assert!(lines_to_move > 0);
        assert!(bytes_to_move > 0);

        // Scroll internal image
        if lines > 0 {
            // TODO: memmove
        } else {
            // TODO: memmove
        }
        scroll_rect.set_height(lines_to_move * self.font_height);

        // TODO: Scroll the widget.
    }

    /// shows the multiline prompt
    fn multiline_confirmation(&mut self, text: &str) -> bool {
        todo!()
    }

    fn calc_geometry(&mut self) {
        let size_hint = self.scroll_bar.size_hint();
        self.scroll_bar.resize(size_hint.width(), size_hint.height());
        let contents_rect = self.contents_rect(Some(Coordinate::Widget));

        let scrollbar_width = if self.scroll_bar.visible() {
            self.scroll_bar.size().width()
        } else {
            0
        };

        match self.scroll_bar_location {
            ScrollBarPosition::NoScrollBar => {
                self.left_margin = self.left_base_margin;
                self.content_width = contents_rect.width() - 2 * self.left_base_margin;
            }
            ScrollBarPosition::ScrollBarLeft => {
                self.left_margin = self.left_base_margin + scrollbar_width;
                self.content_width =
                    contents_rect.width() - 2 * self.left_base_margin - scrollbar_width;
                // TODO: ScrollBar move
            }
            ScrollBarPosition::ScrollBarRight => {
                self.left_margin = self.left_base_margin;
                self.content_width =
                    contents_rect.width() - 2 * self.left_base_margin - scrollbar_width;
                // TODO: ScrollBar move
            }
        }

        self.top_margin = self.top_base_margin;
        self.content_height = contents_rect.height() - 2 * self.top_base_margin + 1;

        if !self.is_fixed_size {
            // ensure that display is always at least one column wide
            self.columns = (self.content_width / self.font_width).max(1);
            self.used_columns = self.used_columns.min(self.columns);

            // ensure that display is always at least one line high
            self.lines = (self.content_height / self.font_height).max(1);
            self.used_lines = self.used_lines.min(self.lines);
        }
    }
    fn propagate_size(&mut self) {
        if self.is_fixed_size {
            // TODO:
            todo!()
        }
        if self.image.is_some() {
            self.update_image_size();
        }
    }
    fn update_image_size(&mut self) {
        let old_line = self.lines;
        let old_col = self.columns;

        let old_image = self.make_image();

        // copy the old image to reduce flicker
        let mlines = old_line.min(self.lines);
        let mcolumns = old_col.min(self.columns);

        if old_image.is_some() {
            for line in 0..mlines {
                let dist_start = (self.columns * line) as usize;
                let dist_end = dist_start + mcolumns as usize;
                let src_start = (old_col * line) as usize;
                let src_end = src_start + mcolumns as usize;
                self.image.as_mut().unwrap()[dist_start..dist_end]
                    .copy_from_slice(&old_image.as_ref().unwrap()[src_start..src_end]);
            }
        }

        if self.screen_window.is_some() {
            unsafe {
                self.screen_window
                    .as_mut()
                    .unwrap()
                    .as_mut()
                    .set_window_lines(self.lines)
            };
        }

        self.resizing = (old_line != self.lines) || (old_col != self.columns);

        if self.resizing {
            self.show_resize_notification();
            emit!(
                self.changed_content_size_signal(),
                self.content_height,
                self.content_width
            );
        }

        self.resizing = false
    }
    /// Make new image and return the new one.
    fn make_image(&mut self) -> Option<Vec<Character>> {
        self.calc_geometry();

        // confirm that array will be of non-zero size, since the painting code
        // assumes a non-zero array length
        assert!(self.lines > 0 && self.columns > 0);
        assert!(self.used_lines <= self.lines && self.used_columns <= self.columns);

        self.image_size = self.lines * self.columns;

        // We over-commit one character so that we can be more relaxed in dealing with
        // certain boundary conditions: _image[_imageSize] is a valid but unused position.
        self.image
            .replace(vec![Character::default(); (self.image_size + 1) as usize])
    }

    /// returns a region covering all of the areas of the widget which contain a hotspot.
    fn hotspot_region(&self) -> Rect {
        let mut region = Rect::default();
        let hotspots = self.filter_chain.hotspots();

        hotspots.iter().for_each(|hotspot| {
            let mut r = Rect::default();
            if hotspot.start_line() == hotspot.end_line() {
                r.set_left(hotspot.start_column());
                r.set_top(hotspot.start_line());
                r.set_right(hotspot.end_column());
                r.set_bottom(hotspot.end_line());
                region.or(&self.image_to_widget(&r))
            } else {
                r.set_left(hotspot.start_column());
                r.set_top(hotspot.start_line());
                r.set_right(self.columns);
                r.set_bottom(hotspot.start_line());
                region.or(&self.image_to_widget(&r));

                for line in hotspot.start_line() + 1..hotspot.end_line() {
                    r.set_left(0);
                    r.set_top(line);
                    r.set_right(self.columns);
                    r.set_bottom(line);
                    region.or(&self.image_to_widget(&r));
                }

                r.set_left(0);
                r.set_top(hotspot.end_line());
                r.set_right(hotspot.end_column());
                r.set_bottom(hotspot.end_line());
                region.or(&self.image_to_widget(&r));
            }
        });

        region
    }

    /// returns the position of the cursor in columns and lines.
    fn cursor_position(&self) -> Point {
        if self.screen_window.is_some() {
            unsafe {
                self.screen_window
                    .as_ref()
                    .unwrap()
                    .as_ref()
                    .cursor_position()
            }
        } else {
            Point::new(0, 0)
        }
    }

    /// redraws the cursor.
    fn update_cursor(&mut self) {
        let rect = Rect::from_point_size(self.cursor_position(), Size::new(1, 1));
        let cursor_rect = self.image_to_widget(&rect);
        // TODO: repaint()?
    }

    fn handle_shortcut_override_event(&mut self, event: KeyEvent) {
        todo!()
    }

    fn is_line_char(&self, c: wchar_t) -> bool {
        self.draw_line_chars && ((c & 0xFF80) == 0x2500)
    }
    fn is_line_char_string(&self, string: &U16String) -> bool {
        string.len() > 0 && self.is_line_char(string.as_slice()[0])
    }
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////
/// Predefine
//////////////////////////////////////////////////////////////////////////////////////////////////////////
static ANTIALIAS_TEXT: AtomicBool = AtomicBool::new(true);
static HAVE_TRANSPARENCY: AtomicBool = AtomicBool::new(true);
static TEXT_BLINK_DELAY: AtomicU64 = AtomicU64::new(500);

const REPCHAR: &'static str = "
ABCDEFGHIJKLMNOPQRSTUVWXYZ
abcdefgjijklmnopqrstuvwxyz
0123456789./+@
";

const LTR_OVERRIDE_CHAR: u16 = 0x202D;

#[inline]
pub fn bound(min: f64, val: f64, max: f64) -> f64 {
    assert!(max >= min);
    min.max(max.min(val))
}

#[inline]
pub fn round(d: f64) -> i32 {
    if d >= 0. {
        (d + 0.5) as i32
    } else {
        (d - 0.5) as i32
    }
}

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
    if to_draw & TopL as u32 != 0 {
        painter.draw_line(cx - 1, y, cx - 1, cy - 2);
    }
    if to_draw & TopC as u32 != 0 {
        painter.draw_line(cx, y, cx, cy - 2);
    }
    if to_draw & TopR as u32 != 0 {
        painter.draw_line(cx + 1, y, cx + 1, cy - 2);
    }

    // Bot lines:
    if to_draw & BotL as u32 != 0 {
        painter.draw_line(cx - 1, cy + 2, cx - 1, ey);
    }
    if to_draw & BotC as u32 != 0 {
        painter.draw_line(cx, cy + 2, cx, ey);
    }
    if to_draw & BotR as u32 != 0 {
        painter.draw_line(cx + 1, cy + 2, cx + 1, ey);
    }

    // Left lines:
    if to_draw & LeftT as u32 != 0 {
        painter.draw_line(x, cy - 1, cx - 2, cy - 1);
    }
    if to_draw & LeftC as u32 != 0 {
        painter.draw_line(x, cy, cx - 2, cy);
    }
    if to_draw & LeftB as u32 != 0 {
        painter.draw_line(x, cy + 1, cx - 2, cy + 1);
    }

    // Right lines:
    if to_draw & RightT as u32 != 0 {
        painter.draw_line(cx + 2, cy - 1, ex, cy - 1);
    }
    if to_draw & RightC as u32 != 0 {
        painter.draw_line(cx + 2, cy, ex, cy);
    }
    if to_draw & RightB as u32 != 0 {
        painter.draw_line(cx + 2, cy + 1, ex, cy + 1);
    }

    // Intersection points.
    if to_draw & Int11 as u32 != 0 {
        painter.draw_point(cx - 1, cy - 1);
    }
    if to_draw & Int12 as u32 != 0 {
        painter.draw_point(cx, cy - 1);
    }
    if to_draw & Int13 as u32 != 0 {
        painter.draw_point(cx + 1, cy - 1);
    }

    if to_draw & Int21 as u32 != 0 {
        painter.draw_point(cx - 1, cy);
    }
    if to_draw & Int22 as u32 != 0 {
        painter.draw_point(cx, cy);
    }
    if to_draw & Int23 as u32 != 0 {
        painter.draw_point(cx + 1, cy);
    }

    if to_draw & Int31 as u32 != 0 {
        painter.draw_point(cx - 1, cy + 1);
    }
    if to_draw & Int32 as u32 != 0 {
        painter.draw_point(cx, cy + 1);
    }
    if to_draw & Int33 as u32 != 0 {
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

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_regular_replace() {
        let str = "hello\r";
        assert_eq!(REGULAR_EXPRESSION.replace(str, ""), "hello");
    }
}