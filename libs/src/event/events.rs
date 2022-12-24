use log::warn;

use super::dispatcher::*;
use std::{any::Any, vec};

/////////////////////////////// Event ////////////////////////////////
/// The trait for SyncEvent
/// ## Example:
/// ```
/// use utilities::event::SyncEvent;
/// use std::any::Any;
///
/// struct TestSyncEvent {}
///
/// impl SyncEvent for TestSyncEvent {
///     // Event listener distinguish the events from the result of function type_of()
///     fn type_of(&self) -> &'static str {
///         "test_sync_event"
///     }
///     
///     fn as_any(&self) -> &dyn Any { self }
/// 
///     fn as_trait(self: Box<Self>) -> Box<dyn SyncEvent> { self }
/// }
/// ```
/// ---
/// And dispatch the sync event by calling marco `utilites::event:dispatch`
/// ``` ignore
/// dispatch!(event)
/// ```
/// Or dispatch by boxed event:
/// ``` ignore
/// Box::new(event).dispatch();
/// ```
pub trait SyncEvent {
    fn type_of(&self) -> &'static str;

    fn as_any(&self) -> &dyn Any;

    fn as_trait(self: Box<Self>) -> Box<dyn SyncEvent>;

    fn dispatch(self: Box<Self>) {
        dispatch_sync_event(self.as_trait());
    }
}

/// The trait for AsyncEvent
/// ## Example:
/// ```
/// use utilities::event::AsyncEvent;
/// use std::any::Any;
///
/// struct TestAsyncEvent {}
///
/// impl AsyncEvent for TestAsyncEvent {
///     // Event listener distinguish the events from the result of function type_of()
///     fn type_of(&self) -> &'static str {
///         "test_async_event"
///     }
///     
///     fn as_any(&self) -> &dyn Any { self }
/// 
///     fn as_trait(self: Box<Self>) -> Box<dyn AsyncEvent> { self }
/// }
/// ```
/// ---
/// And dispatch the async event by calling marco `utilites::event:dispatch`
/// ``` ignore
/// dispatch!(event)
/// ```
/// Or dispatch by boxed event:
/// ``` ignore
/// Box::new(event).dispatch();
/// ```
pub trait AsyncEvent: Send + Sync {
    fn type_of(&self) -> &'static str;

    fn as_any(&self) -> &dyn Any;

    fn as_trait(self: Box<Self>) -> Box<dyn AsyncEvent>;

    fn dispatch(self: Box<Self>) {
        dispatch_async_event(self.as_trait());
    }
}

/// Transfer the 'SyncEvent' trait object to the struct SyncEvent impletion 'T'.  
///
/// **Panic on type mismatch**
///
/// ## Usage
/// ```ignore
/// ...
/// let val: &TestSyncEvent = as_sync_event::<TestSyncEvent>(event);
/// ```
pub fn as_sync_event<T>(event: &dyn SyncEvent) -> &T
where
    T: SyncEvent + 'static,
{
    event.as_any().downcast_ref::<T>().expect(
        format!(
            "Sync event listener act failed on event type transfer, event = {}",
            event.type_of()
        )
        .as_str(),
    )
}

/// Transfer the 'AsyncEvent' trait object to the struct AsyncEvent impletion 'T'
///
/// **Panic on type mismatch**
///
/// ## Usage
/// ```ignore
/// ...
/// let val: &TestAsyncEvent = as_async_event::<TestAsyncEvent>(event);
/// ```
pub fn as_async_event<T>(event: &dyn AsyncEvent) -> &T
where
    T: AsyncEvent + 'static,
{
    event.as_any().downcast_ref::<T>().expect(
        format!(
            "Async event listener act failed on event type transfer, event = {}",
            event.type_of()
        )
        .as_str(),
    )
}

/////////////////////////////// Event Listener ////////////////////////////////
pub trait SyncEventListener: Sync + Send {
    fn watch(&self) -> &'static str;

    fn act_on(&self, event: &dyn SyncEvent);

    fn as_trait(self: Box<Self>) -> Box<dyn SyncEventListener>;

    fn register(self: Box<Self>) {
        match SYNC_LISTENER_MAP.lock() {
            Ok(mut map_guard) => {
                map_guard
                    .entry(self.watch())
                    .or_insert(vec![])
                    .push(self.as_trait());
            }
            Err(_) => {
                warn!(
                    "Register sync event listener failed, listener watch: {}",
                    self.watch()
                )
            }
        }
    }
}

pub trait AsyncEventListener: Sync + Send {
    fn watch(&self) -> &'static str;

    fn act_on(&self, event: &dyn AsyncEvent);

    fn as_trait(self: Box<Self>) -> Box<dyn AsyncEventListener>;

    fn register(self: Box<Self>) {
        match ASYNC_LISTENER_MAP.lock() {
            Ok(mut map_guard) => {
                map_guard
                    .entry(self.watch())
                    .or_insert(vec![])
                    .push(self.as_trait());
            }
            Err(_) => {
                warn!(
                    "Register async event listener failed, listener watch: {}",
                    self.watch()
                )
            }
        }
    }
}

// /////////////////////////////// Macros ////////////////////////////////
#[macro_export]
macro_rules! reg_listeners {
    () => {};
    ( $($x:expr),* ) => {
        {
            $(
                Box::new($x).register();
            )*
        }
     };
}

#[macro_export]
macro_rules! dispatch {
    () => {};
    ( $($x:expr),* ) => {
        {
            $(
                Box::new($x).dispatch();
            )*
        }
     };
}
