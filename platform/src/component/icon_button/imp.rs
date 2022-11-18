use std::cell::{Cell, RefCell};

use gtk::{
    glib::{
        self,
        once_cell::sync::{Lazy, OnceCell},
        ParamSpec, ParamSpecString, Value,
    },
    prelude::*,
    subclass::prelude::*, Image,
};

use crate::{FontIcon, FontType, IconType, SvgIcon};

#[derive(Default)]
pub struct IconButton {
    pub font_icon: RefCell<Option<FontIcon>>,
    pub svg_icon: RefCell<Option<SvgIcon>>,
    pub gtk_icon: RefCell<Option<Image>>,
    pub code: RefCell<Option<String>>,
    pub icon_name: RefCell<Option<String>>,
    pub icon_size: Cell<i32>,
    pub icon_type: OnceCell<IconType>,
}

#[glib::object_subclass]
impl ObjectSubclass for IconButton {
    const NAME: &'static str = "IconButton";

    type Type = super::IconButton;

    type ParentType = gtk::Widget;

    fn class_init(klass: &mut Self::Class) {
        // The layout manager determines how child widgets are laid out.
        klass.set_layout_manager_type::<gtk::BinLayout>();

        // Make it look like a GTK button.
        klass.set_css_name("button");

        // Make it appear as a button to accessibility tools.
        klass.set_accessible_role(gtk::AccessibleRole::Button);
    }
}

impl IconButton {
    pub fn property_change(&self) {
        let mut generated = false;
        match self.icon_type.get() {
            None => return,
            Some(IconType::SegoeMDL2) => {
                if let Some(code) = self.code.borrow().as_ref() {
                    let icon = FontIcon::new(code, FontType::SegoeMDL2);
                    icon.set_parent(&*self.instance());
                    self.font_icon.borrow_mut().replace(icon);
                    generated = true;
                }
            }
            Some(IconType::SegoeFluent) => {
                if let Some(code) = self.code.borrow().as_ref() {
                    let icon = FontIcon::new(code, FontType::SegoeFluent);
                    icon.set_parent(&*self.instance());
                    self.font_icon.borrow_mut().replace(icon);
                    generated = true;
                }
            }
            Some(IconType::FontAwesomeFreeRegular) => {
                if let Some(code) = self.code.borrow().as_ref() {
                    let icon = FontIcon::new(code, FontType::FontAwesomeFreeRegular);
                    icon.set_parent(&*self.instance());
                    self.font_icon.borrow_mut().replace(icon);
                    generated = true;
                }
            }
            Some(IconType::FontAwesomeFreeSolid) => {
                if let Some(code) = self.code.borrow().as_ref() {
                    let icon = FontIcon::new(code, FontType::FontAwesomeFreeSolid);
                    icon.set_parent(&*self.instance());
                    self.font_icon.borrow_mut().replace(icon);
                    generated = true;
                }
            }
            Some(IconType::FontAwesomeBrands) => {
                if let Some(code) = self.code.borrow().as_ref() {
                    let icon = FontIcon::new(code, FontType::FontAwesomeBrands);
                    icon.set_parent(&*self.instance());
                    self.font_icon.borrow_mut().replace(icon);
                    generated = true;
                }
            }
            Some(IconType::Svg) => {
                if let Some(icon_name) = self.icon_name.borrow().as_ref() {
                    let icon = SvgIcon::new(icon_name);
                    icon.set_parent(&*self.instance());
                    self.svg_icon.borrow_mut().replace(icon);
                    generated = true;
                }
            }
            Some(IconType::Gtk) => {
                if let Some(icon_name) = self.icon_name.borrow().as_ref() {
                    let icon = Image::builder().icon_name(icon_name).build();
                    icon.set_parent(&*self.instance());
                    self.gtk_icon.borrow_mut().replace(icon);
                    generated = true;
                }
            }
        }
        if generated {
            self.size_change()
        }
    }

