use gtk::gdk::ModifierType;

pub const GTK_KEYCODE_ESCAPE: u32 = 27;
pub const GTK_KEYCODE_TAB: u32 = 9;
pub const GTK_KEYCODE_BACKSPACE: u32 = 8;
pub const GTK_KEYCODE_ENTER: u32 = 13;
pub const GTK_KEYCODE_INSERT: u32 = 45;
pub const GTK_KEYCODE_DELETE: u32 = 46;
pub const GTK_KEYCODE_PAUSE: u32 = 19;
pub const GTK_KEYCODE_PRINT: u32 = 44;
pub const GTK_KEYCODE_HOME: u32 = 36;
pub const GTK_KEYCODE_END: u32 = 35;
pub const GTK_KEYCODE_LEFT: u32 = 37;
pub const GTK_KEYCODE_UP: u32 = 38;
pub const GTK_KEYCODE_RIGHT: u32 = 39;
pub const GTK_KEYCODE_DOWN: u32 = 40;
pub const GTK_KEYCODE_PAGE_UP: u32 = 33;
pub const GTK_KEYCODE_PAGE_DOWN: u32 = 34;
pub const GTK_KEYCODE_SHIFT_L: u32 = 16;
pub const GTK_KEYCODE_SHIFT_R: u32 = 161;
pub const GTK_KEYCODE_CONTROL_L: u32 = 17;
pub const GTK_KEYCODE_CONTROL_R: u32 = 163;
pub const GTK_KEYCODE_META_L: u32 = 91;
pub const GTK_KEYCODE_META_R: u32 = 92;
pub const GTK_KEYCODE_ALT_L: u32 = 18;
pub const GTK_KEYCODE_ALT_R: u32 = 165;
pub const GTK_KEYCODE_CAPS_LOCK: u32 = 20;
pub const GTK_KEYCODE_NUM_LOCK: u32 = 144;
pub const GTK_KEYCODE_SCROLL_LOCK: u32 = 145;
pub const GTK_KEYCODE_F1: u32 = 112;
pub const GTK_KEYCODE_F2: u32 = 113;
pub const GTK_KEYCODE_F3: u32 = 114;
pub const GTK_KEYCODE_F4: u32 = 115;
pub const GTK_KEYCODE_F5: u32 = 116;
pub const GTK_KEYCODE_F6: u32 = 117;
pub const GTK_KEYCODE_F7: u32 = 118;
pub const GTK_KEYCODE_F8: u32 = 119;
pub const GTK_KEYCODE_F9: u32 = 120;
pub const GTK_KEYCODE_F10: u32 = 121;
pub const GTK_KEYCODE_F11: u32 = 122;
pub const GTK_KEYCODE_F12: u32 = 123;
pub const GTK_KEYCODE_F13: u32 = 124;
pub const GTK_KEYCODE_F14: u32 = 125;
pub const GTK_KEYCODE_F15: u32 = 126;
pub const GTK_KEYCODE_F16: u32 = 127;
pub const GTK_KEYCODE_F17: u32 = 128;
pub const GTK_KEYCODE_F18: u32 = 129;
pub const GTK_KEYCODE_F19: u32 = 130;
pub const GTK_KEYCODE_F20: u32 = 131;
pub const GTK_KEYCODE_F21: u32 = 132;
pub const GTK_KEYCODE_F22: u32 = 133;
pub const GTK_KEYCODE_F23: u32 = 134;
pub const GTK_KEYCODE_F24: u32 = 135;

pub struct QtCodeMapping;

