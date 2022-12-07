#![allow(dead_code)]
use std::{net::UdpSocket, time::Duration};
use utilities::TimeStamp;
use crate::crypto::{Base64Key, Session};
use super::{Direction, MoshPacket, MAX_RTO, MIN_RTO, RTTVAR, SRIT};
use log::info;

pub struct Connection {
    socket: UdpSocket,
    local_addr: String,
    remote_addr: String,

    session: Session,

    saved_timestamp: i64,
    saved_timestamp_receive_at: i64,
    expected_receiver_seq: u64,
}

impl Connection {
    const DIRECTION: Direction = Direction::ToServer;

    pub fn new(ip: &str, port: &str, key: &str) -> Self {
        let local_ip = local_ipaddress::get().expect("Get local ip address failed.");
        println!("local ip: {}, port: {}, key: {}", local_ip, port, key);
        let local_addr = format!("{}:{}", local_ip, port);
        let remote_addr = format!("{}:{}", ip, port);

        let socket = UdpSocket::bind(local_addr.as_str())
            .expect(format!("Bind to local udp socket failed, {}", local_addr.as_str()).as_str());
        socket.connect(remote_addr.as_str()).expect(
            format!(
                "Connect to remote udp socket failed, {}",
                remote_addr.as_str()
            )
            .as_str(),
        );
        socket
            .set_read_timeout(Some(Duration::from_millis(5)))
            .expect("Socket set read timeout failed.");

        Connection {
            socket,
            local_addr,
            remote_addr,
            session: Session::new(Base64Key::new(key.to_string())),
            saved_timestamp: -1,
            saved_timestamp_receive_at: 0,
            expected_receiver_seq: 0,
        }
    }

    pub fn send(&self, bytes: Vec<u8>) {
        self.socket.send(&bytes).expect(
            format!(
                "Udp socket send data failed, local_addr = {}, remote_addr = {}",
                self.local_addr, self.remote_addr
            )
            .as_str(),
        );
    }

    pub fn recv(&self) -> Option<Vec<u8>> {
        let mut recvd = vec![];
        if let Ok(len) = self.socket.recv(&mut recvd) {
            info!("Udp socket data received, len = {}", len);
            Some(recvd)
        } else {
            None
        }
    }

    pub fn recv_one(&mut self, bytes: Vec<u8>) -> Vec<u8> {
        let decrypt_message = self.session.decrypt(&bytes, bytes.len());
        let packet = MoshPacket::from_message(decrypt_message);
        self.expected_receiver_seq = packet.seq() + 1;
        self.saved_timestamp = packet.timestamp() as i64;
        self.saved_timestamp_receive_at = TimeStamp::timestamp() as i64;
        packet.payload()
    }

    pub fn timeout(&self) -> u64 {
        let mut rto = (SRIT + 4. * RTTVAR).ceil() as u64;
        if rto < MIN_RTO {
            rto = MIN_RTO
        } else if rto > MAX_RTO {
            rto = MAX_RTO
        }
        rto
    }

    fn new_packet(&mut self, msg: Vec<u8>) -> MoshPacket {
        let mut outgoing_timestamp_reply = -1i16;

        let now = TimeStamp::timestamp() as i64;

        if now - self.saved_timestamp_receive_at < 1000 {
            outgoing_timestamp_reply =
                self.saved_timestamp as i16 + (now as i16 - self.saved_timestamp_receive_at as i16);
            self.saved_timestamp = -1;
            self.saved_timestamp_receive_at = -1;
        }

        MoshPacket::from_payload(
            msg,
            Direction::ToServer,
            TimeStamp::timestamp_16(),
            outgoing_timestamp_reply as u16,
        )
    }
}
