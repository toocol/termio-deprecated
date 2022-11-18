mod imp;

use glib::Object;
use gtk::glib;

pub const ICON_TYPE_SEGOE_MDL2: &str = "Segoe MDL2";
pub const ICON_TYPE_SEGOE_FLUENT: &str = "Segoe Fluent";
pub const ICON_TYPE_FONT_AWESOME_FREE_REGULAR: &str = "Font Awesome Free Regular";
pub const ICON_TYPE_FONT_AWESOME_FREE_SOLID: &str = "Font Awesome Free Solid";
pub const ICON_TYPE_FONT_AWESOME_BRANDS: &str = "Font Awesome Brands";
pub const ICON_TYPE_SVG: &str = "Svg";
pub const ICON_TYPE_GTK: &str = "Gtk";

glib::wrapper! {
    pub struct IconButton(ObjectSubclass<imp::IconButton>)
        @extends gtk::Widget,
        @implements gtk::Accessible, gtk::Buildable, gtk::ConstraintTarget;
}

impl IconButton {
    pub fn new() -> Self {
        Object::builder().build()
    }
}

#[repr(usize)]
#[derive(Default, Debug)]
pub enum IconType {
    #[default]
    SegoeMDL2 = 0,
    SegoeFluent,
    FontAwesomeFreeRegular,
    FontAwesomeFreeSolid,
    FontAwesomeBrands,
    Svg,
    Gtk,
}

impl IconType {
    pub fn from_string(icon_type: &str) -> Self {
        match icon_type {
            ICON_TYPE_SEGOE_MDL2 => Self::SegoeMDL2,
            ICON_TYPE_SEGOE_FLUENT => Self::SegoeFluent,
            ICON_TYPE_FONT_AWESOME_FREE_REGULAR => Self::FontAwesomeFreeRegular,
            ICON_TYPE_FONT_AWESOME_FREE_SOLID => Self::FontAwesomeFreeSolid,
            ICON_TYPE_FONT_AWESOME_BRANDS => Self::FontAwesomeBrands,
            ICON_TYPE_SVG => Self::Svg,
            ICON_TYPE_GTK => Self::Gtk,
            _ => panic!("Unrecorderlize icon type: {}", icon_type),
        }
    }

    pub fn to_str(&self) -> &str {
        match self {
            Self::SegoeMDL2 => ICON_TYPE_SEGOE_MDL2,
            Self::SegoeFluent => ICON_TYPE_SEGOE_FLUENT,
            Self::FontAwesomeFreeRegular => ICON_TYPE_FONT_AWESOME_FREE_REGULAR,
            Self::FontAwesomeFreeSolid => ICON_TYPE_FONT_AWESOME_FREE_SOLID,
            Self::FontAwesomeBrands => ICON_TYPE_FONT_AWESOME_BRANDS,
            Self::Svg => ICON_TYPE_SVG,
            Self::Gtk => ICON_TYPE_SVG,
        }
    }

    pub fn to_usize(&self) -> usize {
        match self {
            Self::SegoeMDL2 => 0,
            Self::SegoeFluent => 1,
            Self::FontAwesomeFreeRegular => 2,
            Self::FontAwesomeFreeSolid => 3,
            Self::FontAwesomeBrands => 4,
            Self::Svg => 5,
            Self::Gtk => 6,
        }
    }
}
