mod imp;

use glib::Object;
use gtk::{glib, prelude::*, subclass::prelude::*, Widget};

glib::wrapper! {
    pub struct FontIcon(ObjectSubclass<imp::FontIcon>);
}

impl FontIcon {
    pub fn new(code: &str, font_type: FontType) -> Self {
        let icon: FontIcon = Object::builder().property("code", code).build();
        icon.imp()
            .font_type
            .set(font_type)
            .expect("`font_type` of SegoeFontIcon can only set once.");
        icon.imp().attribute_change();
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

    pub fn set_bold(&self, bold: bool) {
        self.imp().bold.set(bold);
        self.imp().attribute_change();
    }

    pub fn set_parent<T: IsA<Widget>>(&self, parent: &T) {
        self.imp().label.borrow().set_parent(parent)
    }

    pub fn unparent(&self) {
        self.imp().label.borrow().unparent()
    }
}

#[repr(usize)]
#[derive(Default, Debug)]
pub enum FontType {
    #[default]
    SegoeMDL2 = 0,
    SegoeFluent,
    FontAwesomeFreeRegular,
    FontAwesomeFreeSolid,
    FontAwesomeBrands,
}

impl FontType {
    pub fn to_usize(&self) -> usize {
        match self {
            Self::SegoeMDL2 => 0,
            Self::SegoeFluent => 1,
            Self::FontAwesomeFreeRegular => 2,
            Self::FontAwesomeFreeSolid => 3,
            Self::FontAwesomeBrands => 4,
        }
    }
}
