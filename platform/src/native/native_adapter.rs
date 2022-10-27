use std::ffi::CString;

#[link(name = "native-adapter", kind = "dylib")]
extern "C" {
    fn next_key() -> i32;
    fn connect_to(name: *const i8) -> i32;
    fn terminate_at(key: i32) -> bool;
    fn is_connected(key: i32) -> bool;
    fn send_msg(key: i32, msg: *const i8, shared_string_type: i32) -> *const i8;
    fn process_native_events(key: i32);
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
        let res = CString::from_raw(res as *mut i8).to_str().unwrap().to_string();
        return res;
    }
}

/// Process the native events which store in the shared memory.
pub fn native_process_native_events(key: i32) {
    unsafe {
        process_native_events(key);
    }
}