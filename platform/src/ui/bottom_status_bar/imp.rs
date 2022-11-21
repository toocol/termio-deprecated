use std::cell::RefCell;

use gtk::{
    glib::{
        self,
        once_cell::sync::{Lazy, OnceCell},
        ParamSpec, ParamSpecString, Value,
    },
    prelude::*,
    subclass::prelude::*,
    traits::WidgetExt,
    Align, Orientation,
};

use crate::{EditionMark, EditionMarkJsonObject};

#[derive(Default)]
pub struct BottomStatusBar {
    pub left_box: RefCell<gtk::Box>,
    pub right_box: RefCell<gtk::Box>,

    pub edtion_mark: OnceCell<EditionMark>,
}

#[glib::object_subclass]
impl ObjectSubclass for BottomStatusBar {
    const NAME: &'static str = "BottomStatusBar";

    type Type = super::BottomStatusBar;

    type ParentType = gtk::Widget;

    fn class_init(klass: &mut Self::Class) {
        // The layout manager determines how child widgets are laid out.
        klass.set_layout_manager_type::<gtk::BoxLayout>();
    }
}

impl ObjectImpl for BottomStatusBar {
    fn constructed(&self) {
        self.parent_constructed();

        let obj = self.instance();

        let layout = obj
            .layout_manager()
            .unwrap()
            .downcast::<gtk::BoxLayout>()
            .unwrap();
        layout.set_orientation(gtk::Orientation::Horizontal);
        layout.set_homogeneous(true);

        let left_box = self.left_box.borrow();
        let right_box = self.right_box.borrow();

        left_box.set_spacing(10);
        right_box.set_spacing(10);

        left_box.set_parent(&*obj);
        right_box.set_parent(&*obj);

        left_box.add_css_class("left-box");
        right_box.add_css_class("right-box");

        left_box.set_halign(Align::Start);
        right_box.set_halign(Align::End);

        left_box.set_orientation(Orientation::Horizontal);
        right_box.set_orientation(Orientation::Horizontal);

        left_box.set_hexpand(false);
        right_box.set_hexpand(false);
    }

    fn dispose(&self) {
        self.left_box.borrow().unparent();
        self.right_box.borrow().unparent();
    }

    fn properties() -> &'static [ParamSpec] {
        static PROPERTIES: Lazy<Vec<ParamSpec>> =
            Lazy::new(|| vec![ParamSpecString::builder("edition-mark").build()]);
        PROPERTIES.as_ref()
    }

    fn set_property(&self, _id: usize, value: &Value, pspec: &ParamSpec) {
        let obj = self.instance();
        match pspec.name() {
            "edition-mark" => {
                let json_data = value
                    .get()
                    .expect("The value needs to be of type `String`.");

                let json_object: EditionMarkJsonObject = serde_json::from_str(json_data)
                    .expect("Serialize `activity-bar-items` json config failed, please check the .ui template");
                let edition_mark = json_object.to_bottom_status_bar_item();
                obj.register_left(&edition_mark);

                self.edtion_mark
                    .set(edition_mark)
                    .expect("`edtion_mark` of BottomStatusBar can only set once.");
            }
            _ => unimplemented!(),
        }
    }
}

impl WidgetImpl for BottomStatusBar {}
