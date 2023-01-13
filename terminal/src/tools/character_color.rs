#![allow(dead_code)]
use tmui::graphics::figure::Color;

///  Specifies the weight to use when drawing text with this color.
#[derive(Default, Debug, PartialEq, Eq, Clone, Copy)]
pub enum FontWeight {
    /// Always draw text in this color with a bold weight.
    Bold,
    /// Always draw text in this color with a normal weight.
    #[default]
    Normal,
    /// Use the current font weight set by the terminal application.
    /// This is the default behavior.
    UseCurrentFormat,
}

/// An entry in a terminal display's color palette.
///
/// A color palette is an array of 16 ColorEntry instances which map
/// system color indexes (from 0 to 15) into actual colors.
///
/// Each entry can be set as bold, in which case any text
/// drawn using the color should be drawn in bold.
///
/// Each entry can also be transparent, in which case the terminal
/// display should avoid drawing the background for any characters
/// using the entry as a background.
#[derive(Default, Debug, PartialEq, Eq, Clone, Copy)]
pub struct ColorEntry {
    /// The color value of this entry for display.
    pub color: Color,
    /// If true character backgrounds using this color should be transparent.
    /// This is not applicable when the color is used to render text.
    pub transparent: bool,
    /// Constructs a new color palette entry with an undefined color, and
    /// with the transparent and bold flags set to false.
    pub font_weight: FontWeight,
}

impl ColorEntry {
    /// Constructs a new color palette entry.
    ///
    /// @param `c` The color value for this entry.
    /// @param `tr` Specifies that the color should be transparent when used as a background color.
    /// @param `weight` Specifies the font weight to use when drawing text with this color.
    pub fn new(c: Color, tr: bool, weight: Option<FontWeight>) -> Self {
        let weight = if weight.is_some() {
            weight.unwrap()
        } else {
            FontWeight::UseCurrentFormat
        };

        ColorEntry {
            color: c,
            transparent: tr,
            font_weight: weight,
        }
    }
}
impl Into<ColorEntry> for (Color, bool) {
    fn into(self) -> ColorEntry {
        ColorEntry {
            color: self.0,
            transparent: self.1,
            font_weight: FontWeight::UseCurrentFormat,
        }
    }
}
impl Into<ColorEntry> for (Color, bool, FontWeight) {
    fn into(self) -> ColorEntry {
        ColorEntry {
            color: self.0,
            transparent: self.1,
            font_weight: self.2,
        }
    }
}

///////////////// Attributed Character Representations
pub const COLOR_SPACE_UNDEFINED: u8 = 0;
pub const COLOR_SPACE_DEFAULT: u8 = 1;
pub const COLOR_SPACE_SYSTEM: u8 = 2;
pub const COLOR_SPACE_256: u8 = 3;
pub const COLOR_SPACE_RGB: u8 = 4;

/// CharacterColor is a union of the various color spaces.
///
/// Assignment is as follows:
///
///   Type  - Space       - Values
///
///   0     - Undefined   - u:  0,      v:0        w:0
///   1     - Default     - u:  0..1    v:intense  w:0
///   2     - System      - u:  0..7    v:intense  w:0
///   3     - Index(256)  - u: 16..255  v:0        w:0
///   4     - RGB         - u:  0..255  v:0..256   w:0..256
///
///   Default colour space has two separate colours, namely
///   default foreground and default background color.
#[derive(Debug, Default, PartialEq, Eq, Clone, Copy)]
pub struct CharacterColor {
    pub color_space: u8,

    // bytes storing the character color
    pub u: u8,
    pub v: u8,
    pub w: u8,
}

impl CharacterColor {
    /// Create the default foreground character color.
    pub fn default_foreground() -> Self {
        Self::new(COLOR_SPACE_DEFAULT, DEFAULT_FORE_COLOR)
    }

    /// Create the default background character color.
    pub fn default_background() -> Self {
        Self::new(COLOR_SPACE_DEFAULT, DEFAULT_BACK_COLOR)
    }

    /// Constructs a new CharacterColor whoose color and color space are undefined.
    pub fn empty() -> Self {
        Self {
            color_space: COLOR_SPACE_UNDEFINED,
            u: 0,
            v: 0,
            w: 0,
        }
    }

