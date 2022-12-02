#![allow(dead_code)]
use utilities::TimeStamp;

use crate::crypto::Session;

use super::{Direction, MoshPacket, MAX_RTO, MIN_RTO, RTTVAR, SRIT};

pub struct Connection {
    session: Session,
    saved_timestamp: i64,
    saved_timestamp_receive_at: i64,
    expected_receiver_seq: u64,
}

impl Connection {
    const DIRECTION: Direction = Direction::ToServer;

    pub fn send(&self, _bytes: Vec<u8>) {}

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
