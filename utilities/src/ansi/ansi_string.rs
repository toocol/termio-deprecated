#![allow(dead_code)]
use super::escape_sequence::*;
use crate::util::string_const::*;

/// Building syled string texts with [Ansi Escape Code Sequence](https://gist.github.com/Joezeo/ce688cf42636376650ead73266256336) for terminal.  
///
/// ### Functions:
/// - Change the text **foreground/background** color.
/// - Set/Reset **bold/underline/italic/blinking/strikethrough** style mode.
/// - Move the cursor position to display inputing text on terminal.
/// ### Example
/// ```
/// use utilities::ansi::AnsiString;
///
/// let mut ansi_string = AnsiString::new();
/// ansi_string.foreground_256(45) // Change the foreground color to 45(256-Color).
///             // Append text, and the text "Hello World!" will display in foreground color 45(256-color).
///             .append("Hello World!")
///             // Clear the foreground color.
///             .de_foreground()
///             // Change the background color to (12, 12, 12) (RGB-Color).
///             .background_rgb(12, 12, 12)
///             // Set text style bold.
///             .bold()
///             // Set text tyle italic.
///             .italic()
///             // Append text, and the text "Hello You!" will display in background color (12,12,12)(RGB-color), bold and italic.
///             .append("Hello you!")
///             // Clear all the style mode (foreground/background/bold/italic...)
///             .clear_style();
/// println!("{}", ansi_string.to_string());
/// ```
pub struct AnsiString {
    builder: String,
    bg_256: i32,
    fg_256: i32,
    bg_r: i32,
    bg_g: i32,
    bg_b: i32,
    fg_r: i32,
    fg_g: i32,
    fg_b: i32,
}

impl AnsiString {
    pub fn new() -> Self {
        AnsiString {
            builder: String::new(),
            bg_256: -1,
            fg_256: -1,
            bg_r: -1,
            bg_g: -1,
            bg_b: -1,
            fg_r: -1,
            fg_g: -1,
            fg_b: -1,
        }
    }

    pub fn to_string(&self) -> &str {
        self.builder.as_str()
    }

    pub fn len(&self) -> usize {
        self.builder.len()
    }

    pub fn foreground_256(&mut self, color: i32) -> &mut Self {
        if color < 0 || color > 255 {
            return self;
        }
        self.fg_r = -1;
        self.fg_g = -1;
        self.fg_b = -1;
        self.fg_256 = color;
        self
    }

    pub fn background_256(&mut self, color: i32) -> &mut Self {
        if color < 0 || color > 255 {
            return self;
        }
        self.bg_r = -1;
        self.bg_g = -1;
        self.bg_b = -1;
        self.bg_256 = color;
        self
    }

    pub fn foreground_rgb(&mut self, r: i32, g: i32, b: i32) -> &mut Self {
        if r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255 {
            return self;
        }
        self.fg_r = r;
        self.fg_g = g;
        self.fg_b = b;
        self.fg_256 = -1;
        self
    }

    pub fn background_rgb(&mut self, r: i32, g: i32, b: i32) -> &mut Self {
        if r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255 {
            return self;
        }
        self.bg_r = r;
        self.bg_g = g;
        self.bg_b = b;
        self.bg_256 = -1;
        self
    }

    fn de_foreground(&mut self) -> &mut Self {
        self.fg_256 = -1;
        self.fg_r = -1;
        self.fg_g = -1;
        self.fg_b = -1;
        self
    }

    fn de_background(&mut self) -> &mut Self {
        self.bg_256 = -1;
        self.bg_r = -1;
        self.bg_g = -1;
        self.bg_b = -1;
        self
    }

    pub fn clear_style(&mut self) -> &mut Self {
        self.builder.push_str(ESC0M);
        self.de_background().de_foreground()
    }

    fn fill_color(&self, str: &str) -> String {
        let mut filled = String::from(str);
        if self.fg_256 != -1 {
            filled = ColorHelper::foreground_256(&filled, self.fg_256);
        }
        if self.bg_256 != -1 {
            filled = ColorHelper::background_256(&filled, self.bg_256);
        }
        if self.fg_r != -1 && self.fg_g != -1 && self.fg_b != -1 {
            filled = ColorHelper::foreground_rgb(&filled, self.fg_r, self.fg_g, self.fg_b);
        }
        if self.bg_r != -1 && self.bg_g != -1 && self.bg_b != -1 {
            filled = ColorHelper::background_rgb(&filled, self.bg_r, self.bg_g, self.bg_b);
        }
        filled
    }

    pub fn append(&mut self, str: &str) -> &mut Self {
        if str == "" {
            return self;
        }
        self.builder.push_str(self.fill_color(str).as_str());
        self
    }

