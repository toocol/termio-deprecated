use std::cell::RefCell;

use gtk::{
    gdk_pixbuf::Pixbuf,
    glib::{self, once_cell::sync::Lazy, ParamSpec, ParamSpecString, Value},
    prelude::*,
    subclass::prelude::*,
    Image, Align,
};

const PATH_PREFIX: &str = "svg";

#[derive(Default)]
pub struct SvgIcon {
    pub image: RefCell<Option<Image>>,
    pub svg: RefCell<Option<String>>,
}

impl SvgIcon {
    pub fn initialize_image(&self) {
        let mut abs_path =
            std::env::current_dir().expect("`initialize_image` get current .exe directory failed.");
        abs_path.push("target");
        abs_path.push("debug");
        abs_path.push(PATH_PREFIX);
        abs_path.push(
            self.svg
                .borrow()
                .as_ref()
                .expect("`svg` of SvgIcon is None."),
        );

        let pixbuf = Pixbuf::from_file(abs_path.as_path()).expect(
            format!(
                "load svg file failed, {}",
                abs_path.to_str().expect("`abs_path` format error.")
            )
            .as_str(),
        );
        let image = Image::from_pixbuf(Some(&pixbuf));
        image.set_halign(Align::Center);
        image.set_valign(Align::Center);
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
                let mut input_value: String = value
                    .get()
                    .expect("The value needs to be of type `String`.");
                input_value.push_str(".svg");
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
