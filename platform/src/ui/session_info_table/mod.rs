mod imp;

use kernel::ProtocolType;

use gtk::{
    glib::{self, Object, Type},
    prelude::*,
    subclass::prelude::*,
    CellAreaBox, CellRendererText, ListStore, TreeViewColumn,
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

pub const GROUP_KEYS: [&'static str; 2] = [
    LanguageBundle::KEY_TEXT_SESSION_GROUP,
    LanguageBundle::KEY_TEXT_SESSION_SESSION_COUNT,
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

    pub fn update_session_group_info_table(&self, group_name: &str, child_num: i32) {
        let store = self
            .imp()
            .list_store
            .get()
            .expect("`list_store` of SessionInfoTable should set first before use.");
        store.clear();

        let iter = store.append();
        store.set_value(
            &iter,
            0,
            &LanguageBundle::message(GROUP_KEYS[0], None).to_value(),
        );
        store.set_value(&iter, 1, &group_name.to_value());

        let iter = store.append();
        store.set_value(
            &iter,
            0,
            &LanguageBundle::message(GROUP_KEYS[1], None).to_value(),
        );
        store.set_value(&iter, 1, &child_num.to_value());
    }

    pub fn update_session_credential_info_table(
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
        store.clear();

        let iter = store.append();
        store.set_value(
            &iter,
            0,
            &LanguageBundle::message(PROPERTIE_KEYS[0], None).to_value(),
        );
        store.set_value(&iter, 1, &name.to_value());

        let iter = store.append();
        store.set_value(
            &iter,
            0,
            &LanguageBundle::message(PROPERTIE_KEYS[1], None).to_value(),
        );
        store.set_value(&iter, 1, &host.to_value());

        let iter = store.append();
        store.set_value(
            &iter,
            0,
            &LanguageBundle::message(PROPERTIE_KEYS[2], None).to_value(),
        );
        store.set_value(&iter, 1, &username.to_value());

        let iter = store.append();
        store.set_value(
            &iter,
            0,
            &LanguageBundle::message(PROPERTIE_KEYS[3], None).to_value(),
        );
        store.set_value(&iter, 1, &protocol.as_str().to_value());

        let iter = store.append();
        store.set_value(
            &iter,
            0,
            &LanguageBundle::message(PROPERTIE_KEYS[4], None).to_value(),
        );
        store.set_value(&iter, 1, &port.to_value());
    }
}
