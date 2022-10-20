#![allow(dead_code)]
const ESC0M: &'static str = "\u{001b}[0m";
const ESC1M: &'static str = "\u{001b}[1m";
const ESC2M: &'static str = "\u{001b}[2m";
const ESC3M: &'static str = "\u{001b}[3m";
const ESC4M: &'static str = "\u{001b}[4m";
const ESC5M: &'static str = "\u{001b}[5m";
const ESC7M: &'static str = "\u{001b}[7m";
const ESC8M: &'static str = "\u{001b}[8m";
const ESC9M: &'static str = "\u{001b}[9m";
const ESC22M: &'static str = "\u{001b}[22m";
const ESC23M: &'static str = "\u{001b}[23m";
const ESC24M: &'static str = "\u{001b}[24m";
const ESC25M: &'static str = "\u{001b}[25m";
const ESC27M: &'static str = "\u{001b}[27m";
const ESC28M: &'static str = "\u{001b}[28m";
const ESC29M: &'static str = "\u{001b}[29m";
const ESCJ: &'static str = "\u{001b}[J";
const ESC0J: &'static str = "\u{001b}[0J";
const ESC1J: &'static str = "\u{001b}[1J";
const ESC2J: &'static str = "\u{001b}[2J";
const ESC3J: &'static str = "\u{001b}[3J";
const ESCK: &'static str = "\u{001b}[K";
const ESC0K: &'static str = "\u{001b}[0K";
const ESC1K: &'static str = "\u{001b}[1K";
const ESC2K: &'static str = "\u{001b}[2K";
const ESCH: &'static str = "\u{001b}[H";

pub struct AnsiString {
    builder: String,
    color_mode: ColorMode,
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
            color_mode: ColorMode::Color256,
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
        self.color_mode = ColorMode::Color256;
        self.fg_256 = color;
        self
    }

    pub fn background_256(&mut self, color: i32) -> &mut Self {
        if color < 0 || color > 255 {
            return self;
        }
        self.color_mode = ColorMode::Color256;
        self.bg_256 = color;
        self
    }

    pub fn foreground_rgb(&mut self, r: i32, g: i32, b: i32) -> &mut Self {
        if r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255 {
            return self;
        }
        self.color_mode = ColorMode::ColorRgb;
        self.fg_r = r;
        self.fg_g = g;
        self.fg_b = b;
        self
    }

    pub fn background_rgb(&mut self, r: i32, g: i32, b: i32) -> &mut Self {
        if r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255 {
            return self;
        }
        self.color_mode = ColorMode::ColorRgb;
        self.bg_r = r;
        self.bg_g = g;
        self.bg_b = b;
        self
    }

    pub fn de_foreground(&mut self) -> &mut Self {
        self.fg_256 = -1;
        self.fg_r = -1;
        self.fg_g = -1;
        self.fg_b = -1;
        self
    }

    pub fn de_background(&mut self) -> &mut Self {
        self.bg_256 = -1;
        self.bg_r = -1;
        self.bg_g = -1;
        self.bg_b = -1;
        self
    }

    pub fn clear_color(&mut self) -> &mut Self {
        self.de_background().de_foreground()
    }

    fn fill_color(&self, str: &str) -> String {
        let mut filled = String::from(str);
        if self.color_mode == ColorMode::Color256 {
            if self.fg_256 != -1 {
                filled = ColorHelper::foreground_256(&filled, self.fg_256);
            }
            if self.bg_256 != -1 {
                filled = ColorHelper::background_256(&filled, self.bg_256);
            }
        } else if self.color_mode == ColorMode::ColorRgb {
            if self.fg_r != -1 && self.fg_g != -1 && self.fg_b != -1 {
                filled = ColorHelper::foreground_rgb(&filled, self.fg_r, self.fg_g, self.fg_b);
            }
            if self.bg_r != -1 && self.bg_g != -1 && self.bg_b != -1 {
                filled = ColorHelper::background_rgb(&filled, self.fg_r, self.fg_g, self.fg_b);
            }
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
}

#[derive(PartialEq, Eq)]
enum ColorMode {
    Color256,
    ColorRgb,
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
