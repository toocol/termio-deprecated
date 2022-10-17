use std::fmt::Display;

pub trait Dispathable {
    fn dispath(&self);
}

pub trait ProduceType {
    fn type_of(&self) -> &'static str;
}

pub trait SyncEvent: Dispathable + ProduceType + Display {
    fn as_sync_event(&self) -> &dyn SyncEvent;

    fn dispath(&self) {
        EventDispathcher::dispatch_sync_event(self.as_sync_event());
    }
}

pub trait AsyncEvent: Dispathable + ProduceType + Display {
    fn as_async_event(&self) -> &dyn AsyncEvent;

    fn dispath(&self) {
        EventDispathcher::dispatch_async_event(self.as_async_event());
    }
}

pub struct EventDispathcher {}

impl EventDispathcher {
    pub fn dispatch_sync_event(event: &dyn SyncEvent) {
        println!("Receive syn event: {}", event);
    }

    pub fn dispatch_async_event(event: &dyn AsyncEvent) {
        println!("Receive syn event: {}", event);
    }
}
