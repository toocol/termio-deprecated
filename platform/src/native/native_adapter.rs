use std::{
    ffi::{c_char, c_int, c_longlong, CString},
    slice,
};

const IPC_NUM_NATIVE_EVT_MSG_SIZE: usize = 1024;

#[derive(Debug)]
pub struct NativeEvent {
    pub action_name: String,
    pub params: Option<Vec<String>>,
}
impl NativeEvent {
    pub fn from_bytes(bytes: *mut u8) -> Self {
        let mut evt_msg;

        unsafe {
            let bytes = slice::from_raw_parts(bytes, IPC_NUM_NATIVE_EVT_MSG_SIZE);
            let mut len = 0usize;
            for i in bytes.iter() {
                if *i == 0 {
                    break;
                }
                len += 1;
            }
            evt_msg = vec![0u8; len];

            evt_msg.copy_from_slice(&bytes[0..len]);
        }

        let evt_msg = String::from_utf8(evt_msg.to_vec())
            .expect("Transfer `evt_msg` to utf-8 string failed.");

        let mut action_name = String::new();
        let mut params = vec![];

        let mut idx = 0;
        for s in evt_msg.split(";").into_iter() {
            if idx == 0 {
                action_name = s.to_string();
            } else {
                params.push(s.to_string());
            }
            idx += 1;
        }

        let native_evt = NativeEvent {
            action_name,
            params: if params.len() == 0 {
                None
            } else {
                Some(params)
            },
        };

        native_evt
    }
}

#[link(name = "native-adapter", kind = "static")]
extern "C" {
    fn next_key() -> c_int;
    fn connect_to(name: *const c_char) -> c_int;
    fn terminate_at(key: c_int) -> bool;
    fn is_connected(key: c_int) -> bool;
    fn send_msg(key: c_int, msg: *const c_char, shared_string_type: c_int) -> *const c_char;
    fn has_native_events(key: c_int) -> bool;
    fn get_native_event(key: c_int) -> *mut u8;
    fn drop_native_event(key: c_int);
    fn resize(key: c_int, width: c_int, height: c_int);
    fn toggle_buffer(key: c_int);
    fn is_dirty(key: c_int) -> bool;
    fn redraw(key: c_int, x: c_int, y: c_int, w: c_int, h: c_int);
    fn set_dirty(key: c_int, value: bool);
    fn set_buffer_ready(key: c_int, is_buffer_ready: bool);
    fn is_buffer_ready(key: c_int) -> bool;
    fn get_w(key: c_int) -> c_int;
    fn get_h(key: c_int) -> c_int;
    fn request_focus(key: c_int, is_focus: bool, timestamp: c_longlong) -> bool;
    fn create_ssh_session(
        key: c_int,
        session_id: c_longlong,
        host: *const c_char,
        user: *const c_char,
        password: *const c_char,
        timestamp: c_longlong,
    ) -> bool;
    fn shell_startup(
        key: c_int,
        session_id: c_longlong,
        param: *const c_char,
        timestamp: c_longlong,
    ) -> bool;
    fn get_primary_buffer(key: c_int) -> *mut u8;
    fn get_secondary_buffer(key: c_int) -> *mut u8;
    fn lock(key: c_int) -> bool;
    fn lock_timeout(key: c_int, timeout: c_longlong) -> bool;
    fn unlock(key: c_int);
    fn wait_for_buffer_changes(key: c_int);
    fn has_buffer_changes(key: c_int) -> bool;
    fn buffer_status(key: c_int) -> i32;
    fn lock_buffer(key: c_int) -> bool;
    fn unlock_buffer(key: c_int);
    fn fire_mouse_pressed_event(
        key: c_int,
        n_press: i32,
        x: f64,
        y: f64,
        buttons: c_int,
        modifiers: c_int,
        timestamp: c_longlong,
    ) -> bool;
    fn fire_mouse_released_event(
        key: c_int,
        x: f64,
        y: f64,
        buttons: c_int,
        modifiers: c_int,
        timestamp: c_longlong,
    ) -> bool;
    fn fire_mouse_clicked_event(
        key: c_int,
        x: f64,
        y: f64,
        buttons: c_int,
        modifiers: c_int,
        click_count: c_int,
        timestamp: c_longlong,
    ) -> bool;
    fn fire_mouse_entered_event(
        key: c_int,
        x: f64,
        y: f64,
        modifiers: c_int,
        timestamp: c_longlong,
    ) -> bool;
    fn fire_mouse_exited_event(key: c_int, modifiers: c_int, timestamp: c_longlong) -> bool;
    fn fire_mouse_move_event(
        key: c_int,
        x: f64,
        y: f64,
        modifiers: c_int,
        timestamp: c_longlong,
    ) -> bool;
    fn fire_mouse_wheel_event(
        key: c_int,
        x: f64,
        y: f64,
        amount: f64,
        modifiers: c_int,
        timestamp: c_longlong,
    ) -> bool;
    fn fire_key_pressed_event(
        key: c_int,
        characters: *const c_char,
        key_code: c_int,
        modifiers: c_int,
        timestamp: c_longlong,
    ) -> bool;
    fn fire_key_released_event(
        key: c_int,
        characters: *const c_char,
        key_code: c_int,
        modifiers: c_int,
        timestamp: c_longlong,
    ) -> bool;
    fn fire_key_typed_event(
        key: c_int,
        characters: *const c_char,
        key_code: c_int,
        modifiers: c_int,
        timestamp: c_longlong,
    ) -> bool;
}

