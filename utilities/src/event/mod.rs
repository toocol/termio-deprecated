#![allow(dead_code)]
mod dispatcher;
pub mod events;

pub use self::events::*;

#[cfg(test)]
mod tests {
    use super::as_sync_event;
    use crate::event::{SyncEvent, SyncEventListener};
    use std::any::Any;

    struct TestSyncEvent {
        val: u32,
    }

    const TEST_SYNC_EVENT_TYPE: &'static str = "event.sync.test";

    impl SyncEvent for TestSyncEvent {
        fn as_sync_event(&self) -> &dyn SyncEvent {
            self
        }

        fn as_any(&self) -> &dyn Any {
            self
        }

        fn type_of(&self) -> &'static str {
            TEST_SYNC_EVENT_TYPE
        }
    }

    struct TestSyncEventListener {}

    impl SyncEventListener for TestSyncEventListener {
        fn act_on(&self, event: &dyn SyncEvent) {
            match as_sync_event::<TestSyncEvent>(event) {
                Some(evt) => {
                    assert_eq!(evt.val, 1);
                }
                None {} => {
                    panic!("Event listener act failed on event type transfer.")
                }
            };
        }

        fn watch(&self) -> &'static str {
            TEST_SYNC_EVENT_TYPE
        }
    }

    #[test]
    fn test_sync_event() {
        let event = TestSyncEvent { val: 1 };
        event.dispath()
    }
}
