mod imp;

use glib::Object;
use gtk::{glib, prelude::IsA, subclass::prelude::*, traits::WidgetExt, Widget};

use self::imp::FontAwesomeIconType;

glib::wrapper! {
    pub struct FontAwesomeIcon(ObjectSubclass<imp::FontAwesomeIcon>);
}

impl FontAwesomeIcon {
    pub fn from_code(code: &str, style: FontAwesomeStyle) -> Self {
        let icon: FontAwesomeIcon = Object::builder().property("code", code).build();
        icon.imp()
            .icon_type
            .set(imp::FontAwesomeIconType::Code)
            .expect("`icon_type` of FontAwesomeIcon can only set once.");
        icon.imp()
            .font_style
            .set(style)
            .expect("`icon_type` of FontAwesomeIcon can only set once.");
        icon.imp().initialize_label();
        icon
    }

    pub fn from_svg(svg_path: &str) -> Self {
        let icon: FontAwesomeIcon = Object::builder().property("svg", svg_path).build();
        icon.imp()
            .icon_type
            .set(imp::FontAwesomeIconType::SVG)
            .expect("`icon_type` of FontAwesomeIcon can only set once.");
        icon.imp().initialize_image();
        icon
    }

    pub fn set_color(&self, color: &str) {
        self.imp().color.borrow_mut().replace(color.to_string());
        self.imp().attribute_change();
    }

    pub fn set_size(&self, size: i32) {
        self.imp().size.set(size);
        self.imp().attribute_change();
    }

    pub fn set_parent<T: IsA<Widget>>(&self, parent: &T) {
        match self
            .imp()
            .icon_type
            .get()
            .expect("`icon_type` of FontAwesomeIcon must initialize before use.")
        {
            FontAwesomeIconType::Code => self
                .imp()
                .label
                .borrow()
                .as_ref()
                .expect("`lable` of FontAwesomeIcon is None.")
                .set_parent(parent),
            FontAwesomeIconType::SVG => self
                .imp()
                .image
                .borrow()
                .as_ref()
                .expect("`image` of FontAwesomeIcon is None.")
                .set_parent(parent),
            _ => unreachable!(),
        }
    }

    pub fn unparent(&self) {
        match self
            .imp()
            .icon_type
            .get()
            .expect("`icon_type` of FontAwesomeIcon must initialize before use.")
        {
            FontAwesomeIconType::Code => self
                .imp()
                .label
                .borrow()
                .as_ref()
                .expect("`lable` of FontAwesomeIcon is None.")
                .unparent(),
            FontAwesomeIconType::SVG => self
                .imp()
                .image
                .borrow()
                .as_ref()
                .expect("`image` of FontAwesomeIcon is None.")
                .unparent(),
            _ => unreachable!(),
        }
    }
}

#[derive(Default, Clone, Copy, Debug)]
#[repr(usize)]
pub enum FontAwesomeStyle {
    #[default]
    FreeRegular = 0,
    FreeSolid,
    Brands,
}

impl FontAwesomeStyle {
    pub fn to_usize(&self) -> usize {
        match self {
            Self::FreeRegular => 0,
            Self::FreeSolid => 1,
            Self::Brands => 2,
        }
    }
}
