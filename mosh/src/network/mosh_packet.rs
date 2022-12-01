#![allow(dead_code)]
use utilities::ByteOrder;

use crate::crypto::{Crypto, Message, Nonce};

#[derive(Debug, PartialEq, Eq)]
pub struct MoshPacket {
    seq: u64,
    direction: Direction,
    payload: Vec<u8>,
    timestamp: u16,
    timestamp_reply: u16,
}

impl MoshPacket {
    pub const ADDED_BYTES: usize = 8;
    pub const DIRECTION_MASK: u64 = 1 << 63;
    pub const SEQUENCE_MASK: u64 = u64::MAX ^ MoshPacket::DIRECTION_MASK;

    pub fn from_payload(
        payload: Vec<u8>,
        direction: Direction,
        timestamp: u16,
        timestamp_reply: u16,
    ) -> Self {
        let seq = Crypto::unique();
        MoshPacket {
            seq,
            direction,
            payload,
            timestamp,
            timestamp_reply,
        }
    }

    pub fn from_message(message: Message) -> Self {
        let len = message.text.len() - 4;
        let mut payload = vec![0u8; len];
        payload.copy_from_slice(&message.text[4..4 + len]);
        MoshPacket {
            seq: message.nonce.val() & MoshPacket::SEQUENCE_MASK,
            direction: if (message.nonce.val() & MoshPacket::DIRECTION_MASK) == 0 {
                Direction::ToServer
            } else {
                Direction::ToClient
            },
            payload,
            timestamp: message.get_timestamp(),
            timestamp_reply: message.get_timestamp_reply(),
        }
    }

    pub fn to_message(&self) -> Message {
        let direction_seq =
            (self.direction.to_u64() << 63) | (self.seq & MoshPacket::SEQUENCE_MASK);

        let mut text = vec![0u8; 4 + self.payload.len()];
        text[0..4].copy_from_slice(&self.timestamps_merge());
        text[4..4 + self.payload.len()].copy_from_slice(&self.payload);

        Message::new(Nonce::from_seq(direction_seq), text)
    }

    fn timestamps_merge(&self) -> [u8; 4] {
        let timestamp_bytes = ByteOrder::htobe16(self.timestamp);
        let timestamp_reply_bytes = ByteOrder::htobe16(self.timestamp_reply);
        let mut target = [0u8; 4];
        target[0..2].copy_from_slice(&timestamp_bytes);
        target[2..4].copy_from_slice(&timestamp_reply_bytes);
        target
    }

    pub fn seq(&self) -> u64 {
        self.seq
    }

    pub fn direction(&self) -> &Direction {
        &self.direction
    }

    pub fn timestamp(&self) -> u16 {
        self.timestamp
    }

    pub fn timestamp_reply(&self) -> u16 {
        self.timestamp_reply
    }

    pub fn payload(self) -> Vec<u8> {
        self.payload
    }
}

#[repr(u64)]
#[derive(Debug, PartialEq, Eq)]
pub enum Direction {
    ToServer = 0,
    ToClient,
}

impl Direction {
    pub fn to_u64(&self) -> u64 {
        match self {
            Direction::ToServer => 0,
            Direction::ToClient => 1,
        }
    }
}

#[cfg(test)]
mod tests {
    use utilities::TimeStamp;

    use super::*;

    #[test]
    fn test_mosh_packet() {
        let _packet = MoshPacket::from_payload(
            vec![],
            Direction::ToClient,
            TimeStamp::timestamp_16(),
            TimeStamp::timestamp_16(),
        );
        let packet = MoshPacket::from_message(Message::new(Nonce::from_seq(1), vec![0; 20]));
        let _message = packet.to_message();
    }
}
