mod imp;

use crate::{constant::GtkMouseButton, NativeNodeObject};
use gtk::{
    glib::{self, clone},
    prelude::*,
    subclass::prelude::ObjectSubclassIsExt,
    traits::WidgetExt,
    EventControllerKey, EventControllerMotion, EventControllerScroll, EventControllerScrollFlags,
    GestureClick, Inhibit,
};
use lazy_static::__Deref;

glib::wrapper! {
    pub struct NativeTerminalEmulator(ObjectSubclass<imp::NativeTerminalEmulator>)
        @extends gtk::Widget,
        @implements gtk::Accessible, gtk::Buildable, gtk::ConstraintTarget;
}

impl NativeTerminalEmulator {
    /// Initialize the keyboard/mouse events reaction of NativeTerminalEmulator
    pub fn setup_callbakcs(&self) {
        self.set_can_focus(true);
        self.set_focusable(true);
        self.set_focus_on_click(true);

        self.connect_has_focus_notify(|terminal| {
            if terminal.has_focus() {
                println!("Terminal emulator grab focus.");
            } else {
                println!("Terminal emulator lose focus.");
            }
        });

        //// Key events
        let key_controller = EventControllerKey::new();
        let native_node_weak = self.imp().native_node_object.borrow().downgrade();
        let widget = self.clone();
        key_controller.connect_key_pressed(move |controller, key, keycode, modifier| {
            if let Some(_) = controller.im_context() {
                return Inhibit(false);
            }
            if let Some(native_node) = native_node_weak.upgrade() {
                native_node.react_key_pressed_event(key, keycode, modifier);
            }
            widget.imp().shortcut_watcher.watch(&widget, keycode);
            Inhibit(true)
        });
        key_controller.connect_key_released(
            clone!(@weak self as terminal => move |_controller, key, keycode, modifier| {
                terminal
                    .imp()
                    .native_node_object
                    .borrow()
                    .react_key_released_event(key, keycode, modifier);
            }),
        );
        self.add_controller(&key_controller);

        //// Mouse click events
        // Left click
        let gesture_click = GestureClick::new();
        gesture_click.set_button(GtkMouseButton::Left as u32);
        gesture_click.connect_pressed(
            clone!(@weak self as terminal => move |_gesture, n_press, x, y| {
                terminal.grab_focus();

                terminal
                    .imp()
                    .native_node_object
                    .borrow()
                    .request_focus(true);
                terminal
                    .imp()
                    .native_node_object.borrow()
                    .react_mouse_pressed_event(n_press, x, y, GtkMouseButton::Left);

                terminal.imp().last_left_mouse_pressed_position.set((x as i32, y as i32));
            }),
        );
        gesture_click.connect_released(
            clone!(@weak self as terminal => move |_gesture, n_press, x, y| {
                terminal
                    .imp()
                    .native_node_object
                    .borrow()
                    .react_mouse_released_event(n_press, x, y, GtkMouseButton::Left);

                terminal.imp().last_left_mouse_release_position.set((x as i32, y as i32));
            }),
        );
        self.add_controller(&gesture_click);

        // Right click
        let gesture_click = GestureClick::new();
        gesture_click.set_button(GtkMouseButton::Right as u32);
        gesture_click.connect_pressed(
            clone!(@weak self as terminal => move |_gesture, n_press, x, y| {
                terminal.grab_focus();
                terminal
                    .imp()
                    .native_node_object
                    .borrow()
                    .request_focus(true);
                terminal
                    .imp()
                    .native_node_object.borrow()
                    .react_mouse_pressed_event(n_press, x, y, GtkMouseButton::Right);

                terminal.imp().last_right_mouse_pressed_position.set((x as i32, y as i32));
            }),
        );
        gesture_click.connect_released(
            clone!(@weak self as terminal => move |_gesture, n_press, x, y| {
                terminal
                    .imp()
                    .native_node_object
                    .borrow()
                    .react_mouse_released_event(n_press, x, y, GtkMouseButton::Right);

                terminal.imp().last_right_mouse_release_position.set((x as i32, y as i32));
            }),
        );
        self.add_controller(&gesture_click);

        //// Mouse motion events
        let motion_controller = EventControllerMotion::new();
        motion_controller.connect_enter(clone!(@weak self as terminal => move |_motion, x, y| {
            terminal
                .imp()
                .native_node_object
                .borrow()
                .react_mouse_motion_enter(x, y);
        }));
        motion_controller.connect_leave(clone!(@weak self as terminal => move |_motion| {
            terminal
                .imp()
                .native_node_object
                .borrow()
                .react_mouse_motion_leave();
        }));
        motion_controller.connect_motion(clone!(@weak self as terminal => move |_motion, x, y| {
            terminal
                .imp()
                .native_node_object
                .borrow()
                .react_mouse_motion_move(x, y);
        }));
        self.add_controller(&motion_controller);

        //// Mouse wheel events
        let wheel_controller = EventControllerScroll::new(EventControllerScrollFlags::VERTICAL);
        let native_node_weak = self.imp().native_node_object.borrow().downgrade();
        wheel_controller.connect_scroll(move |_scroll, x, y| {
            if let Some(native_node) = native_node_weak.upgrade() {
                native_node.react_mouse_wheel(x, y);
            }
            Inhibit(true)
        });
        self.add_controller(&wheel_controller);
    }

    pub fn create_ssh_session(
        &self,
        session_id: u64,
        host: &str,
        user: &str,
        password: &str,
        timestmap: u64,
    ) {
        self.imp()
            .native_node_object
            .borrow()
            .create_ssh_session(session_id, host, user, password, timestmap);
    }

    pub fn shell_startup(&self, session_id: u64, param: &str, timestamp: u64) {
        self.imp()
            .native_node_object
            .borrow()
            .shell_startup(session_id, param, timestamp)
    }

    pub fn with_node<F>(&self, f: F)
    where
        F: Fn(&NativeNodeObject),
    {
        f(self.imp().native_node_object.borrow().deref());
    }

    /// Resize the `NativeNode`.
    pub fn resize(&self, width: i32, height: i32) {
        self.imp().native_node_object.borrow().resize(width, height);
    }

    /// Terminate the native node.
    pub fn terminate(&self) {
        self.imp().native_node_object.borrow().terminate();
    }

    /// Last left mouse pressed position
    pub fn last_left_mouse_pressed_position(&self) -> (i32, i32) {
        self.imp().last_left_mouse_pressed_position.get()
    }

    /// Last right mouse pressed position
    pub fn last_right_mouse_pressed_position(&self) -> (i32, i32) {
        self.imp().last_right_mouse_pressed_position.get()
    }

    /// Last left mouse release position
    pub fn last_left_mouse_release_position(&self) -> (i32, i32) {
        self.imp().last_left_mouse_release_position.get()
    }

    /// Last right mouse release position
    pub fn last_right_mouse_release_position(&self) -> (i32, i32) {
        self.imp().last_right_mouse_release_position.get()
    }
}