/// Acquire next avaliable key of connection.
pub fn native_next_key() -> i32 {
    unsafe {
        return next_key();
    }
}

/// Connect to the shared memory by name;
pub fn native_connect_to(name: &str) -> i32 {
    unsafe {
        let c_str = CString::new(name).unwrap();
        return connect_to(c_str.as_ptr());
    }
}

/// Terminate the shared memory connection by key.
pub fn native_terminate_at(key: i32) -> bool {
    unsafe {
        return terminate_at(key);
    }
}

/// Judge whether the shared memory corresponding to the key is connected.
pub fn native_is_connected(key: i32) -> bool {
    unsafe {
        return is_connected(key);
    }
}

/// Block send msg to shared memory server side with response.
pub fn native_send_msg(key: i32, msg: &str, shared_string_type: i32) -> String {
    unsafe {
        let c_str = CString::new(msg).unwrap();
        let res = send_msg(key, c_str.as_ptr(), shared_string_type);
        let res = CString::from_raw(res as *mut c_char)
            .to_str()
            .unwrap()
            .to_string();
        return res;
    }
}

/// Determain whether has native events.
pub fn native_has_native_events(key: i32) -> bool {
    unsafe { has_native_events(key) }
}

///  Get the native event.
pub fn native_get_native_event(key: i32) -> NativeEvent {
    unsafe {
        let bytes = get_native_event(key);
        NativeEvent::from_bytes(bytes)
    }
}

/// Drop the native event.
pub fn native_drop_native_event(key: i32) {
    unsafe { drop_native_event(key) }
}

/// Resize the teminal emulator.
pub fn native_resize(key: i32, width: i32, height: i32) {
    unsafe {
        resize(key, width, height);
    }
}

/// Toggle buffer between primary/secondary buffer.
pub fn native_toggle_buffer(key: c_int) {
    unsafe { toggle_buffer(key) }
}

/// When the native image buffer was changed, the property of dirty was true.
pub fn native_is_dirty(key: i32) -> bool {
    unsafe { is_dirty(key) }
}

/// Client request redraw the native image buffer.
pub fn native_redraw(key: i32, x: i32, y: i32, w: i32, h: i32) {
    unsafe {
        redraw(key, x, y, w, h);
    }
}

/// Set the native image buffer was dirty.
pub fn native_set_dirty(key: i32, value: bool) {
    unsafe {
        set_dirty(key, value);
    }
}

/// Set true when the native image buffer was rendering completed, set false otherwise.
pub fn native_set_buffer_ready(key: i32, is_buffer_ready: bool) {
    unsafe {
        set_buffer_ready(key, is_buffer_ready);
    }
}

/// Get the native image buffer redering state.
pub fn native_is_buffer_ready(key: i32) -> bool {
    unsafe { is_buffer_ready(key) }
}

/// Get the width of native image buffer.
pub fn native_get_w(key: i32) -> i32 {
    unsafe { get_w(key) }
}

/// Get the height of native image buffer.
pub fn native_get_h(key: i32) -> i32 {
    unsafe { get_h(key) }
}

/// Tell terminal emulator to request focus or not.
pub fn native_request_focus(key: i32, is_focus: bool, timestamp: i64) -> bool {
    unsafe { request_focus(key, is_focus, timestamp) }
}

/// Tell terminal emulator to create a ssh sesison.
pub fn native_create_ssh_session(
    key: i32,
    session_id: i64,
    host: &str,
    user: &str,
    password: &str,
    timestamp: i64,
) -> bool {
    unsafe {
        let host = CString::new(host).unwrap();
        let user = CString::new(user).unwrap();
        let password = CString::new(password).unwrap();
        create_ssh_session(
            key,
            session_id,
            host.as_ptr(),
            user.as_ptr(),
            password.as_ptr(),
            timestamp,
        )
    }
}

