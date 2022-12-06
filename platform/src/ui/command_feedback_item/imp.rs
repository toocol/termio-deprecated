use gtk::{
    glib::{self, once_cell::sync::OnceCell},
    prelude::*,
    subclass::prelude::*,
    Label, 
};
use crate::ShortcutLabel;

#[derive(Default)]
pub struct CommandFeedbackItem {
    pub command: OnceCell<Label>,
    pub comment: OnceCell<Label>,
    pub param: OnceCell<Label>,
    pub shortcuts: OnceCell<ShortcutLabel>,
}

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
