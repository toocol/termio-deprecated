mod imp;

use glib::clone;
use glib::Object;
use gtk::Adjustment;
use gtk::Frame;
use gtk::Label;
use gtk::PasswordEntry;
use gtk::SpinButton;
use gtk::{glib, prelude::*, subclass::prelude::*, Dialog, DialogFlags, Entry, ResponseType};
use log::info;

use super::TermioCommunityWindow;

glib::wrapper! {
    pub struct NewSessionDialog(ObjectSubclass<imp::NewSessionDialog>);
}

impl NewSessionDialog {
    pub fn new(parent: &TermioCommunityWindow) -> Self {
        let obj: NewSessionDialog = Object::new(&[]);
        let dialog = Dialog::with_buttons(
            Some("New Session"),
            Some(parent),
            DialogFlags::MODAL | DialogFlags::DESTROY_WITH_PARENT,
            &[
                ("Cancel", ResponseType::Cancel),
                ("Create", ResponseType::Accept),
            ],
        );
        dialog.set_default_response(ResponseType::Accept);
        dialog.add_css_class("new-session-dialog");

        // Make the dialog button insensitive initially, and add css class
        let dialog_button = dialog
            .widget_for_response(ResponseType::Accept)
            .expect("The dialog needs to have a widget for response type `Accept`.");
        dialog_button.set_sensitive(false);
        dialog_button.set_css_classes(&["new-session-dialog-button", "collection-accept"]);

        let cancel_button = dialog
            .widget_for_response(ResponseType::Cancel)
            .expect("The dialog needs to have a widget for response type `Accept`.");
        cancel_button.set_css_classes(&["new-session-dialog-button"]);

        let gtk_box = gtk::Box::builder()
            .margin_top(10)
            .margin_bottom(10)
            .margin_start(10)
            .margin_end(10)
            .spacing(15)
            .build();

        // Create entry and add it to the box
        // Remote host entry.
        let host_label = Label::builder().label("Remote Host :").build();
        let host_entry = Entry::builder()
            .placeholder_text("Remote Host")
            .activates_default(true)
            .build();
        // Username entry.
        let username_label = Label::builder().label("Username :").build();
        let username_entry = Entry::builder()
            .placeholder_text("Username")
            .activates_default(true)
            .build();
        // Password entry.
        let password_label = Label::builder().label("Password :").build();
        let password_entry = PasswordEntry::builder()
            .placeholder_text("Password")
            .activates_default(true)
            .build();
        // Port entry.
        let port_label = Label::builder().label("Port :").build();
        let port_adjustment = Adjustment::new(22., 0., 9999., 1., 5., 0.);
        let port_button = SpinButton::builder().adjustment(&port_adjustment).build();

        gtk_box.append(&host_label);
        gtk_box.append(&host_entry);
        gtk_box.append(&username_label);
        gtk_box.append(&username_entry);
        gtk_box.append(&password_label);
        gtk_box.append(&password_entry);
        gtk_box.append(&port_label);
        gtk_box.append(&port_button);
        let basic_ssh_frame = Frame::builder()
            .label("Basic SSH Settings :")
            .margin_top(10)
            .margin_bottom(10)
            .margin_start(10)
            .margin_end(10)
            .child(&gtk_box)
            .build();

        dialog.content_area().append(&basic_ssh_frame);

        // Set entry's css class to "error", when there is not text in it
        host_entry.connect_changed(
            clone!(@weak dialog, @weak username_entry => move |host_entry| {
                let text = host_entry.text();
                let dialog_button = dialog.
                    widget_for_response(ResponseType::Accept).
                    expect("The dialog needs to have a widget for response type `Accept`.");
                let empty = text.is_empty();

                dialog_button.set_sensitive(!empty && !username_entry.text().is_empty());

                if empty {
                    host_entry.add_css_class("error");
                } else {
                    host_entry.remove_css_class("error");
                }
            }),
        );
        username_entry.connect_changed(
            clone!(@weak dialog, @weak host_entry => move |username_entry| {
                let text = username_entry.text();
                let dialog_button = dialog.
                    widget_for_response(ResponseType::Accept).
                    expect("The dialog needs to have a widget for response type `Accept`.");
                let empty = text.is_empty();

                dialog_button.set_sensitive(!empty && !host_entry.text().is_empty());

                if empty {
                    username_entry.add_css_class("error");
                } else {
                    username_entry.remove_css_class("error");
                }
            }),
        );

        // Connect response to dialog
        dialog.connect_response(
            clone!(@weak parent as window, @weak host_entry, @weak username_entry, @weak password_entry, @weak port_button => move |dialog, response| {
                dialog.hide();

                if response != ResponseType::Accept {
                    return;
                }

                let host = host_entry.text().to_string();
                let username = username_entry.text();
                let password = password_entry.text();
                let port = port_button.value_as_int();
                let mut shown_name = String::new();
                shown_name.push_str(host.as_str());
                shown_name.push_str("@");
                shown_name.push_str(username.as_str());
                window.new_session_credential(shown_name.as_str(), host.as_str(), username.as_str(), password.as_str(), "default", port as u32);
                info!("Create new session credential: host={}, username={}, password={}, prot={}", host, username, password, port);
        }));

        obj.imp()
            .dialog
            .set(dialog)
            .expect("`dialog` of `NewSessionDialog` can only set once.");
        obj
    }

    pub fn show_dialog(&self) {
        self.imp()
            .dialog
            .get()
            .expect("`dialog` of `NewSessionDialog` must be initialized first before use.")
            .present();
    }

    pub fn hide_dialog(&self) {
        self.imp()
            .dialog
            .get()
            .expect("`dialog` of `NewSessionDialog` must be initialized first before use.")
            .hide();
    }
}
