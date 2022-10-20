use super::{AsyncEvent, AsyncEventListener, SyncEvent, SyncEventListener};
use lazy_static::{lazy_static, __Deref};
use log::warn;
use std::collections::HashMap;
use std::sync::Mutex;

/////////////////////////////// Event Dispatcher ////////////////////////////////
pub fn dispatch_sync_event(event: Box<dyn SyncEvent>) {
    match SYNC_LISTENER_MAP.lock() {
        Ok(map_guard) => {
            if let Some(listener_vec) = map_guard.get(event.type_of()) {
                for listener_box in listener_vec {
                    listener_box.act_on(event.deref())
                }
            } else {
                warn!("Sync event ({}) has none listener.", event.type_of());
            }
        }
        Err(_) => {
            warn!(
                "Dispatcher try mutex lock err, skip the processing of event: {}",
                event.type_of()
            )
        }
    }
}

pub fn dispatch_async_event(event: Box<dyn AsyncEvent>) {
    // TODO: Modify the fake async calling to real async calling.
    match ASYNC_LISTENER_MAP.lock() {
        Ok(map_guard) => {
            if let Some(listener_vec) = map_guard.get(event.type_of()) {
                for listener_box in listener_vec {
                    listener_box.act_on(event.deref())
                }
            } else {
                warn!("Sync event ({}) has none listener.", event.type_of());
            }
        }
        Err(_) => {
            warn!(
                "Dispatcher try mutex lock err, skip the processing of event: {}",
                event.type_of()
            )
        }
    }
}

lazy_static! {
    pub static ref SYNC_LISTENER_MAP: Mutex<HashMap<&'static str, Vec<Box<dyn SyncEventListener>>>> =
        Mutex::new(HashMap::new());
    pub static ref ASYNC_LISTENER_MAP: Mutex<HashMap<&'static str, Vec<Box<dyn AsyncEventListener>>>> =
        Mutex::new(HashMap::new());
}