#[allow(non_upper_case_globals)]
impl QtCodeMapping {
    pub fn get_qt_modifier(gtk_modifier: ModifierType) -> i32 {
        match gtk_modifier {
            ModifierType::SHIFT_MASK => 0x02000000,
            ModifierType::LOCK_MASK => 0,
            ModifierType::CONTROL_MASK => 0x04000000,
            ModifierType::ALT_MASK => 0x08000000,
            ModifierType::BUTTON1_MASK => 0,
            ModifierType::BUTTON2_MASK => 0,
            ModifierType::BUTTON3_MASK => 0,
            ModifierType::BUTTON4_MASK => 0,
            ModifierType::BUTTON5_MASK => 0,
            ModifierType::SUPER_MASK => 0,
            ModifierType::HYPER_MASK => 0,
            ModifierType::META_MASK => 0x10000000,
            _ => 0,
        }
    }

    pub fn get_qt_code(gtk_code: u32) -> i32 {
        match gtk_code {
            GTK_KEYCODE_ESCAPE => 0x01000000,
            GTK_KEYCODE_TAB => 0x01000001,
            GTK_KEYCODE_BACKSPACE => 0x01000003,
            GTK_KEYCODE_ENTER => 0x01000005,
            GTK_KEYCODE_INSERT => 0x01000006,
            GTK_KEYCODE_DELETE => 0x01000007,
            GTK_KEYCODE_PAUSE => 0x01000008,
            GTK_KEYCODE_PRINT => 0x01000009,
            GTK_KEYCODE_HOME => 0x01000010,
            GTK_KEYCODE_END => 0x01000011,
            GTK_KEYCODE_LEFT => 0x01000012,
            GTK_KEYCODE_UP => 0x01000013,
            GTK_KEYCODE_RIGHT => 0x01000014,
            GTK_KEYCODE_DOWN => 0x01000015,
            GTK_KEYCODE_PAGE_UP => 0x01000016,
            GTK_KEYCODE_PAGE_DOWN => 0x01000017,
            GTK_KEYCODE_SHIFT_L => 0x01000020,
            GTK_KEYCODE_SHIFT_R => 0x01000020,
            GTK_KEYCODE_CONTROL_L => 0x01000021,
            GTK_KEYCODE_CONTROL_R => 0x01000021,
            GTK_KEYCODE_META_L => 0x01000022,
            GTK_KEYCODE_META_R => 0x01000022,
            GTK_KEYCODE_ALT_L => 0x01000023,
            GTK_KEYCODE_ALT_R => 0x01000023,
            GTK_KEYCODE_CAPS_LOCK => 0x01000024,
            GTK_KEYCODE_NUM_LOCK => 0x01000025,
            GTK_KEYCODE_SCROLL_LOCK => 0x01000026,
            GTK_KEYCODE_F1 => 0x01000030,
            GTK_KEYCODE_F2 => 0x01000031,
            GTK_KEYCODE_F3 => 0x01000032,
            GTK_KEYCODE_F4 => 0x01000033,
            GTK_KEYCODE_F5 => 0x01000034,
            GTK_KEYCODE_F6 => 0x01000035,
            GTK_KEYCODE_F7 => 0x01000036,
            GTK_KEYCODE_F8 => 0x01000037,
            GTK_KEYCODE_F9 => 0x01000038,
            GTK_KEYCODE_F10 => 0x01000039,
            GTK_KEYCODE_F11 => 0x0100003a,
            GTK_KEYCODE_F12 => 0x0100003b,
            GTK_KEYCODE_F13 => 0x0100003c,
            GTK_KEYCODE_F14 => 0x0100003d,
            GTK_KEYCODE_F15 => 0x0100003e,
            GTK_KEYCODE_F16 => 0x0100003f,
            GTK_KEYCODE_F17 => 0x01000040,
            GTK_KEYCODE_F18 => 0x01000041,
            GTK_KEYCODE_F19 => 0x01000042,
            GTK_KEYCODE_F20 => 0x01000043,
            GTK_KEYCODE_F21 => 0x01000044,
            GTK_KEYCODE_F22 => 0x01000045,
            GTK_KEYCODE_F23 => 0x01000046,
            GTK_KEYCODE_F24 => 0x01000047,
            _ => gtk_code as i32,
        }
    }
}
