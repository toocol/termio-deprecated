#![allow(dead_code)]
pub struct TextStream<'a> {
    buffer: &'a mut String
}

impl<'a> TextStream<'a> {
    pub fn new(buffer: &'a mut String) -> Self {
        Self { buffer }
    }

    /// Append string to the buffer
    pub fn append(&mut self, text: &str) {
        self.buffer.push_str(text);
    }

    /// Whehter the buffer is empty
    pub fn is_empty(&self) -> bool {
        self.buffer.is_empty()
    }

    /// Get the chars count
    pub fn count(&self) -> usize {
        self.buffer.chars().count()
    }
}