    pub fn append_with_cursor(&mut self, str: &str, line: i32, column: i32) -> &mut Self {
        if str == "" {
            return self;
        }
        let changed = CursorPositionHelper::cursor_move(str, line, column);
        self.builder.push_str(changed.as_str());
        self
    }

    pub fn append_char(&mut self, ch: char) -> &mut Self {
        self.append(ch.to_string().as_str());
        self
    }

    pub fn append_i8(&mut self, val: i8) -> &mut Self {
        self.append(val.to_string().as_str());
        self
    }

    pub fn append_u8(&mut self, val: u8) -> &mut Self {
        self.append(val.to_string().as_str());
        self
    }

    pub fn append_i16(&mut self, val: i16) -> &mut Self {
        self.append(val.to_string().as_str());
        self
    }

    pub fn append_u16(&mut self, val: u16) -> &mut Self {
        self.append(val.to_string().as_str());
        self
    }

    pub fn append_i32(&mut self, val: i32) -> &mut Self {
        self.append(val.to_string().as_str());
        self
    }

    pub fn append_u32(&mut self, val: u32) -> &mut Self {
        self.append(val.to_string().as_str());
        self
    }

    pub fn append_i64(&mut self, val: i64) -> &mut Self {
        self.append(val.to_string().as_str());
        self
    }

    pub fn append_u64(&mut self, val: u64) -> &mut Self {
        self.append(val.to_string().as_str());
        self
    }

    pub fn append_f32(&mut self, val: f32) -> &mut Self {
        self.append(val.to_string().as_str());
        self
    }

    pub fn append_f64(&mut self, val: f64) -> &mut Self {
        self.append(val.to_string().as_str());
        self
    }

    pub fn append_bool(&mut self, val: bool) -> &mut Self {
        self.append(val.to_string().as_str());
        self
    }

    pub fn bold(&mut self) -> &mut Self {
        self.builder.push_str(ESC1M);
        self
    }

    pub fn de_bold(&mut self) -> &mut Self {
        self.builder.push_str(ESC22M);
        self
    }

    pub fn italic(&mut self) -> &mut Self {
        self.builder.push_str(ESC3M);
        self
    }

    pub fn de_italic(&mut self) -> &mut Self {
        self.builder.push_str(ESC23M);
        self
    }

    pub fn underline(&mut self) -> &mut Self {
        self.builder.push_str(ESC4M);
        self
    }

    pub fn de_underline(&mut self) -> &mut Self {
        self.builder.push_str(ESC24M);
        self
    }

    pub fn blinking(&mut self) -> &mut Self {
        self.builder.push_str(ESC5M);
        self
    }

    pub fn de_blinking(&mut self) -> &mut Self {
        self.builder.push_str(ESC25M);
        self
    }

    pub fn strikethrough(&mut self) -> &Self {
        self.builder.push_str(ESC9M);
        self
    }

    pub fn de_strikethrough(&mut self) -> &Self {
        self.builder.push_str(ESC29M);
        self
    }

    pub fn crlf(&mut self) -> &Self {
        self.builder.push_str(CRLF);
        self
    }

    pub fn tab(&mut self) -> &Self {
        self.builder.push_str(TAB);
        self
    }

    pub fn space(&mut self) -> &Self {
        self.builder.push_str(SPACE);
        self
    }

    pub fn space_in(&mut self, cnt: usize) -> &Self {
        self.builder.push_str(SPACE.repeat(cnt).as_str());
        self
    }

    pub fn clear_str(&mut self) -> &Self {
        self.builder.clear();
        self
    }
}

struct ColorHelper {}
impl ColorHelper {
    // 256-Color mode
    fn foreground_256(msg: &str, color: i32) -> String {
        format!("\u{001b}[38;5;{}m{}", color, msg)
    }
    fn background_256(msg: &str, color: i32) -> String {
        format!("\u{001b}[48;5;{}m{}", color, msg)
    }

    // RGB-color mode
    fn foreground_rgb(msg: &str, r: i32, g: i32, b: i32) -> String {
        format!("\u{001b}[38;2;{};{};{}m{}", r, g, b, msg)
    }
    fn background_rgb(msg: &str, r: i32, g: i32, b: i32) -> String {
        format!("\u{001b}[48;2;{};{};{}m{}", r, g, b, msg)
    }
}

struct CursorPositionHelper {}
impl CursorPositionHelper {
    fn cursor_move(msg: &str, line: i32, column: i32) -> String {
        format!("\u{001b}[{};{}H{}", line, column, msg)
    }
}