#![allow(dead_code)]
use std::cell::Cell;

/// TextStream was built to operate the mutable string buffer.
pub struct TextStream<'a> {
    buffer: &'a mut String,
}

impl<'a> TextStream<'a> {
    pub fn new(buffer: &'a mut String) -> Self {
        Self { buffer }
    }

    /// Append string to the buffer
    pub fn append(&mut self, text: &str) {
        self.buffer.push_str(text);
    }

    /// Append a new line to the buffer
    pub fn append_line(&mut self, text: &str) {
        self.buffer.push_str(text);
        self.buffer.push('\n');
    }

    /// Whehter the buffer is empty
    pub fn is_empty(&self) -> bool {
        self.buffer.is_empty()
    }

    /// Get the chars count
    pub fn count(&self) -> usize {
        self.buffer.chars().count()
    }

    /// Get the current buffer string reference.
    pub fn text(&self) -> &str {
        &self.buffer
    }
}

/// Struct to read string by lines.
pub struct LineReader {
    source: String,
    lines: Vec<String>,
    pos: Cell<usize>,
}

impl LineReader {
    pub fn new(source: String) -> Self {
        let mut lines = vec![];
        for line in source.split("\n") {
            lines.push(line.to_string());
        }
        Self {
            source: source,
            lines: lines,
            pos: Cell::new(0),
        }
    }

    pub fn next(&self) -> Option<&str> {
        let pos = self.pos.get();
        if pos >= self.lines.len() {
            None
        } else {
            let str = &self.lines[pos];
            self.pos.set(pos + 1);
            Some(str)
        }
    }
}

#[cfg(test)]
mod tests {
    use super::{TextStream, LineReader};

    #[test]
    fn test_text_stream() {
        let mut buffer = String::new();
        let mut stream = TextStream::new(&mut buffer);
        stream.append("Hello rust.");
        assert_eq!(stream.count(), 11);
        assert!(!stream.is_empty());
        assert_eq!("Hello rust.", stream.text());
        stream.append_line("text");
        assert_eq!("Hello rust.text\n", stream.text());
    }

    #[test]
    fn test_line_reader() {
        let source = "Hello\nrust\n!";
        let line_reader = LineReader::new(source.to_string());
        while let Some(line) = line_reader.next() {
            println!("{}", line);
        }
    }
}