    #[allow(unused_assignments)]
    /// Constructs a new CharacterColor using the specified @p colorSpace and with
    /// color value @p co
    ///
    /// The meaning of @p co depends on the @p colorSpace used.
    pub fn new(color_space: u8, co: u32) -> Self {
        let mut color_space = color_space;
        let mut u = 0;
        let mut v = 0;
        let mut w = 0;
        match color_space {
            COLOR_SPACE_DEFAULT => u = (co & 1) as u8,
            COLOR_SPACE_SYSTEM => {
                u = (co & 7) as u8;
                v = ((co >> 3) & 1) as u8;
            }
            COLOR_SPACE_256 => u = (co & 255) as u8,
            COLOR_SPACE_RGB => {
                u = (co >> 16) as u8;
                v = (co >> 8) as u8;
                w = co as u8;
            }
            _ => color_space = COLOR_SPACE_UNDEFINED,
        };
        Self {
            color_space,
            u,
            v,
            w,
        }
    }

    /// Returns true if this character color entry is valid.
    #[inline]
    pub fn is_valid(&self) -> bool {
        self.color_space != COLOR_SPACE_UNDEFINED
    }

    /// Set the value of this color from a normal system color to the corresponding
    /// intensive system color if it's not already an intensive system color.
    ///
    /// This is only applicable if the color is using the COLOR_SPACE_DEFAULT or
    /// COLOR_SPACE_SYSTEM color spaces.
    #[inline]
    pub fn set_intensive(&mut self) {
        if self.color_space == COLOR_SPACE_SYSTEM || self.color_space == COLOR_SPACE_DEFAULT {
            self.v = 1;
        }
    }

    #[inline]
    pub fn change_color(&mut self, color_space: u8, co: i32) {
        self.color_space = color_space;
        self.u = 0;
        self.v = 0;
        self.w = 0;

        match color_space {
            COLOR_SPACE_DEFAULT => self.u = (co & 1) as u8,
            COLOR_SPACE_SYSTEM => {
                self.u = (co & 7) as u8;
                self.v = (co >> 3) as u8 & 1;
            }
            COLOR_SPACE_256 => self.u = (co & 255) as u8,
            COLOR_SPACE_RGB => {
                self.u = (co >> 16) as u8;
                self.v = (co >> 8) as u8;
                self.w = co as u8;
            }
            _ => self.color_space = COLOR_SPACE_UNDEFINED,
        }
    }

    #[inline]
    pub fn change_color_inter(&mut self, color: &CharacterColor) {
        self.color_space = color.color_space;
        self.u = color.u;
        self.v = color.v;
        self.w = color.w;
    }

    #[inline]
    pub fn color(&self, palette: &[ColorEntry]) -> Color {
        match self.color_space {
            COLOR_SPACE_DEFAULT => {
                palette[self.u as usize + 0 + if self.v > 0 { BASE_COLORS } else { 0 }].color
            }
            COLOR_SPACE_SYSTEM => {
                palette[self.u as usize + 2 + if self.v > 0 { BASE_COLORS } else { 0 }].color
            }
            COLOR_SPACE_256 => Self::color_256(self.u, palette),
            COLOR_SPACE_RGB => Color::from_rgb(self.u, self.v, self.w),
            COLOR_SPACE_UNDEFINED => Color::new(),
            _ => unimplemented!(),
        }
    }

    #[inline]
    pub fn color_256(mut u: u8, palette: &[ColorEntry]) -> Color {
        // 0..16: Sytem colors.
        if u < 8 {
            return palette[u as usize + 2].color;
        }
        u -= 8;
        if u < 8 {
            return palette[u as usize + 2 + BASE_COLORS].color;
        }
        u -= 8;

        // 16..231: 6x6x6 rgb color cube
        if u < 216 {
            return Color::from_rgb(
                if (u / 36) % 6 > 0 {
                    40 * ((u / 36) % 6) + 55
                } else {
                    0
                },
                if (u / 6) % 6 > 0 {
                    40 * ((u / 6) % 6) + 55
                } else {
                    0
                },
                if (u / 1) % 6 > 0 {
                    40 * ((u / 1) % 6) + 55
                } else {
                    0
                },
            );
        }
        u -= 216;

        // 232..255: gray, leaving out black and white
        let gray = u * 10 + 8;
        Color::from_rgb(gray, gray, gray)
    }
}

//// Colors
pub const BASE_COLORS: usize = 10;
pub const INTENSITIES: usize = 2;
pub const TABLE_COLORS: usize = INTENSITIES * BASE_COLORS;

pub const DEFAULT_FORE_COLOR: u32 = 0;
pub const DEFAULT_BACK_COLOR: u32 = 1;

pub const VT100_GRAPHICS:[u16; 32] = [
    // 0/8     1/9    2/10    3/11    4/12    5/13    6/14    7/15
    0x0020, 0x25C6, 0x2592, 0x2409, 0x240c, 0x240d, 0x240a, 0x00b0,
    0x00b1, 0x2424, 0x240b, 0x2518, 0x2510, 0x250c, 0x2514, 0x253c,
    0xF800, 0xF801, 0x2500, 0xF803, 0xF804, 0x251c, 0x2524, 0x2534,
    0x252c, 0x2502, 0x2264, 0x2265, 0x03C0, 0x2260, 0x00A3, 0x00b7
];