pub fn native_shell_startup(key: i32, session_id: i64, param: &str, timestamp: i64) -> bool {
    unsafe {
        let param = CString::new(param).unwrap();
        shell_startup(key, session_id, param.as_ptr(), timestamp)
    }
}

/// Get the primary native image buffer.
pub fn native_get_primary_buffer(key: i32) -> *mut u8 {
    unsafe { get_primary_buffer(key) }
}

/// Get the secondary native image buffer.
pub fn native_get_secondary_buffer(key: i32) -> *mut u8 {
    unsafe { get_secondary_buffer(key) }
}

/// Thread lock the common resource.
pub fn native_lock(key: i32) -> bool {
    unsafe { lock(key) }
}

/// Thread lock the common resource with timeout.
pub fn native_lock_timeout(key: i32, timeout: i64) -> bool {
    unsafe { lock_timeout(key, timeout) }
}

/// Unlock the common resource.
pub fn native_unlock(key: i32) {
    unsafe { unlock(key) }
}

/// Blocking wait for native image buffer changes.
pub fn native_wait_for_buffer_changes(key: i32) {
    unsafe { wait_for_buffer_changes(key) }
}

/// Whether the native image buffer has changed.
pub fn native_has_buffer_changes(key: i32) -> bool {
    unsafe { has_buffer_changes(key) }
}

/// Get current native image buffer status
pub fn native_buffer_status(key: i32) -> i32 {
    unsafe { buffer_status(key) }
}

/// Thread lock the primary native image buffer.
pub fn native_lock_buffer(key: i32) -> bool {
    unsafe { lock_buffer(key) }
}

/// Thread unlock the primary native image buffer.
pub fn native_unlock_buffer(key: i32) {
    unsafe {
        unlock_buffer(key);
    }
}

pub fn native_fire_mouse_pressed_event(
    key: i32,
    n_press: i32,
    x: f64,
    y: f64,
    buttons: i32,
    modifiers: i32,
    timestamp: i64,
) -> bool {
    unsafe { fire_mouse_pressed_event(key, n_press, x, y, buttons, modifiers, timestamp) }
}

pub fn native_fire_mouse_released_event(
    key: i32,
    x: f64,
    y: f64,
    buttons: i32,
    modifiers: i32,
    timestamp: i64,
) -> bool {
    unsafe { fire_mouse_released_event(key, x, y, buttons, modifiers, timestamp) }
}

pub fn native_fire_mouse_clicked_event(
    key: i32,
    x: f64,
    y: f64,
    buttons: i32,
    modifiers: i32,
    click_count: i32,
    timestamp: i64,
) -> bool {
    unsafe { fire_mouse_clicked_event(key, x, y, buttons, modifiers, click_count, timestamp) }
}

pub fn native_fire_mouse_entered_event(
    key: i32,
    x: f64,
    y: f64,
    modifiers: i32,
    timestamp: i64,
) -> bool {
    unsafe { fire_mouse_entered_event(key, x, y, modifiers, timestamp) }
}

pub fn native_fire_mouse_exited_event(key: i32, modifiers: i32, timestamp: i64) -> bool {
    unsafe { fire_mouse_exited_event(key, modifiers, timestamp) }
}

pub fn native_fire_mouse_move_event(
    key: i32,
    x: f64,
    y: f64,
    modifiers: i32,
    timestamp: i64,
) -> bool {
    unsafe { fire_mouse_move_event(key, x, y, modifiers, timestamp) }
}

pub fn native_fire_mouse_wheel_event(
    key: i32,
    x: f64,
    y: f64,
    amount: f64,
    modifiers: i32,
    timestamp: i64,
) -> bool {
    unsafe { fire_mouse_wheel_event(key, x, y, amount, modifiers, timestamp) }
}

pub fn native_fire_key_pressed_event(
    key: i32,
    characters: &str,
    key_code: i32,
    modifiers: i32,
    timestamp: i64,
) -> bool {
    unsafe {
        let characters = CString::new(characters).unwrap();
        fire_key_pressed_event(key, characters.as_ptr(), key_code, modifiers, timestamp)
    }
}

pub fn native_fire_key_released_event(
    key: i32,
    characters: &str,
    key_code: i32,
    modifiers: i32,
    timestamp: i64,
) -> bool {
    unsafe {
        let characters = CString::new(characters).unwrap();
        fire_key_released_event(key, characters.as_ptr(), key_code, modifiers, timestamp)
    }
}

pub fn native_fire_key_typed_event(
    key: i32,
    characters: &str,
    key_code: i32,
    modifiers: i32,
    timestamp: i64,
) -> bool {
    unsafe {
        let characters = CString::new(characters).unwrap();
        fire_key_typed_event(key, characters.as_ptr(), key_code, modifiers, timestamp)
    }
}
