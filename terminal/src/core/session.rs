#![allow(dead_code)]
use tmui::{
    prelude::*,
    tlib::{
        object::{ObjectImpl, ObjectSubclass},
        Object,
    }, graphics::figure::Color,
};
use crate::pty::ProtocolType;

#[extends_object]
#[derive(Default)]
pub struct Session {
    enviroment: Vec<String>,

    auto_close: bool,
    wanted_close: bool,

    local_tab_title_format: String,
    remote_tab_title_format: String,

    initial_working_dir: String,

    /// flag if the title/icon was changed by user
    is_title_changed: bool,
    add_to_utmp: bool,
    flow_control: bool,
    full_scripting: bool,

    program: String,
    arguments: Vec<String>,

    protocol_type: ProtocolType,
    session_group_id: i32,
    session_id: u64,
    host: String,
    user: String,
    password: String,

    has_dark_background: bool,
    modified_background: Color,

    // Zmodem
    zmodem_busy: bool,
    // zmodem_proc: Process
}
impl ObjectSubclass for Session {
    const NAME: &'static str = "Session";

    type Type = Session;

    type ParentType = Object;
}
impl ObjectImpl for Session {}
