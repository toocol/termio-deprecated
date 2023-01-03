#![allow(dead_code)]
/*
                                            |- Session/Tab/Emulation |- ScreenWidow/Screens
          - SessionGroup/TerminalView/TabBar|
          |                                 |- Session/Tab/Emulation |- ScreenWidow/Screens
 Terminal-|
          |                                 |- Session/Tab/Emulation |- ScreenWidow/Screens
          - SessionGroup/TerminalView/TabBar|
                                            |- Session/Tab/Emulation |- ScreenWidow/Screens
*/

/// The terminal's main widget. Responsible for all layouts management of `TerminalView`,
/// forward the client's input information from the ipc channel.
pub struct TerminalEmulator {

}