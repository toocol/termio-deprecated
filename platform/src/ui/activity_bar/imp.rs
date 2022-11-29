use std::cell::RefCell;

use gtk::{prelude::*, subclass::prelude::*, glib::{self, ParamSpec, once_cell::sync::Lazy, ParamSpecString, Value}};

#[derive(Default)]
pub struct ActivityBar {
    pub activate_widget_name: RefCell<Option<String>>
}

#[glib::object_subclass]
impl ObjectSubclass for ActivityBar {
    const NAME: &'static str = "ActivityBar";

    type Type = super::ActivityBar;

    type ParentType = gtk::Widget;

    fn class_init(klass: &mut Self::Class) {
        klass.set_layout_manager_type::<gtk::BoxLayout>();
    }
}

impl ObjectImpl for ActivityBar {
    fn constructed(&self) {
        self.parent_constructed();

        let layout = self.instance()
            .layout_manager()
            .unwrap()
            .downcast::<gtk::BoxLayout>()
            .unwrap();

        layout.set_orientation(gtk::Orientation::Vertical);
        layout.set_homogeneous(true);
    }

    fn properties() -> &'static [ParamSpec] {
        static PROPERTIES: Lazy<Vec<ParamSpec>> = Lazy::new(|| {
            vec![
                ParamSpecString::builder("initial-widget-name").build(),
            ]
        });
        PROPERTIES.as_ref()
    }

    fn set_property(&self, _id: usize, value: &Value, pspec: &ParamSpec) {
        match pspec.name() {
            "initial-widget-name" => {
                let input_value = value
                    .get()
                    .expect("The value needs to be of type `String`.");
                *self.activate_widget_name.borrow_mut() = input_value;
            }
            _ => unimplemented!(),
        }
    }
}

impl WidgetImpl for ActivityBar {}
