use gtk::{glib, prelude::*, subclass::prelude::*};

#[derive(Default)]
pub struct CommandFeedbackItem {}

#[glib::object_subclass]
impl ObjectSubclass for CommandFeedbackItem {
    const NAME: &'static str = "CommandFeedbackItem";

    type Type = super::CommandFeedbackItem;

    type ParentType = gtk::Widget;

    fn class_init(klass: &mut Self::Class) {
        klass.set_layout_manager_type::<gtk::BoxLayout>();
    }
}

impl ObjectImpl for CommandFeedbackItem {
    fn constructed(&self) {
        self.parent_constructed();

        let layout = self
            .instance()
            .layout_manager()
            .unwrap()
            .downcast::<gtk::BoxLayout>()
            .unwrap();
        layout.set_orientation(gtk::Orientation::Vertical);
    }
}

impl WidgetImpl for CommandFeedbackItem {}
