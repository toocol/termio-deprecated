#![allow(dead_code)]
mod dispatcher;
pub mod events;

pub use self::events::*;

#[cfg(test)]
mod tests {
    use super::{as_async_event, as_sync_event};
    use crate::{
        event::{AsyncEvent, AsyncEventListener, Dispatchable, SyncEvent, SyncEventListener},
        reg_async_listeners, reg_sync_listeners,
    };
    use std::any::Any;

    #[derive(Dispatchable)]
    struct TestSyncEvent {
        val: u32,
    }

    const TEST_SYNC_EVENT_TYPE: &'static str = "event.sync.test";

    impl SyncEvent for TestSyncEvent {
        fn as_any(&self) -> &dyn Any {
            self
        }

        fn type_of(&self) -> &'static str {
            TEST_SYNC_EVENT_TYPE
        }
    }

    struct TestSyncEventListener {}

    impl SyncEventListener for TestSyncEventListener {
        fn watch(&self) -> &'static str {
            TEST_SYNC_EVENT_TYPE
        }

        fn act_on(&self, event: &dyn SyncEvent) {
            let evt = as_sync_event::<TestSyncEvent>(event);
            println!("Success processing the event, {}", event.type_of());
            assert_eq!(evt.val, 1);
        }
    }

    #[derive(Dispatchable)]
    struct TestAsyncEvent {
        val: u32,
    }
    const TEST_ASYNC_EVENT_TYPE: &'static str = "event.async.test";

    impl AsyncEvent for TestAsyncEvent {
        fn as_any(&self) -> &dyn Any {
            self
        }

        fn type_of(&self) -> &'static str {
            TEST_ASYNC_EVENT_TYPE
        }
    }

    struct TestAsyncEventListener {}

    impl AsyncEventListener for TestAsyncEventListener {
        fn watch(&self) -> &'static str {
            TEST_ASYNC_EVENT_TYPE
        }

        fn act_on(&self, event: &dyn AsyncEvent) {
            let evt = as_async_event::<TestAsyncEvent>(event);
            println!("Success processing the event, {}", event.type_of());
            assert_eq!(evt.val, 1);
        }
    }

    #[test]
    fn test_sync_event() {
        reg_sync_listeners![TestSyncEventListener {}];
        let event = TestSyncEvent { val: 1 };
        TestSyncEvent::dispatch_sync(&event);
    }

    #[test]
    fn test_async_event() {
        reg_async_listeners!(TestAsyncEventListener {});
        let evt = TestAsyncEvent { val: 1 };
        TestAsyncEvent::dispath_async(Box::new(evt));
    }
}
