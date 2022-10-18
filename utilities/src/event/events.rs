use std::any::Any;

use super::dispatcher::*;

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
///     fn as_sync_event(&self) -> &dyn SyncEvent { self }
/// }
/// ```
pub trait SyncEvent {
    fn type_of(&self) -> &'static str;

    fn as_sync_event(&self) -> &dyn SyncEvent;

    fn as_any(&self) -> &dyn Any;

    fn dispath(&self) {
        dispatch_sync_event(self.as_sync_event());
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
///     fn as_async_event(&self) -> &dyn AsyncEvent { self }
/// }
/// ```
pub trait AsyncEvent {
    fn type_of(&self) -> &'static str;

    fn as_async_event(&self) -> &dyn AsyncEvent;

    fn as_any(&self) -> &dyn Any;

    fn dispath(&self) {
        dispatch_async_event(self.as_async_event());
    }
}

/// Transfer the 'SyncEvent' trait object to the struct SyncEvent impletion 'T'
/// ## Usage
/// ```ignore
/// ...
/// let val_opt: Option<&TestSyncEvent> = as_sync_event::<TestSyncEvent>(event);
/// ```
pub fn as_sync_event<T>(event: &dyn SyncEvent) -> Option<&T>
where
    T: SyncEvent + 'static,
{
    event.as_any().downcast_ref::<T>()
}

/// Transfer the 'AsyncEvent' trait object to the struct AsyncEvent impletion 'T'
/// ## Usage
/// ```ignore
/// ...
/// let val_opt: Option<&TestAsyncEvent> = as_async_event::<TestAsyncEvent>(event);
/// ```
pub fn as_async_event<T>(event: &dyn AsyncEvent) -> Option<&T>
where
    T: AsyncEvent + 'static,
{
    event.as_any().downcast_ref::<T>()
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

/////////////////////////////// Macros ////////////////////////////////
pub trait RegisterListener {
    fn process(self);
}

impl RegisterListener for Box<dyn SyncEventListener> {
    fn process(self) {
        SYNC_LISTENER_MAP.lock().unwrap().insert(self.watch(), self);
    }
}

impl RegisterListener for Box<dyn AsyncEventListener> {
    fn process(self) {
        ASYNC_LISTENER_MAP
            .lock()
            .unwrap()
            .insert(self.watch(), self);
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