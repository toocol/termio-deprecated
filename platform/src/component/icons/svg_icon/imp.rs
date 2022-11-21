use std::{cell::RefCell, io::BufReader, slice};

use gtk::{
    gdk_pixbuf::Pixbuf,
    glib::{self, once_cell::sync::Lazy, ParamSpec, ParamSpecString, Value},
    prelude::*,
    subclass::prelude::*,
    Image,
};
use utilities::Asset;

const PATH_PREFIX: &str = "svg/";
const CSS_CLASS: &str = "svg-icon";

#[derive(Default)]
pub struct SvgIcon {
    pub image: RefCell<Option<Image>>,
}

#[glib::object_subclass]
impl ObjectSubclass for SvgIcon {
    const NAME: &'static str = "SvgIcon";

    type Type = super::SvgIcon;
}

impl ObjectImpl for SvgIcon {
    fn properties() -> &'static [ParamSpec] {
        static PROPERTIES: Lazy<Vec<ParamSpec>> =
            Lazy::new(|| vec![ParamSpecString::builder("icon-name").build()]);
        PROPERTIES.as_ref()
    }

    fn set_property(&self, _id: usize, value: &Value, pspec: &ParamSpec) {
        match pspec.name() {
            "icon-name" => {
                let mut input_value: String = value
                    .get()
                    .expect("The value needs to be of type `String`.");
                input_value.push_str(".svg");

                let mut path = PATH_PREFIX.to_string();
                path.push_str(input_value.as_str());

                let asset = Asset::get(path.as_str())
                    .expect(format!("Get embed asset `{}` failed.", path).as_str());

                let length = asset.data.len();
                let data = asset.data.as_ptr();

                unsafe {
                    let data = slice::from_raw_parts(data, length);

                    let pixbuf = Pixbuf::from_read(BufReader::new(data))
                        .expect(format!("Load svg file {} failed.", path).as_str());

                    let image = Image::from_pixbuf(Some(&pixbuf));
                    image.add_css_class(CSS_CLASS);
                    self.image.borrow_mut().replace(image);
                }
            }
            _ => unimplemented!(),
        }
    }

    fn dispose(&self) {
        if let Some(image) = self.image.borrow().as_ref() {
            image.unparent();
        }
    }
}
