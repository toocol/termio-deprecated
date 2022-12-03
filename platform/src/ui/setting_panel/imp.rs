use gtk::{glib, prelude::*, subclass::prelude::*};

#[derive(Default)]
pub struct SettingPanel {}

#[glib::object_subclass]
impl ObjectSubclass for SettingPanel {
    const NAME: &'static str = "SettingPanel";

    type Type = super::SettingPanel;

    type ParentType = gtk::Widget;

    fn class_init(klass: &mut Self::Class) {
        klass.set_layout_manager_type::<gtk::BoxLayout>();
    }
}

impl ObjectImpl for SettingPanel {
    fn constructed(&self) {
        self.parent_constructed();

        let layout = self
            .instance()
            .layout_manager()
            .unwrap()
            .downcast::<gtk::BoxLayout>()
            .unwrap();

        layout.set_orientation(gtk::Orientation::Horizontal);
    }
}

impl WidgetImpl for SettingPanel {}
