#![allow(dead_code)]
#[derive(Debug, PartialEq, Eq, Clone, Copy)]
pub enum UserEventType {
    UserByteType,
    ResizeType,
}
pub trait UserEventTrait {
    const NAME: &'static str;

    fn name(&self) -> &'static str {
        Self::NAME
    }
}

#[derive(Debug, PartialEq, Eq, Clone)]
pub struct UserEvent {
    user_bytes: Option<UserBytes>,
    resize: Option<Resize>,
    event_type: UserEventType,
}

impl UserEvent {
    pub fn new_user_bytes(bytes: Vec<u8>) -> Self {
        UserEvent {
            user_bytes: Some(UserBytes::new(bytes)),
            resize: None,
            event_type: UserEventType::UserByteType,
        }
    }

    pub fn new_resize(width: i32, height: i32) -> Self {
        UserEvent {
            user_bytes: None,
            resize: Some(Resize { width, height }),
            event_type: UserEventType::ResizeType,
        }
    }

    pub fn event_type(&self) -> &UserEventType {
        &self.event_type
    }

    pub fn to_user_bytes(&self) -> &UserBytes {
        self.user_bytes
            .as_ref()
            .expect("`UserEvent` type mismatched, `user_bytes` is None.")
    }

    pub fn to_resize(&self) -> &Resize {
        self.resize
            .as_ref()
            .expect("`UserEvent` type mismatched, `resize` is None.")
    }
}

#[derive(Debug, PartialEq, Eq, Clone)]
pub struct UserBytes {
    bytes: Vec<u8>,
}
impl UserEventTrait for UserBytes {
    const NAME: &'static str = "UserBytes";
}
impl UserBytes {
    pub fn new(bytes: Vec<u8>) -> Self {
        UserBytes { bytes }
    }

    pub fn bytes(&self) -> Vec<u8> {
        self.bytes.clone()
    }
}

#[derive(Debug, PartialEq, Eq, Clone, Copy)]
pub struct Resize {
    width: i32,
    height: i32,
}
impl UserEventTrait for Resize {
    const NAME: &'static str = "Resize";
}
impl Resize {
    pub fn new(width: i32, height: i32) -> Self {
        Resize { width, height }
    }

    pub fn width(&self) -> i32 {
        self.width
    }

    pub fn height(&self) -> i32 {
        self.height
    }
}
