#![allow(dead_code)]
mod events;

use std::collections::HashMap;

pub use self::events::{AsyncEvent, SyncEvent};

pub trait WatchTypes {
    fn watch(&self) -> [&'static str];
}

pub trait AsyncEventListener: WatchTypes {
    fn act_on(&self, event: &dyn AsyncEvent);
}

pub trait SyncEventListener: WatchTypes {
    fn act_on(&self, event: &dyn SyncEvent);
}

struct EventListenerContainer {
    sync_listener_map: HashMap<String, &'static dyn SyncEventListener>,
    async_listener_map: HashMap<String, &'static dyn AsyncEventListener>,
}
