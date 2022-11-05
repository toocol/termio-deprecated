use std::path::PathBuf;
use gtk::glib;

use crate::APP_ID;

pub fn data_path(file_name: &str) -> PathBuf {
    let mut path = glib::user_data_dir();
    path.push(APP_ID);
    std::fs::create_dir_all(&path).expect("Could not create data directory.");
    path.push(file_name);
    path
}