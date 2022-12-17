use utilities::TimeStamp;

use crate::{
    native_create_ssh_session, native_fire_key_pressed_event, native_fire_key_released_event,
    native_fire_mouse_entered_event, native_fire_mouse_exited_event, native_fire_mouse_move_event,
    native_fire_mouse_pressed_event, native_fire_mouse_released_event,
    native_fire_mouse_wheel_event, native_request_focus, native_shell_startup,
};

const ERROR_MSG: &str = "`CrossProcessEvent` type mismatch";

pub fn cross_process_event_dispatch(evt: CrossProcessEvent) {
    let mut evt = evt;
    match evt.event_type() {
        EventType::KeyPressedEvent => {
            native_fire_key_pressed_event(
                evt.key(),
                evt.characters().as_str(),
                evt.key_code(),
                evt.modifier(),
                TimeStamp::timestamp() as i64,
            );
        }
        EventType::KeyReleasedEvent => {
            native_fire_key_released_event(
                evt.key(),
                evt.characters().as_str(),
                evt.key_code(),
                evt.modifier(),
                TimeStamp::timestamp() as i64,
            );
        }
        EventType::MousePressedEvent => {
            native_fire_mouse_pressed_event(
                evt.key(),
                evt.x(),
                evt.y(),
                evt.button(),
                evt.modifier(),
                TimeStamp::timestamp() as i64,
            );
        }
        EventType::MouseReleasedEvent => {
            native_fire_mouse_released_event(
                evt.key(),
                evt.x(),
                evt.y(),
                evt.button(),
                evt.modifier(),
                TimeStamp::timestamp() as i64,
            );
        }
        EventType::MouseEnterEvent => {
            native_fire_mouse_entered_event(
                evt.key(),
                evt.x(),
                evt.y(),
                evt.modifier(),
                TimeStamp::timestamp() as i64,
            );
        }
        EventType::MouseLeaveEvent => {
            native_fire_mouse_exited_event(
                evt.key(),
                evt.modifier(),
                TimeStamp::timestamp() as i64,
            );
        }
        EventType::MouseMoveEvent => {
            native_fire_mouse_move_event(
                evt.key(),
                evt.x(),
                evt.y(),
                evt.modifier(),
                TimeStamp::timestamp() as i64,
            );
        }
        EventType::MouseWheelEvent => {
            native_fire_mouse_wheel_event(
                evt.key(),
                evt.x(),
                evt.y(),
                evt.amount(),
                evt.modifier(),
                TimeStamp::timestamp() as i64,
            );
        }
        EventType::RequestFocusEvent => {
            native_request_focus(evt.key(), evt.is_focus(), TimeStamp::timestamp() as i64);
        }
        EventType::CreateSshSessionEvent => {
            native_create_ssh_session(
                evt.key(),
                evt.session_id() as i64,
                evt.host().as_str(),
                evt.user().as_str(),
                evt.password().as_str(),
                TimeStamp::timestamp() as i64,
            );
        }
        EventType::ShellStartupEvent => {
            native_shell_startup(
                evt.key(),
                evt.session_id() as i64,
                evt.param().as_str(),
                TimeStamp::timestamp() as i64,
            );
        }
        _ => unimplemented!(),
    }
}

#[derive(Default)]
#[repr(u8)]
pub enum EventType {
    #[default]
    None = 0,
    KeyPressedEvent,
    KeyReleasedEvent,
    MousePressedEvent,
    MouseReleasedEvent,
    MouseEnterEvent,
    MouseLeaveEvent,
    MouseMoveEvent,
    MouseWheelEvent,
    RequestFocusEvent,

    CreateSshSessionEvent,
    ShellStartupEvent,
}

#[derive(Default)]
pub struct CrossProcessEvent {
    event_type: EventType,
    key: i32,
    characters: Option<String>,
    key_code: Option<i32>,
    modifier: Option<i32>,
    button: Option<i32>,
    n_press: Option<i32>,
    x: Option<f64>,
    y: Option<f64>,
    amount: Option<f64>,
    is_focus: Option<bool>,
    session_id: Option<u64>,
    host: Option<String>,
    user: Option<String>,
    password: Option<String>,
    param: Option<String>,
}

impl CrossProcessEvent {
    pub fn new_key_pressed_event(
        key: i32,
        characters: String,
        key_code: i32,
        modifier: i32,
    ) -> Self {
        let mut evt = CrossProcessEvent::default();
        evt.event_type = EventType::KeyPressedEvent;
        evt.key = key;
        evt.characters.replace(characters);
        evt.key_code.replace(key_code);
        evt.modifier.replace(modifier);
        evt
    }

    pub fn new_key_released_event(
        key: i32,
        characters: String,
        key_code: i32,
        modifier: i32,
    ) -> Self {
        let mut evt = CrossProcessEvent::default();
        evt.event_type = EventType::KeyReleasedEvent;
        evt.key = key;
        evt.characters.replace(characters);
        evt.key_code.replace(key_code);
        evt.modifier.replace(modifier);
        evt
    }

