use std::ffi::{CString, c_int, c_char, c_longlong};

#[link(name = "native-adapter")]
extern "C" {
    fn next_key() -> c_int;
    fn connect_to(name: *const c_char) -> c_int;
    fn terminate_at(key: c_int) -> bool;
    fn is_connected(key: c_int) -> bool;
    fn send_msg(key: c_int, msg: *const c_char, shared_string_type: c_int) -> *const c_char;
    fn process_native_events(key: c_int);
    fn resize(key: c_int, width: c_int, height: c_int);
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
    fn get_buffer(key: c_int) -> *const u8;
    fn lock(key: c_int) -> bool;
    fn lock_timeout(key: c_int, timeout: c_longlong) -> bool;
    fn unlock(key: c_int);
    fn wait_for_buffer_changes(key: c_int);
    fn has_buffer_changes(key: c_int) -> bool;
    fn lock_buffer(key: c_int);
    fn unlock_buffer(key: c_int);
    fn fire_mouse_pressed_event(
        key: c_int,
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
        buttons: c_int,
        modifiers: c_int,
        click_count: c_int,
        timestamp: c_longlong,
    ) -> bool;
    fn fire_mouse_exited_event(
        key: c_int,
        x: f64,
        y: f64,
        buttons: c_int,
        modifiers: c_int,
        click_count: c_int,
        timestamp: c_longlong,
    ) -> bool;
    fn fire_mouse_move_event(
        key: c_int,
        x: f64,
        y: f64,
        buttons: c_int,
        modifiers: c_int,
        timestamp: c_longlong,
    ) -> bool;
    fn fire_mouse_wheel_event(
        key: c_int,
        x: f64,
        y: f64,
        amount: f64,
        buttons: c_int,
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

/// Process the native events which store in the shared memory.
pub fn native_process_native_events(key: i32) {
    unsafe {
        process_native_events(key);
    }
}

/// Resize the teminal emulator.
pub fn native_resize(key: i32, width: i32, height: i32) {
    unsafe {
        resize(key, width, height);
    }
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

/// Get the native image buffer.
pub fn native_get_buffer(key: i32) -> *const u8 {
    unsafe { get_buffer(key) }
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

/// Thread lock the native image buffer.
pub fn native_lock_buffer(key: i32) {
    unsafe {
        lock_buffer(key);
    }
}

/// Thread unlock the native image buffer.
pub fn native_unlock_buffer(key: i32) {
    unsafe {
        unlock_buffer(key);
    }
}

pub fn native_fire_mouse_pressed_event(
    key: i32,
    x: f64,
    y: f64,
    buttons: i32,
    modifiers: i32,
    timestamp: i64,
) -> bool {
    unsafe { fire_mouse_pressed_event(key, x, y, buttons, modifiers, timestamp) }
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
    buttons: i32,
    modifiers: i32,
    click_count: i32,
    timestamp: i64,
) -> bool {
    unsafe { fire_mouse_entered_event(key, x, y, buttons, modifiers, click_count, timestamp) }
}

pub fn native_fire_mouse_exited_event(
    key: i32,
    x: f64,
    y: f64,
    buttons: i32,
    modifiers: i32,
    click_count: i32,
    timestamp: i64,
) -> bool {
    unsafe { fire_mouse_exited_event(key, x, y, buttons, modifiers, click_count, timestamp) }
}

pub fn native_fire_mouse_move_event(
    key: i32,
    x: f64,
    y: f64,
    buttons: i32,
    modifiers: i32,
    timestamp: i64,
) -> bool {
    unsafe { fire_mouse_move_event(key, x, y, buttons, modifiers, timestamp) }
}

pub fn native_fire_mouse_wheel_event(
    key: i32,
    x: f64,
    y: f64,
    amount: f64,
    buttons: i32,
    modifiers: i32,
    timestamp: i64,
) -> bool {
    unsafe { fire_mouse_wheel_event(key, x, y, amount, buttons, modifiers, timestamp) }
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
