mod core;
mod emulation;
mod tools;
use std::{os::windows::prelude::AsRawHandle, ptr::null};

use tempfile::tempfile;

use crate::tools::system_ffi::{mmap, PROT_READ, MAP_PRIVATE, munmap};

fn main() {
    let tempfile = tempfile().unwrap();
    let ion = tempfile.as_raw_handle() as i32;
    let ptr = mmap(null(), 1024, PROT_READ, MAP_PRIVATE, ion, 0);
    munmap(ptr, 1024);
    println!("Hello, Terminal!");
}
