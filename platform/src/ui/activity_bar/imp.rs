use gtk::{prelude::*, subclass::prelude::*, glib};

#[derive(Default)]
pub struct ActivityBar {}

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
}

impl WidgetImpl for ActivityBar {}
