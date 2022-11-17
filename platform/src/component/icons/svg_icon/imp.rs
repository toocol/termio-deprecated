use std::cell::RefCell;

use gtk::{
    gdk_pixbuf::Pixbuf,
    glib::{self, once_cell::sync::Lazy, ParamSpec, ParamSpecString, Value},
    prelude::*,
    subclass::prelude::*,
    Image,
};

const PATH_PREFIX: &str = "svg/";

#[derive(Default)]
pub struct SvgIcon {
    pub image: RefCell<Option<Image>>,
    pub svg: RefCell<Option<String>>,
}

impl SvgIcon {
    pub fn initialize_image(&self) {
        let mut path_prefix = PATH_PREFIX.to_string();
        path_prefix.push_str(
            self.svg
                .borrow()
                .as_ref()
                .expect("`svg` of SvgIcon is None.")
                .as_str(),
        );
        path_prefix.push_str(".svg");
        let pixbuf = Pixbuf::from_file(path_prefix.as_str())
            .expect(format!("load svg file failed, {}", path_prefix).as_str());
        let image = Image::from_pixbuf(Some(&pixbuf));
        self.image.borrow_mut().replace(image);
    }
}

#[glib::object_subclass]
impl ObjectSubclass for SvgIcon {
    const NAME: &'static str = "SvgIcon";

    type Type = super::SvgIcon;
}

impl ObjectImpl for SvgIcon {
    fn properties() -> &'static [ParamSpec] {
        static PROPERTIES: Lazy<Vec<ParamSpec>> =
            Lazy::new(|| vec![ParamSpecString::builder("svg").build()]);
        PROPERTIES.as_ref()
    }

    fn set_property(&self, _id: usize, value: &Value, pspec: &ParamSpec) {
        match pspec.name() {
            "svg" => {
                let input_value = value
                    .get()
                    .expect("The value needs to be of type `String`.");
                self.svg.borrow_mut().replace(input_value);
            }
            _ => unimplemented!(),
        }
    }

    fn property(&self, _id: usize, pspec: &ParamSpec) -> Value {
        match pspec.name() {
            "svg" => match self.svg.borrow().as_deref() {
                Some(svg) => svg.to_value(),
                None => "".to_value(),
            },
            _ => unimplemented!(),
        }
    }
}
