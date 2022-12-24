#![allow(dead_code)]
mod dispatcher;
pub mod events;

pub use self::events::*;

#[cfg(test)]
mod tests {
    use super::{as_async_event, as_sync_event};
    use crate::{
        dispatch,
        event::{AsyncEvent, AsyncEventListener, SyncEvent, SyncEventListener},
        reg_listeners,
    };
    use std::any::Any;

    struct TestSyncEvent {
        val: u32,
    }

    const TEST_SYNC_EVENT_TYPE: &'static str = "event.sync.test";

    impl SyncEvent for TestSyncEvent {
        fn type_of(&self) -> &'static str {
            TEST_SYNC_EVENT_TYPE
        }

        fn as_any(&self) -> &dyn Any {
            self
        }

        fn as_trait(self: Box<Self>) -> Box<dyn SyncEvent> {
            self
        }
    }

    struct TestSyncEventListener1 {}

    impl SyncEventListener for TestSyncEventListener1 {
        fn watch(&self) -> &'static str {
            TEST_SYNC_EVENT_TYPE
        }

        fn act_on(&self, event: &dyn SyncEvent) {
            let evt = as_sync_event::<TestSyncEvent>(event);
            println!("[1] Success processing the event, {}", event.type_of());
            assert_eq!(evt.val, 1);
        }

        fn as_trait(self: Box<Self>) -> Box<dyn SyncEventListener> {
            self
        }
    }

    struct TestSyncEventListener2 {}

    impl SyncEventListener for TestSyncEventListener2 {
        fn watch(&self) -> &'static str {
            TEST_SYNC_EVENT_TYPE
        }

        fn act_on(&self, event: &dyn SyncEvent) {
            let evt = as_sync_event::<TestSyncEvent>(event);
            println!("[2] Success processing the event, {}", event.type_of());
            assert_eq!(evt.val, 1);
        }

        fn as_trait(self: Box<Self>) -> Box<dyn SyncEventListener> {
            self
        }
    }

    struct TestAsyncEvent {
        val: u32,
    }
    const TEST_ASYNC_EVENT_TYPE: &'static str = "event.async.test";

    impl AsyncEvent for TestAsyncEvent {
        fn type_of(&self) -> &'static str {
            TEST_ASYNC_EVENT_TYPE
        }

        fn as_any(&self) -> &dyn Any {
            self
        }

        fn as_trait(self: Box<Self>) -> Box<dyn AsyncEvent> {
            self
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

        fn as_trait(self: Box<Self>) -> Box<dyn AsyncEventListener> {
            self
        }
    }

    #[test]
    fn test_sync_event() {
        reg_listeners![TestSyncEventListener1 {}, TestSyncEventListener2 {}];
        let event = TestSyncEvent { val: 1 };
        dispatch!(event);
    }

    #[test]
    fn test_async_event() {
        reg_listeners!(TestAsyncEventListener {});
        let evt = TestAsyncEvent { val: 1 };
        dispatch!(evt);
    }

    #[test]
    fn test_mixup() {
        reg_listeners![
            TestSyncEventListener1 {},
            TestSyncEventListener2 {},
            TestAsyncEventListener {}
        ];
        let event = TestSyncEvent { val: 1 };
        let evt = TestAsyncEvent { val: 1 };
        dispatch!(event, evt);
    }
}
