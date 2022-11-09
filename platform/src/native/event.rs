const ERROR_MSG: &str = "`CrossProcessEvent` type mismatch";

#[repr(u8)]
pub enum EventType {
    KeyEvent = 0,
    MouseEvent,
}
pub struct CrossProcessEvent {
    event_type: EventType,
    key: i32,
    characters: Option<String>,
    key_code: Option<i32>,
    modifier: Option<i32>,
}

impl CrossProcessEvent {
    pub fn new_key_pressed_event(key: i32, characters: String, key_code: i32, modifier: i32) -> Self {
        CrossProcessEvent {
            event_type: EventType::KeyEvent,
            key,
            characters: Some(characters),
            key_code: Some(key_code),
            modifier: Some(modifier),
        }
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
}

unsafe impl Send for CrossProcessEvent {}
unsafe impl Sync for CrossProcessEvent {}
