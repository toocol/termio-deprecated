mod imp;

use gtk::{
    glib::{self, clone::Downgrade},
    subclass::prelude::ObjectSubclassIsExt,
    traits::WidgetExt,
    EventControllerKey, EventControllerMotion, EventControllerScroll, EventControllerScrollFlags,
    GestureClick, Inhibit,
};
use platform::native_node::NativeNodeImpl;

glib::wrapper! {
    pub struct NativeTerminalEmulator(ObjectSubclass<imp::NativeTerminalEmulator>)
        @extends gtk::Widget,
        @implements gtk::Accessible, gtk::Buildable, gtk::ConstraintTarget;
}

impl NativeTerminalEmulator {
    /// Connect `NativeNode` to native shared memory server.
    pub fn connect_native(&self, _width: i32, _height: i32) {
       imp::NativeTerminalEmulator::connect(self.imp().native_node_object.clone());
    }

    /// Initialize the keyboard/mouse events reaction of NativeTerminalEmulator
    pub fn initialize(&self) {
        self.set_focusable(true);
        self.set_focus_on_click(true);

        //// Key events
        let key_controller = EventControllerKey::new();
        let native_node_weak = self.imp().native_node_object.borrow().downgrade();
        key_controller.connect_key_pressed(move |controller, key, keycode, modifier| {
            if let Some(_) = controller.im_context() {
                return Inhibit(false);
            }
            if let Some(native_node) = native_node_weak.upgrade() {
                native_node.react_key_pressed_event(key, keycode, modifier);
            }
            Inhibit(false)
        });
        let native_node_weak = self.imp().native_node_object.borrow().downgrade();
        key_controller.connect_key_released(move |_controller, key, keycode, modifier| {
            if let Some(native_node) = native_node_weak.upgrade() {
                native_node.react_key_released_event(key, keycode, modifier);
            }
        });
        self.add_controller(&key_controller);

        //// Mouse click events
        let gesture_click = GestureClick::new();
        let native_node_weak = self.imp().native_node_object.borrow().downgrade();
        gesture_click.connect_pressed(move |_gesture, n_press, x, y| {
            if let Some(native_node) = native_node_weak.upgrade() {
                native_node.react_mouse_pressed_event(n_press, x, y);
            }
        });
        let native_node_weak = self.imp().native_node_object.borrow().downgrade();
        gesture_click.connect_released(move |_gesture, n_press, x, y| {
            if let Some(native_node) = native_node_weak.upgrade() {
                native_node.react_mouse_released_event(n_press, x, y);
            }
        });
        self.add_controller(&gesture_click);

        //// Mouse motion events
        let motion_controller = EventControllerMotion::new();
        let native_node_weak = self.imp().native_node_object.borrow().downgrade();
        motion_controller.connect_enter(move |_motion, x, y| {
            if let Some(native_node) = native_node_weak.upgrade() {
                native_node.react_mouse_motion_enter(x, y);
            }
        });
        let native_node_weak = self.imp().native_node_object.borrow().downgrade();
        motion_controller.connect_leave(move |_motion| {
            if let Some(native_node) = native_node_weak.upgrade() {
                native_node.react_mouse_motion_leave();
            }
        });
        let native_node_weak = self.imp().native_node_object.borrow().downgrade();
        motion_controller.connect_motion(move |_motion, x, y| {
            if let Some(native_node) = native_node_weak.upgrade() {
                native_node.react_mouse_motion_move(x, y);
            }
        });
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
}