pub use tproc::Dispatchable;

use super::dispatcher::*;
use std::{any::Any, vec};

/////////////////////////////// Event ////////////////////////////////
/// The trait for SyncEvent
/// ## Example:
/// ```
/// use utilities::event::{SyncEvent, Dispatchable};
/// use std::any::Any;
///
/// #[derive(Dispatchable)]
/// struct TestSyncEvent {}
///
/// impl SyncEvent for TestSyncEvent {
///     // Event listener distinguish the events from the result of function type_of()
///     fn type_of(&self) -> &'static str {
///         "test_sync_event"
///     }
///     
///     fn as_any(&self) -> &dyn Any { self }
/// }
/// ```
/// ---
/// And dispatch the sync event by calling
/// ``` ignore
/// let evt = TestSyncEvent {};
/// TestSyncEvent::dispatch(&evt);
/// ```
pub trait SyncEvent {
    fn type_of(&self) -> &'static str;

    fn as_any(&self) -> &dyn Any;
}

/// The trait for AsyncEvent
/// ## Example:
/// ```
/// use utilities::event::{AsyncEvent, Dispatchable};
/// use std::any::Any;
///
/// #[derive(Dispatchable)]
/// struct TestAsyncEvent {}
///
/// impl AsyncEvent for TestAsyncEvent {
///     // Event listener distinguish the events from the result of function type_of()
///     fn type_of(&self) -> &'static str {
///         "test_async_event"
///     }
///     
///     fn as_any(&self) -> &dyn Any { self }
/// }
/// ```
/// ---
/// And dispatch the async event by calling(the api was a little diffrent from SyncEvent):
/// ``` ignore
/// let evt = TestAsyncEvent {};
/// TestAsyncEvent::dispatch(Box::new(evt));
/// ```
pub trait AsyncEvent: Send + Sync {
    fn type_of(&self) -> &'static str;

    fn as_any(&self) -> &dyn Any;
}

/// Every sync/async event should derive this trait by proc marco `#[derive(Dispatchable)]`
/// ## Sample
/// ``` ignore
/// #[derive(Dispatchable)]
/// struct TestSyncEvent {}
/// ```
pub trait Dispatchable {
    fn dispatch_sync(evt: &dyn SyncEvent) {
        dispatch_sync_event(evt);
    }

    fn dispath_async(evt: Box<dyn AsyncEvent>) {
        dispatch_async_event(evt);
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
pub fn as_sync_event<'a, T>(event: &'a dyn SyncEvent) -> &'a T
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
pub fn as_async_event<'a, T>(event: &'a dyn AsyncEvent) -> &'a T
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
}

pub trait AsyncEventListener: Sync + Send {
    fn watch(&self) -> &'static str;

    fn act_on(&self, event: &dyn AsyncEvent);
}

// /////////////////////////////// Macros ////////////////////////////////
pub trait RegisterListener {
    fn process(self);
}

impl RegisterListener for Box<dyn SyncEventListener> {
    fn process(self) {
        match SYNC_LISTENER_MAP.lock() {
            Ok(mut map_guard) => {
                map_guard.entry(self.watch()).or_insert(vec![]).push(self);
            }
            Err(_) => {
                eprintln!(
                    "Register sync event listener failed, listener watch: {}",
                    self.watch()
                )
            }
        }
    }
}

impl RegisterListener for Box<dyn AsyncEventListener> {
    fn process(self) {
        match ASYNC_LISTENER_MAP.lock() {
            Ok(mut map_guard) => {
                map_guard.entry(self.watch()).or_insert(vec![]).push(self);
            }
            Err(_) => {
                eprintln!(
                    "Register async event listener failed, listener watch: {}",
                    self.watch()
                )
            }
        }
    }
}

#[macro_export]
macro_rules! reg_sync_listeners {
    ( $($x:expr),* ) => {
        {
            $(
                let boxed_listener = Box::new($x) as Box<dyn super::events::SyncEventListener>;
                super::events::RegisterListener::process(boxed_listener);
            )*
        }
     };
}

#[macro_export]
macro_rules! reg_async_listeners {
    ( $($x:expr),* ) => {
        {
            $(
                let boxed_listener = Box::new($x) as Box<dyn super::events::AsyncEventListener>;
                super::events::RegisterListener::process(boxed_listener);
            )*
        }
     };
}
