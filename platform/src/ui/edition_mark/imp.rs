use gtk::{
    glib::{
        self,
        once_cell::sync::{Lazy, OnceCell},
        ParamSpec, ParamSpecString, Value,
    },
    prelude::*,
    subclass::prelude::*,
    traits::WidgetExt,
    Label,
};

use crate::FontIcon;

#[derive(Default)]
pub struct EditionMark {
    // Has to be SegoeFluent font icon
    pub icon: OnceCell<FontIcon>,
    pub label: OnceCell<Label>,
}

#[glib::object_subclass]
impl ObjectSubclass for EditionMark {
    const NAME: &'static str = "EditionMark";

    type Type = super::EditionMark;

    type ParentType = gtk::Widget;

    fn class_init(klass: &mut Self::Class) {
        // The layout manager determines how child widgets are laid out.
        klass.set_layout_manager_type::<gtk::BoxLayout>();
    }
}

impl ObjectImpl for EditionMark {
    fn constructed(&self) {
        self.parent_constructed();

        let obj = self.instance();

        let layout = obj
            .layout_manager()
            .unwrap()
            .downcast::<gtk::BoxLayout>()
            .unwrap();
        layout.set_orientation(gtk::Orientation::Horizontal);
        layout.set_spacing(5);
        layout.set_homogeneous(false);

        obj.set_margin_start(5);
        obj.set_vexpand(false);
        obj.set_hexpand(false);
        obj.set_height_request(16);
    }

    fn dispose(&self) {
        if let Some(icon) = self.icon.get() {
            icon.unparent();
        }
        if let Some(label) = self.label.get() {
            label.unparent();
        }
    }

    fn properties() -> &'static [ParamSpec] {
        static PROPERTIES: Lazy<Vec<ParamSpec>> = Lazy::new(|| {
            vec![
                ParamSpecString::builder("code").build(),
                ParamSpecString::builder("label").build(),
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
                let icon = FontIcon::new(input_value, crate::FontType::SegoeFluent);
                icon.set_parent(&*self.instance());
                self.icon
                    .set(icon)
                    .expect("`code` of EditionMark can only set once.");
            }
            "label" => {
                let input_value: &str = value
                    .get()
                    .expect("The value needs to be of type `String`.");

                let label = Label::new(None);
                label.set_use_markup(true);
                label.set_markup(format!("<span font_desc=\"8\">{}</span>", input_value).as_str());
                label.set_parent(&*self.instance());

                self.label
                    .set(label)
                    .expect("`label` of EditionMark should only set once.")
            }
            _ => unimplemented!(),
        }
    }

    fn property(&self, _id: usize, pspec: &ParamSpec) -> Value {
        match pspec.name() {
            "code" => self
                .icon
                .get()
                .expect("`icon` of EditionMark should set first before use.")
                .get_code()
                .to_value(),
            "label" => self
                .label
                .get()
                .expect("`label` of EditionMark should set first before use.")
                .label()
                .to_value(),
            _ => unimplemented!(),
        }
    }
}

impl WidgetImpl for EditionMark {}
