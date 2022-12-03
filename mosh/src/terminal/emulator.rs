#![allow(dead_code)]
#[derive(Debug, PartialEq, Eq, Clone)]
pub struct Emulator {}

impl Emulator {
    pub fn new() -> Self {
        Emulator {}
    }

    pub fn print(&self, output: &str) {
        print!("{}", output);
    }
}