    pub fn new_mouse_pressed_event(key: i32, button: i32, x: f64, y: f64, modifier: i32) -> Self {
        let mut evt = CrossProcessEvent::default();
        evt.event_type = EventType::MousePressedEvent;
        evt.key = key;
        evt.button.replace(button);
        evt.x.replace(x);
        evt.y.replace(y);
        evt.modifier.replace(modifier);
        evt
    }

    pub fn new_mouse_released_event(key: i32, button: i32, x: f64, y: f64, modifier: i32) -> Self {
        let mut evt = CrossProcessEvent::default();
        evt.event_type = EventType::MouseReleasedEvent;
        evt.key = key;
        evt.button.replace(button);
        evt.x.replace(x);
        evt.y.replace(y);
        evt.modifier.replace(modifier);
        evt
    }

    pub fn new_mouse_enter_event(key: i32, x: f64, y: f64, modifier: i32) -> Self {
        let mut evt = CrossProcessEvent::default();
        evt.event_type = EventType::MouseEnterEvent;
        evt.key = key;
        evt.x.replace(x);
        evt.y.replace(y);
        evt.modifier.replace(modifier);
        evt
    }

    pub fn new_mouse_leave_event(key: i32, modifier: i32) -> Self {
        let mut evt = CrossProcessEvent::default();
        evt.event_type = EventType::MouseLeaveEvent;
        evt.key = key;
        evt.modifier.replace(modifier);
        evt
    }

    pub fn new_mouse_move_event(key: i32, x: f64, y: f64, modifier: i32) -> Self {
        let mut evt = CrossProcessEvent::default();
        evt.event_type = EventType::MouseMoveEvent;
        evt.key = key;
        evt.x.replace(x);
        evt.y.replace(y);
        evt.modifier.replace(modifier);
        evt
    }

    pub fn new_mouse_wheel_event(key: i32, x: f64, y: f64, amount: f64, modifier: i32) -> Self {
        let mut evt = CrossProcessEvent::default();
        evt.event_type = EventType::MouseWheelEvent;
        evt.key = key;
        evt.x.replace(x);
        evt.y.replace(y);
        evt.amount.replace(amount);
        evt.modifier.replace(modifier);
        evt
    }

    pub fn new_request_focus_event(key: i32, is_focus: bool) -> Self {
        let mut evt = CrossProcessEvent::default();
        evt.event_type = EventType::RequestFocusEvent;
        evt.key = key;
        evt.is_focus.replace(is_focus);
        evt
    }

    pub fn new_create_ssh_session_event(
        key: i32,
        session_id: u64,
        host: &str,
        user: &str,
        password: &str,
    ) -> Self {
        let mut evt = CrossProcessEvent::default();
        evt.event_type = EventType::CreateSshSessionEvent;
        evt.key = key;
        evt.session_id.replace(session_id);
        evt.host.replace(host.to_string());
        evt.user.replace(user.to_string());
        evt.password.replace(password.to_string());
        evt
    }

    pub fn new_shell_startup_event(key: i32, session_id: u64, param: &str) -> Self {
        let mut evt = CrossProcessEvent::default();
        evt.event_type = EventType::ShellStartupEvent;
        evt.key = key;
        evt.session_id.replace(session_id);
        evt.param.replace(param.to_string());
        evt
    }

    pub fn event_type(&self) -> &EventType {
        &self.event_type
    }

    pub fn key(&self) -> i32 {
        self.key
    }

    pub fn characters(&mut self) -> String {
        self.characters.take().expect(ERROR_MSG)
    }

    pub fn key_code(&mut self) -> i32 {
        self.key_code.take().expect(ERROR_MSG)
    }

    pub fn modifier(&mut self) -> i32 {
        self.modifier.take().expect(ERROR_MSG)
    }

    pub fn button(&mut self) -> i32 {
        self.button.take().expect(ERROR_MSG)
    }

    pub fn n_press(&mut self) -> i32 {
        self.n_press.take().expect(ERROR_MSG)
    }

    pub fn x(&mut self) -> f64 {
        self.x.take().expect(ERROR_MSG)
    }

    pub fn y(&mut self) -> f64 {
        self.y.take().expect(ERROR_MSG)
    }

    pub fn amount(&mut self) -> f64 {
        self.amount.take().expect(ERROR_MSG)
    }

    pub fn is_focus(&mut self) -> bool {
        self.is_focus.take().expect(ERROR_MSG)
    }

    pub fn session_id(&mut self) -> u64 {
        self.session_id.take().expect(ERROR_MSG)
    }

    pub fn host(&mut self) -> String {
        self.host.take().expect(ERROR_MSG)
    }

    pub fn user(&mut self) -> String {
        self.user.take().expect(ERROR_MSG)
    }

    pub fn password(&mut self) -> String {
        self.password.take().expect(ERROR_MSG)
    }

    pub fn param(&mut self) -> String {
        self.param.take().expect(ERROR_MSG)
    }
}

unsafe impl Send for CrossProcessEvent {}
unsafe impl Sync for CrossProcessEvent {}
