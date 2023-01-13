#![allow(dead_code)]
use super::screen_window::ScreenWindow;
use crate::tools::{
    character::{Character, LineProperty},
    character_color::{ColorEntry, TABLE_COLORS},
    filter::TerminalImageFilterChain,
};
use std::{
    ptr::NonNull,
    sync::atomic::{AtomicBool, AtomicI32},
};
use tmui::{
    graphics::{
        figure::{Color, Size},
        painter::Painter,
    },
    prelude::*,
    tlib::object::{ObjectImpl, ObjectSubclass},
    widget::WidgetImpl,
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
    fn paint(&self, mut painter: Painter) {
        painter.set_antialiasing();
    }
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////
/// TerminalView Implements
//////////////////////////////////////////////////////////////////////////////////////////////////////////
impl TerminalView {
    #[inline]
    pub fn loc(&self, x: i32, y: i32) -> i32 {
        y * self.columns + x
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
            0x4D => { // BOX DRAWINGS HEAVY DOUBLE DASH HORIZONTAL
                painter.draw_line(x, cy - 1, cx - x_half_gap - 1, cy - 1);
                painter.draw_line(x, cy + 1, cx - x_half_gap - 1, cy + 1);
                painter.draw_line(cx + x_half_gap, cy - 1, ex, cy - 1);
                painter.draw_line(cx + x_half_gap, cy + 1, ex, cy + 1);
            }
            0x4C => { // BOX DRAWINGS LIGHT DOUBLE DASH HORIZONTAL
                painter.draw_line(x, cy, cx - x_half_gap - 1, cy);
                painter.draw_line(cx + x_half_gap, cy, ex, cy);
            }
            0x4F => { // BOX DRAWINGS HEAVY DOUBLE DASH VERTICAL
                painter.draw_line(cx - 1, y, cx - 1, cy - y_half_gap - 1);
                painter.draw_line(cx + 1, y, cx + 1, cy - y_half_gap - 1);
                painter.draw_line(cx - 1, cy + y_half_gap, cx - 1, ey);
                painter.draw_line(cx + 1, cy + y_half_gap, cx + 1, ey);
            }
            0x4E => { // BOX DRAWINGS LIGHT DOUBLE DASH VERTICAL
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
            0x6D => { // BOX DRAWINGS LIGHT ARC DOWN AND RIGHT
                painter.draw_line(cx, cy + r, cx, ey);
                painter.draw_line(cx + r, cy, ex, cy);
                painter.draw_arc(cx, cy, d, d, 90 * 16, 90 * 16);
            }
            0x6E => { // BOX DRAWINGS LIGHT ARC DOWN AND LEFT
                painter.draw_line(cx, cy + r, cx, ey);
                painter.draw_line(x, cy, cx - r, cy);
                painter.draw_arc(cx - d, cy, d, d, 0 * 16, 90 * 16);
            }
            0x6F => { // BOX DRAWINGS LIGHT ARC UP AND LEFT
                painter.draw_line(cx, y, cx, cy - r);
                painter.draw_line(x, cy, cx - r, cy);
                painter.draw_arc(cx - d, cy - d, d, d, 270 * 16, 90 * 16);
            }
            0x70 => { // BOX DRAWINGS LIGHT ARC UP AND RIGHT
                painter.draw_line(cx, y, cx, cy - r);
                painter.draw_line(cx + r, cy, ex, cy);
                painter.draw_arc(cx, cy - d, d, d, 180 * 16, 90 * 16);
            }
            _ => {}
        }

    // Diagonals
    } else if 0x71 <= code && code <= 0x73 {
        match code {
            0x71 => { // BOX DRAWINGS LIGHT DIAGONAL UPPER RIGHT TO LOWER LEFT
                painter.draw_line(ex, y, x, ey);
            }
            0x72 => { // BOX DRAWINGS LIGHT DIAGONAL UPPER LEFT TO LOWER RIGHT
                painter.draw_line(x, y, ex, ey);
            }
            0x73 => { // BOX DRAWINGS LIGHT DIAGONAL CROSS
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