    pub fn size_change(&self) {
        let size = self.icon_size.get();
        match self.icon_type.get() {
            None => panic!("`icon_type` of IconButton is None!"),
            Some(IconType::SegoeMDL2) => self
                .font_icon
                .borrow()
                .as_ref()
                .expect("`segoe_icon` should not be None when IconButton icon type is `SegoeMDL2`")
                .set_size(size),
            Some(IconType::SegoeFluent) => self
                .font_icon
                .borrow()
                .as_ref()
                .expect(
                    "`segoe_icon` should not be None when IconButton icon type is `SegoeFluent`",
                )
                .set_size(size),
            Some(IconType::FontAwesomeFreeRegular) => self
                .font_icon
                .borrow()
                .as_ref()
                .expect("`font_awesome_icon` should not be None when IconButton icon type is `FontAwesomeFreeRegular`")
                .set_size(size),
            Some(IconType::FontAwesomeFreeSolid) => self
                .font_icon
                .borrow()
                .as_ref()
                .expect("`font_awesome_icon` should not be None when IconButton icon type is `FontAwesomeFreeSolid`")
                .set_size(size),
            Some(IconType::FontAwesomeBrands) => self
                .font_icon
                .borrow()
                .as_ref()
                .expect("`font_awesome_icon` should not be None when IconButton icon type is `FontAwesomeBrands`")
                .set_size(size),
            Some(IconType::Svg) => {},
            Some(IconType::Gtk) => {},
        }
    }
}

impl ObjectImpl for IconButton {
    fn constructed(&self) {
        self.parent_constructed();
        // Set the default icon size to 10
        self.icon_size.set(10);
    }

    fn dispose(&self) {
        if let Some(icon) = self.font_icon.take() {
            icon.unparent();
        }
        if let Some(icon) = self.svg_icon.take() {
            icon.unparent();
        }
        if let Some(icon) = self.gtk_icon.take() {
            icon.unparent();
        }
    }

    fn properties() -> &'static [ParamSpec] {
        static PROPERTIES: Lazy<Vec<ParamSpec>> = Lazy::new(|| {
            vec![
                ParamSpecString::builder("code").build(),
                ParamSpecString::builder("icon-name").build(),
                ParamSpecString::builder("icon-type").build(),
                ParamSpecString::builder("icon-size").build(),
            ]
        });
        PROPERTIES.as_ref()
    }

    fn set_property(&self, _id: usize, value: &Value, pspec: &ParamSpec) {
        match pspec.name() {
            "code" => {
                let input_value = value
                    .get()
                    .expect("The value needs to be of type `String`.");
                self.code.borrow_mut().replace(input_value);
                self.property_change();
            }
            "icon-name" => {
                let input_value = value
                    .get()
                    .expect("The value needs to be of type `String`.");
                self.icon_name.borrow_mut().replace(input_value);
                self.property_change();
            }
            "icon-type" => {
                let input_value = value
                    .get()
                    .expect("The value needs to be of type `String`.");
                let icon_type = IconType::from_string(input_value);
                self.icon_type
                    .set(icon_type)
                    .expect("`icon_type` of IconButton can only set once.");
                self.property_change();
            }
            _ => unimplemented!(),
        }
    }

    fn property(&self, _id: usize, pspec: &ParamSpec) -> Value {
        match pspec.name() {
            "code" => match self.code.borrow().as_deref() {
                Some(code) => code.to_value(),
                None => "".to_value(),
            },
            "icon-name" => match self.icon_name.borrow().as_deref() {
                Some(svg) => svg.to_value(),
                None => "".to_value(),
            },
            "icon-type" => self
                .icon_type
                .get()
                .expect("`icon_type` of IconButton should initialize first before ues.")
                .to_str()
                .to_value(),
            _ => unimplemented!(),
        }
    }
}

impl WidgetImpl for IconButton {}
