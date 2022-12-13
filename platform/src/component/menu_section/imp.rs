use gtk::{
    glib::{
        self,
        once_cell::sync::{Lazy, OnceCell},
        ParamSpec, ParamSpecString, Value,
    },
    prelude::*,
    subclass::prelude::*,
    Label,
};

use crate::PartialSeperator;

#[derive(Default)]
pub struct MenuSection {
    seperator: OnceCell<PartialSeperator>,
    label: OnceCell<Label>,
}

#[glib::object_subclass]
impl ObjectSubclass for MenuSection {
    const NAME: &'static str = "MenuSection";

    type Type = super::MenuSection;

    type ParentType = gtk::Widget;

    fn class_init(klass: &mut Self::Class) {
        klass.set_layout_manager_type::<gtk::BoxLayout>();
    }
}

impl ObjectImpl for MenuSection {
    fn constructed(&self) {
        self.parent_constructed();
        self.instance().add_css_class("menu-section");

        let layout = self
            .instance()
            .layout_manager()
            .unwrap()
            .downcast::<gtk::BoxLayout>()
            .unwrap();
        layout.set_orientation(gtk::Orientation::Vertical);

        let seperator = PartialSeperator::builder()
            .orientation(gtk::Orientation::Horizontal)
            .factor(0.95)
            .build();
        self.seperator
            .set(seperator)
            .expect("`seperator` of `MenuSection` can only set once.");
    }

    fn dispose(&self) {
        if let Some(seperator) = self.seperator.get() {
            seperator.unparent();
        }
        if let Some(label) = self.label.get() {
            label.unparent();
        }
    }

    fn properties() -> &'static [ParamSpec] {
        static PROPERTIES: Lazy<Vec<ParamSpec>> =
            Lazy::new(|| vec![ParamSpecString::builder("label").build()]);
        PROPERTIES.as_ref()
    }

    fn set_property(&self, _id: usize, value: &Value, pspec: &ParamSpec) {
        match pspec.name() {
            "label" => {
                let input_value: Label =
                    value.get().expect("The value needs to be of type `Label`.");
                input_value.set_parent(&*self.instance());
                self.label
                    .set(input_value)
                    .expect("`label` of `MenuSection` can only set once.");
            }
            _ => unimplemented!(),
        }
    }
}

impl WidgetImpl for MenuSection {}
