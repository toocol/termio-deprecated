use super::{AsyncEvent, AsyncEventListener, SyncEvent, SyncEventListener};
use lazy_static::lazy_static;
use std::collections::HashMap;
use std::sync::Mutex;

/////////////////////////////// Event Dispatcher ////////////////////////////////
pub fn dispatch_sync_event(event: &dyn SyncEvent) {
    println!("Receive syn event, {}", event.type_of());
}

pub fn dispatch_async_event(event: &dyn AsyncEvent) {
    println!("Receive asyn event, {}", event.type_of());
}

lazy_static! {
    static ref SYNC_LISTENER_MAP: Mutex<HashMap<String, &'static dyn SyncEventListener>> =
        Mutex::new(HashMap::new());
    static ref ASYNC_LISTENER_MAP: Mutex<HashMap<String, &'static dyn AsyncEventListener>> =
        Mutex::new(HashMap::new());
}
