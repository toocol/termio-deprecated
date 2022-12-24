#![allow(dead_code)]
use std::ffi::{c_int, c_long};

use libc::c_void;
use wchar::wchar_t;

pub const SEEK_CUR: i32 = 1;
pub const SEEK_END: i32 = 2;
pub const SEEK_SET: i32 = 0;

pub const PROT_NONE: i32 = 0;
pub const PROT_READ: i32 = 1;
pub const PROT_WRITE: i32 = 2;
pub const PROT_EXEC: i32 = 4;

pub const MAP_FILE: i32 = 0;
pub const MAP_SHARED: i32 = 1;
pub const MAP_PRIVATE: i32 = 2;
pub const MAP_TYPE: i32 = 0xf;
pub const MAP_FIXED: i32 = 0x10;
pub const MAP_ANONYMOUS: i32 = 0x20;
pub const MAP_ANON: i32 = MAP_ANONYMOUS;

pub const MAP_FAILED: *const c_void = &-1 as *const i32 as *const c_void;

#[link(name = "native-system", kind = "static")]
extern "C" {
    fn mmap_ffi(
        addr: *const u8,
        len: usize,
        prot: c_int,
        flags: c_int,
        fildes: c_int,
        offset_type: i64,
    ) -> *const u8;
    fn munmap_ffi(addr: *const u8, len: usize) -> c_int;
    fn chsize_ffi(file_handle: c_int, size: c_long) -> c_int;
    fn wcwidth_ffi(ucs: wchar_t) -> c_int;
    fn string_width_ffi(wstr: *const wchar_t) -> c_int;
}

pub fn mmap(
    addr: *const u8,
    len: usize,
    prot: i32,
    flags: i32,
    fildes: i32,
    offset_type: i64,
) -> *const u8 {
    unsafe { mmap_ffi(addr, len, prot, flags, fildes, offset_type) }
}

pub fn munmap(addr: *const u8, len: usize) -> i32 {
    unsafe { munmap_ffi(addr, len) }
}

pub fn chsize(file_handle: i32, size: i32) -> i32 {
    unsafe { chsize_ffi(file_handle, size) }
}

pub fn wcwidth(ucs: u16) -> c_int {
    unsafe { wcwidth_ffi(wchar_t::from(ucs)) }
}

pub fn string_width(wstr: &[wchar_t]) -> c_int {
    unsafe { string_width_ffi(wstr.as_ptr()) }
}

#[cfg(test)]
mod tests {
    use std::{os::windows::prelude::AsRawHandle, ptr::null};

    use tempfile::tempfile;

    use super::*;

    #[test]
    fn test_mmap() {
        let tempfile = tempfile().unwrap();
        let ion = tempfile.as_raw_handle() as i32;
        let ptr = mmap(null(), 1024, PROT_READ, MAP_PRIVATE, ion, 0);
        munmap(ptr, 1024);
    }
}
