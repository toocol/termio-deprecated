#![allow(dead_code)]
use log::info;
use ssh2::Session;
use std::{io::Read, net::TcpStream};

pub fn ssh_touch(host: &str, port: &str, username: &str, password: &str) -> (String, String) {
    let mut remote = String::new();
    remote.push_str(host);
    remote.push_str(":");
    remote.push_str(port);
    let tcp = TcpStream::connect(remote.as_str()).expect("Establish tcp stream failed.");

    let mut session = Session::new().expect("Create ssh session failed.");
    session.set_tcp_stream(tcp);
    session.handshake().expect("Ssh session hand shake failed.");
    session
        .userauth_password(username, password)
        .expect("Ssh session userauth failed.");

    let mut channel = session
        .channel_session()
        .expect("Open channel session failed.");
    channel.exec("mosh-server").expect("Execute command failed");
    let mut response = String::new();
    channel
        .read_to_string(&mut response)
        .expect("Read response to String failed.");
    channel.close().expect("Close ssh channel failed.");

    let mut port = String::new();
    let mut key = String::new();
    for line in response.split("\r\n").into_iter() {
        if line.contains("MOSH CONNECT") {
            let mut idx = 0;
            for p in line.split(" ").into_iter() {
                if idx == 2 {
                    port.push_str(p.trim());
                }
                if idx == 3 {
                    key.push_str(p.trim());
                }
                idx += 1;
            }
            break;
        }
    }

    info!("Ssh touch success, port = {}, key = {}", port, key);
    (port, key)
}
