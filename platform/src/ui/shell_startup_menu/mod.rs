mod imp;

use gtk::{
    gdk::Rectangle,
    glib::{self, Object},
    prelude::ToValue,
    subclass::prelude::*,
    PositionType,
};

use crate::{MenuItem, MenuSection};

glib::wrapper! {
    pub struct ShellStartupMenu(ObjectSubclass<imp::ShellStartupMenu>)
        @extends gtk::Widget,
        @implements gtk::Accessible, gtk::Buildable, gtk::ConstraintTarget, gtk::Native, gtk::ShortcutManager;
}

impl ShellStartupMenu {
    pub fn builder() -> ShellStartupMenuBuilder {
        ShellStartupMenuBuilder::new()
    }

    pub fn append_item(&self, item: MenuItem) {
        self.imp()
            .model
            .get()
            .expect("`model` of `ShellStartupMenu` is None.")
            .append_item(item);
    }

    pub fn append_section(&self, section: MenuSection) {
        self.imp()
            .model
            .get()
            .expect("`model` of `ShellStartupMenu` is None.")
            .append_section(section);
    }
}

#[derive(Default)]
pub struct ShellStartupMenuBuilder {
    point_to: Option<Rectangle>,
    position: Option<PositionType>,
    has_arrow: Option<bool>,
}

impl ShellStartupMenuBuilder {
    pub fn new() -> Self {
        ShellStartupMenuBuilder::default()
    }

    pub fn build(self) -> ShellStartupMenu {
        let mut properties: Vec<(&str, &dyn ToValue)> = vec![];
        if let Some(ref point_to) = self.point_to {
            properties.push(("point-to", point_to));
        }
        if let Some(ref position) = self.position {
            properties.push(("position", position));
        }
        if let Some(ref has_arrow) = self.has_arrow {
            properties.push(("has-arrow", has_arrow));
        }
        Object::new(&properties)
    }

    pub fn point_to(mut self, point_to: Rectangle) -> Self {
        self.point_to = Some(point_to);
        self
    }

    pub fn position(mut self, position: PositionType) -> Self {
        self.position = Some(position);
        self
    }

    pub fn has_arrow(mut self, has_arrow: bool) -> Self {
        self.has_arrow = Some(has_arrow);
        self
    }
}