/// A standard set of colors using black text on a white background.
pub const BASE_COLOR_TABLE: [ColorEntry; TABLE_COLORS] = [
    // The following are almost IBM standard color codes, with some slight
    // gamma correction for the dim colors to compensate for bright X screens.
    // It contains the 8 ansiterm/xterm colors in 2 intensities.

    ////// Normal color
    ColorEntry {
        color: Color {
            r: 0x00,
            g: 0x00,
            b: 0x00,
            a: 0xFF,
            valid: true,
        },
        transparent: false,
        font_weight: FontWeight::UseCurrentFormat,
    }, // Dfore
    ColorEntry {
        color: Color {
            r: 0xB2,
            g: 0xB2,
            b: 0xB2,
            a: 0xFF,
            valid: true,
        },
        transparent: true,
        font_weight: FontWeight::UseCurrentFormat,
    }, // Dback
    ColorEntry {
        color: Color {
            r: 0x00,
            g: 0x00,
            b: 0x00,
            a: 0xFF,
            valid: true,
        },
        transparent: false,
        font_weight: FontWeight::UseCurrentFormat,
    }, // Black
    ColorEntry {
        color: Color {
            r: 0xB2,
            g: 0x18,
            b: 0x18,
            a: 0xFF,
            valid: true,
        },
        transparent: false,
        font_weight: FontWeight::UseCurrentFormat,
    }, // Red
    ColorEntry {
        color: Color {
            r: 0x18,
            g: 0xB2,
            b: 0x18,
            a: 0xFF,
            valid: true,
        },
        transparent: false,
        font_weight: FontWeight::UseCurrentFormat,
    }, // Green
    ColorEntry {
        color: Color {
            r: 0xB2,
            g: 0x68,
            b: 0x18,
            a: 0xFF,
            valid: true,
        },
        transparent: false,
        font_weight: FontWeight::UseCurrentFormat,
    }, // Yellow
    ColorEntry {
        color: Color {
            r: 0x18,
            g: 0x18,
            b: 0xB2,
            a: 0xFF,
            valid: true,
        },
        transparent: false,
        font_weight: FontWeight::UseCurrentFormat,
    }, // Blue
    ColorEntry {
        color: Color {
            r: 0xB2,
            g: 0x18,
            b: 0xB2,
            a: 0xFF,
            valid: true,
        },
        transparent: false,
        font_weight: FontWeight::UseCurrentFormat,
    }, // Magenta
    ColorEntry {
        color: Color {
            r: 0x18,
            g: 0xB2,
            b: 0xB2,
            a: 0xFF,
            valid: true,
        },
        transparent: false,
        font_weight: FontWeight::UseCurrentFormat,
    }, // Cyan
    ColorEntry {
        color: Color {
            r: 0xB2,
            g: 0xB2,
            b: 0xB2,
            a: 0xFF,
            valid: true,
        },
        transparent: false,
        font_weight: FontWeight::UseCurrentFormat,
    }, // White
    ////// Intensive color
    ColorEntry {
        color: Color {
            r: 0x00,
            g: 0x00,
            b: 0x00,
            a: 0xFF,
            valid: true,
        },
        transparent: false,
        font_weight: FontWeight::UseCurrentFormat,
    }, // Dfore
    ColorEntry {
        color: Color {
            r: 0xFF,
            g: 0xFF,
            b: 0xFF,
            a: 0xFF,
            valid: true,
        },
        transparent: true,
        font_weight: FontWeight::UseCurrentFormat,
    }, // Dback
    ColorEntry {
        color: Color {
            r: 0x68,
            g: 0x68,
            b: 0x68,
            a: 0xFF,
            valid: true,
        },
        transparent: false,
        font_weight: FontWeight::UseCurrentFormat,
    }, // Black
    ColorEntry {
        color: Color {
            r: 0xFF,
            g: 0x54,
            b: 0x54,
            a: 0xFF,
            valid: true,
        },
        transparent: false,
        font_weight: FontWeight::UseCurrentFormat,
    }, // Red
    ColorEntry {
        color: Color {
            r: 0x54,
            g: 0xFF,
            b: 0x54,
            a: 0xFF,
            valid: true,
        },
        transparent: false,
        font_weight: FontWeight::UseCurrentFormat,
    }, // Green
    ColorEntry {
        color: Color {
            r: 0xFF,
            g: 0xFF,
            b: 0x54,
            a: 0xFF,
            valid: true,
        },
        transparent: false,
        font_weight: FontWeight::UseCurrentFormat,
    }, // Yellow
    ColorEntry {
        color: Color {
            r: 0x54,
            g: 0x54,
            b: 0xFF,
            a: 0xFF,
            valid: true,
        },
        transparent: false,
        font_weight: FontWeight::UseCurrentFormat,
    }, // Blue
    ColorEntry {
        color: Color {
            r: 0xFF,
            g: 0x54,
            b: 0xFF,
            a: 0xFF,
            valid: true,
        },
        transparent: false,
        font_weight: FontWeight::UseCurrentFormat,
    }, // Magenta
    ColorEntry {
        color: Color {
            r: 0x54,
            g: 0xFF,
            b: 0xFF,
            a: 0xFF,
            valid: true,
        },
        transparent: false,
        font_weight: FontWeight::UseCurrentFormat,
    }, // Cyan
    ColorEntry {
        color: Color {
            r: 0xFF,
            g: 0xFF,
            b: 0xFF,
            a: 0xFF,
            valid: true,
        },
        transparent: false,
        font_weight: FontWeight::UseCurrentFormat,
    }, // White
];

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_color_table() {
        let color: (i32, i32, i32) = BASE_COLOR_TABLE[0].color.into();
        assert_eq!(color, (0x00, 0x00, 0x00));
        let color: (i32, i32, i32) = BASE_COLOR_TABLE[1].color.into();
        assert_eq!(color, (0xB2, 0xB2, 0xB2));
        let color: (i32, i32, i32) = BASE_COLOR_TABLE[2].color.into();
        assert_eq!(color, (0x00, 0x00, 0x00));
        let color: (i32, i32, i32) = BASE_COLOR_TABLE[3].color.into();
        assert_eq!(color, (0xB2, 0x18, 0x18));
        let color: (i32, i32, i32) = BASE_COLOR_TABLE[4].color.into();
        assert_eq!(color, (0x18, 0xB2, 0x18));
        let color: (i32, i32, i32) = BASE_COLOR_TABLE[5].color.into();
        assert_eq!(color, (0xB2, 0x68, 0x18));
        let color: (i32, i32, i32) = BASE_COLOR_TABLE[6].color.into();
        assert_eq!(color, (0x18, 0x18, 0xB2));
        let color: (i32, i32, i32) = BASE_COLOR_TABLE[7].color.into();
        assert_eq!(color, (0xB2, 0x18, 0xB2));
        let color: (i32, i32, i32) = BASE_COLOR_TABLE[8].color.into();
        assert_eq!(color, (0x18, 0xB2, 0xB2));
        let color: (i32, i32, i32) = BASE_COLOR_TABLE[9].color.into();
        assert_eq!(color, (0xB2, 0xB2, 0xB2));

        let color: (i32, i32, i32) = BASE_COLOR_TABLE[10].color.into();
        assert_eq!(color, (0x00, 0x00, 0x00));
        let color: (i32, i32, i32) = BASE_COLOR_TABLE[11].color.into();
        assert_eq!(color, (0xFF, 0xFF, 0xFF));
        let color: (i32, i32, i32) = BASE_COLOR_TABLE[12].color.into();
        assert_eq!(color, (0x68, 0x68, 0x68));
        let color: (i32, i32, i32) = BASE_COLOR_TABLE[13].color.into();
        assert_eq!(color, (0xFF, 0x54, 0x54));
        let color: (i32, i32, i32) = BASE_COLOR_TABLE[14].color.into();
        assert_eq!(color, (0x54, 0xFF, 0x54));
        let color: (i32, i32, i32) = BASE_COLOR_TABLE[15].color.into();
        assert_eq!(color, (0xFF, 0xFF, 0x54));
        let color: (i32, i32, i32) = BASE_COLOR_TABLE[16].color.into();
        assert_eq!(color, (0x54, 0x54, 0xFF));
        let color: (i32, i32, i32) = BASE_COLOR_TABLE[17].color.into();
        assert_eq!(color, (0xFF, 0x54, 0xFF));
        let color: (i32, i32, i32) = BASE_COLOR_TABLE[18].color.into();
        assert_eq!(color, (0x54, 0xFF, 0xFF));
        let color: (i32, i32, i32) = BASE_COLOR_TABLE[19].color.into();
        assert_eq!(color, (0xFF, 0xFF, 0xFF));
    }

    #[test]
    fn test_character_color() {
        let c1 = CharacterColor::new(COLOR_SPACE_RGB, 20);
        let c2 = CharacterColor::new(COLOR_SPACE_RGB, 20);
        let c3 = CharacterColor::empty();

        assert!(c1 == c2);
        assert!(c1 != c3);
    }
}
