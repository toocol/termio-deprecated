mod imp;

use core::ProtocolType;

use gtk::{
    glib::{self, Object, Type},
    prelude::*,
    subclass::prelude::*,
    CellAreaBox, CellRendererText, ListStore, TreeViewColumn, TreePath,
};
use utilities::DynamicBundle;

use crate::LanguageBundle;

glib::wrapper! {
    pub struct SessionInfoTable(ObjectSubclass<imp::SessionInfoTable>)
        @extends gtk::TreeView, gtk::Widget,
        @implements gtk::Accessible, gtk::Buildable, gtk::ConstraintTarget, gtk::Scrollable;
}

pub const PROPERTIE_KEYS: [&'static str; 5] = [
    LanguageBundle::KEY_TEXT_SESSION_INFO_NAME,
    LanguageBundle::KEY_TEXT_SESSION_INFO_HOST,
    LanguageBundle::KEY_TEXT_SESSION_INFO_USERNAME,
    LanguageBundle::KEY_TEXT_SESSION_INFO_PROTOCOL,
    LanguageBundle::KEY_TEXT_SESSION_INFO_PORT,
];

impl SessionInfoTable {
    pub fn new() -> Self {
        Object::new(&[])
    }

    pub fn setup_columns(&self) {
        // Column 0: key
        let cell_renderer = CellRendererText::new();
        let cell_area = CellAreaBox::builder()
            .orientation(gtk::Orientation::Vertical)
            .build();
        cell_area.pack_start(&cell_renderer, false, true, true);
        let column = TreeViewColumn::builder()
            .cell_area(&cell_area)
            .expand(false)
            .build();
        column.add_attribute(&cell_renderer, "text", 0);
        self.append_column(&column);

        // Column 1: val
        let cell_renderer = CellRendererText::new();
        let cell_area = CellAreaBox::builder()
            .orientation(gtk::Orientation::Vertical)
            .build();
        cell_area.pack_start(&cell_renderer, false, true, true);
        let column = TreeViewColumn::builder()
            .cell_area(&cell_area)
            .expand(false)
            .build();
        column.add_attribute(&cell_renderer, "text", 1);
        self.append_column(&column);
    }

    pub fn setup_model(&self) {
        let list_store = ListStore::new(&[Type::STRING, Type::STRING]);
        self.set_model(Some(&list_store));
        self.imp()
            .list_store
            .set(list_store)
            .expect("`list_store` of SessionInfoTable can only set once.")
    }

    pub fn create_session_info_table(&self) {
        let store = self
            .imp()
            .list_store
            .get()
            .expect("`list_store` of SessionInfoTable should set first before use.");
        for key in PROPERTIE_KEYS {
            let iter = store.append();
            store.set_value(&iter, 0, &LanguageBundle::message(key, None).to_value());
            store.set_value(&iter, 1, &"".to_value());
        }
    }

    pub fn update_session_info_table(
        &self,
        name: &str,
        host: &str,
        username: &str,
        protocol: ProtocolType,
        port: u32,
    ) {
        let store = self
            .imp()
            .list_store
            .get()
            .expect("`list_store` of SessionInfoTable should set first before use.");
        let mut tree_path = TreePath::new_first();
        let mut iter_opt = store.iter(&tree_path);
        if let Some(iter) = iter_opt.as_ref() {
            store.set_value(iter, 1, &name.to_value());
            if store.iter_next(iter) {
                tree_path.next();
                iter_opt = store.iter(&tree_path);
            }
        }

        if let Some(iter) = iter_opt.as_ref() {
            store.set_value(iter, 1, &host.to_value());
            if store.iter_next(iter) {
                tree_path.next();
                iter_opt = store.iter(&tree_path);
            }
        }

        if let Some(iter) = iter_opt.as_ref() {
            store.set_value(iter, 1, &username.to_value());
            if store.iter_next(iter) {
                tree_path.next();
                iter_opt = store.iter(&tree_path);
            }
        }

        if let Some(iter) = iter_opt.as_ref() {
            store.set_value(iter, 1, &protocol.as_str().to_value());
            if store.iter_next(iter) {
                tree_path.next();
                iter_opt = store.iter(&tree_path);
            }
        }

        if let Some(iter) = iter_opt.as_ref() {
            store.set_value(iter, 1, &port.to_value());
        }
    }
}
