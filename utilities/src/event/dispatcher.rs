use super::{AsyncEvent, AsyncEventListener, SyncEvent, SyncEventListener};
use lazy_static::lazy_static;
use std::collections::HashMap;
use std::sync::Mutex;

/////////////////////////////// Event Dispatcher ////////////////////////////////
pub fn dispatch_sync_event(event: &dyn SyncEvent) {
    if let Some(listener_box) = SYNC_LISTENER_MAP.lock().unwrap().get(event.type_of()) {
        listener_box.act_on(event)
    }
}

pub fn dispatch_async_event(event: &dyn AsyncEvent) {
    println!("Receive asyn event, {}", event.type_of());
}

lazy_static! {
    pub static ref SYNC_LISTENER_MAP: Mutex<HashMap<&'static str, Box<dyn SyncEventListener>>> =
        Mutex::new(HashMap::new());
    pub static ref ASYNC_LISTENER_MAP: Mutex<HashMap<&'static str, Box<dyn AsyncEventListener>>> =
        Mutex::new(HashMap::new());
}
