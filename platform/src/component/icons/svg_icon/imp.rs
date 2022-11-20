use std::cell::RefCell;

use gtk::{
    gdk_pixbuf::Pixbuf,
    glib::{
        self,
        once_cell::sync::Lazy,
        ParamSpec, ParamSpecString, Value,
    },
    prelude::*,
    subclass::prelude::*,
    Image,
};

const PATH_PREFIX: &str = "svg";
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

                let mut abs_path = std::env::current_dir().expect("Getting current dir failed.");
                abs_path.push("target");
                abs_path.push("debug");
                abs_path.push(PATH_PREFIX);
                abs_path.push(input_value);

                let pixbuf = Pixbuf::from_file(abs_path.as_path()).expect(
                    format!(
                        "load svg file failed, {}",
                        abs_path.to_str().expect("`abs_path` format error.")
                    )
                    .as_str(),
                );
                let image = Image::from_pixbuf(Some(&pixbuf));
                image.add_css_class(CSS_CLASS);
                self.image.borrow_mut().replace(image);
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
