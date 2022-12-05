use gtk::{
    gio::ListStore,
    glib::{self, once_cell::sync::OnceCell},
    prelude::*,
    subclass::prelude::*,
    Align, ListBox,
};

use crate::ShortcutWatcher;

const DEFAULT_WIDGET_WIDTH: i32 = 500;
const DEFAULT_MARGIN_TOP: i32 = 22;

#[derive(Default)]
pub struct CommandPanel {
    pub entry: OnceCell<gtk::Entry>,
    pub feedbacks: OnceCell<ListBox>,
    pub collections: OnceCell<ListStore>,

    pub shortcut_watcher: ShortcutWatcher,
}

#[glib::object_subclass]
impl ObjectSubclass for CommandPanel {
    const NAME: &'static str = "CommandPanel";

    type Type = super::CommandPanel;

    type ParentType = gtk::Widget;

    fn class_init(klass: &mut Self::Class) {
        klass.set_layout_manager_type::<gtk::BoxLayout>();
    }
}

impl ObjectImpl for CommandPanel {
    fn constructed(&self) {
        self.parent_constructed();
        let obj = self.instance();

        let layout = self
            .instance()
            .layout_manager()
            .unwrap()
            .downcast::<gtk::BoxLayout>()
            .unwrap();

        layout.set_orientation(gtk::Orientation::Vertical);

        let entry = gtk::Entry::builder()
            .css_classes(vec!["command-panel-entry".to_string()])
            .build();
        entry.set_parent(&*obj);
        self.entry
            .set(entry)
            .expect("`entry` of CommandPanel can only set once.");

        let feedbacks = ListBox::builder()
            .css_classes(vec!["command-feedback-list-box".to_string()])
            .build();
        feedbacks.set_parent(&*obj);
        self.feedbacks
            .set(feedbacks)
            .expect("`feedbacks` of CommandPanel can only set once.");

        obj.set_width_request(DEFAULT_WIDGET_WIDTH);
        obj.set_halign(Align::Center);
        obj.set_margin_top(DEFAULT_MARGIN_TOP);

        obj.setup_collections();
        obj.setup_callbacks();
    }

    fn dispose(&self) {
        if let Some(entry) = self.entry.get() {
            entry.unparent();
        }
        if let Some(feedbacks) = self.feedbacks.get() {
            feedbacks.unparent();
        }
    }
}

impl WidgetImpl for CommandPanel {}
