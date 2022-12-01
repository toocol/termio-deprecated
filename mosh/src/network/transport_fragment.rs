#![allow(dead_code)]

use std::collections::BinaryHeap;

use protobuf::Message;
use utilities::ByteOrder;

use crate::{network::Compressor, proto::transportinstruction::Instruction};

use super::DEFAULT_SEND_MTU;

const FRAG_HEADER_LEN: usize = 10;

static mut BUFFER: [u8; DEFAULT_SEND_MTU] = [0u8; DEFAULT_SEND_MTU];
#[derive(Debug, PartialEq, Eq, PartialOrd)]
pub struct Fragment {
    id: i64,
    finalize: bool,
    initialized: bool,
    contents: Vec<u8>,
    fragment_num: i16,
}
impl Default for Fragment {
    fn default() -> Self {
        Self {
            id: -1,
            finalize: false,
            initialized: false,
            contents: vec![],
            fragment_num: -1,
        }
    }
}
impl Ord for Fragment {
    fn cmp(&self, other: &Self) -> std::cmp::Ordering {
        self.fragment_num.cmp(&other.fragment_num)
    }
}

impl Fragment {
    pub fn from_id(id: i64, fragment_num: i16, finalize: bool, contents: Vec<u8>) -> Self {
        Fragment {
            id,
            finalize,
            initialized: true,
            contents,
            fragment_num,
        }
    }

    pub fn from_bytes(bytes: &[u8]) -> Self {
        assert!(bytes.len() > FRAG_HEADER_LEN);

        let mut contents = vec![0; bytes.len() - FRAG_HEADER_LEN];
        contents[..].copy_from_slice(&bytes[FRAG_HEADER_LEN..bytes.len()]);

        let mut id_bytes = [0u8; 8];
        let mut fragment_num_bytes = [0u8; 2];
        id_bytes.copy_from_slice(&bytes[0..8]);
        fragment_num_bytes.copy_from_slice(&bytes[8..10]);

        let id = ByteOrder::be64toh(id_bytes);
        let fragment_num = ByteOrder::be16toh(fragment_num_bytes);

        Fragment {
            id: id as i64,
            finalize: ((fragment_num & 0x8000) >> 15) != 0,
            initialized: true,
            contents,
            fragment_num: fragment_num as i16,
        }
    }

    pub fn to_bytes(&self) -> Vec<u8> {
        assert!(self.initialized);

        let mut proceed = 0;
        let nob = Fragment::network_order_bytes_u64(self.id as u64);
        unsafe { BUFFER[proceed..proceed + nob.len()].copy_from_slice(&nob) }
        proceed += nob.len();

        let combined_fragment_num =
            ((if self.finalize { 1 } else { 0 }) << 15) | self.fragment_num as u16;
        let nob = Fragment::network_order_bytes_u16(combined_fragment_num);
        unsafe { BUFFER[proceed..proceed + nob.len()].copy_from_slice(&nob) }
        proceed += nob.len();

        assert_eq!(proceed, FRAG_HEADER_LEN);

        unsafe { BUFFER[proceed..proceed + self.contents.len()].copy_from_slice(&self.contents) }
        proceed += self.contents.len();

        let mut ret = vec![0u8; proceed];
        unsafe { ret[..].copy_from_slice(&BUFFER[0..proceed]) };
        ret
    }

    pub fn id(&self) -> i64 {
        self.id
    }

    fn network_order_bytes_u16(host_order: u16) -> [u8; 2] {
        ByteOrder::htobe16(host_order)
    }

    fn network_order_bytes_u64(host_order: u64) -> [u8; 8] {
        ByteOrder::htobe64(host_order)
    }
}

pub struct Fragmenter {
    next_instruction_id: i64,
    last_instruction: Option<Instruction>,
    last_mtu: i64,
}

impl Fragmenter {
    pub fn new() -> Self {
        Fragmenter {
            next_instruction_id: 0,
            last_instruction: None,
            last_mtu: -1,
        }
    }

    pub fn make_fragments(&mut self, inst: Instruction, mtu: usize) -> Vec<Fragment> {
        let mut mtu = mtu;
        mtu -= FRAG_HEADER_LEN;

        if self.last_instruction.is_none()
            || *self.last_instruction.as_ref().unwrap() != inst
            || self.last_mtu != mtu as i64
        {
            self.next_instruction_id += 1;
        }

        assert!(
            self.last_instruction.is_none()
                || inst.old_num() != self.last_instruction.as_ref().unwrap().old_num()
                || inst.new_num() != self.last_instruction.as_ref().unwrap().new_num()
                || inst.diff() == self.last_instruction.as_ref().unwrap().diff()
        );

        self.last_instruction.replace(inst);
        self.last_mtu = mtu as i64;

        let inst = self.last_instruction.as_ref().unwrap();

        let payload = Compressor::compress(
            inst.write_to_bytes()
                .expect("Instruction writes to bytes failed."),
        );
        let mut remain = payload.len();
        let mut deal = 0;
        let mut fragment_num = 0;

        let mut ret = vec![];
        while remain > 0 {
            let mut this_fragment: Vec<u8>;
            let mut finalize = false;

            if remain > mtu {
                this_fragment = vec![0u8; mtu];
                this_fragment[0..mtu].copy_from_slice(&payload[deal..deal + mtu]);
                deal += mtu;
                remain -= mtu;
            } else {
                this_fragment = vec![0u8; remain];
                this_fragment[0..remain].copy_from_slice(&payload[deal..deal + remain]);
                remain = 0;
                finalize = true;
            }

            ret.push(Fragment::from_id(
                self.next_instruction_id,
                fragment_num,
                finalize,
                this_fragment,
            ));
            fragment_num += 1;
        }
        ret
    }

    pub fn last_ack_sent(&self) -> Option<u64> {
        if let Some(last_instrucion) = self.last_instruction.as_ref() {
            Some(last_instrucion.ack_num())
        } else {
            None
        }
    }
}

pub struct FragmentAssembly {
    fragments: Option<BinaryHeap<Fragment>>,
    current_id: i64,
    fragments_arrived: i32,
    fragments_total: i32,
    contents_length: i32,
}

impl FragmentAssembly {
    pub fn new() -> Self {
        FragmentAssembly {
            fragments: Some(BinaryHeap::new()),
            current_id: -1,
            fragments_arrived: 0,
            fragments_total: -1,
            contents_length: 0,
        }
    }

    pub fn add_fragment(&mut self, fragment: Fragment) -> bool {
        let finalize = fragment.finalize;
        let fragment_num = fragment.fragment_num;

        if self.current_id != fragment.id {
            self.fragments_arrived = 1;
            self.fragments_total = -1;
            self.contents_length = fragment.contents.len() as i32;
            self.current_id = fragment.id;

            self.fragments.as_mut().unwrap().clear();
            self.fragments.as_mut().unwrap().push(fragment);
        } else {
            /* see if we already have this fragment */
            let mut frag = self.get_at(fragment.fragment_num);
            let have = self.fragments.as_ref().unwrap().len() as i16 > fragment.fragment_num
                && (frag.is_some() && fragment == *frag.take().unwrap());
            if !have {
                self.fragments_arrived += 1;
                self.contents_length += fragment.contents.len() as i32;
                self.fragments.as_mut().unwrap().push(fragment);
            }
        }

        if finalize {
            self.fragments_total = fragment_num as i32 + 1;
            assert!(self.fragments.as_ref().unwrap().len() as i32 <= self.fragments_total);
        }

        assert!(self.fragments_total == -1 || self.fragments_arrived <= self.fragments_total);

        self.fragments_arrived == self.fragments_total
    }

    pub fn get_assembly(&mut self) -> Option<Instruction> {
        assert_eq!(self.fragments_arrived, self.fragments_total);

        let mut contents = vec![0u8; self.contents_length as usize];
        let mut proceed = 0;
        for frag in self.fragments.take().unwrap().into_sorted_vec() {
            contents[proceed..proceed + frag.contents.len()]
                .copy_from_slice(&frag.contents[0..frag.contents.len()]);
            proceed += frag.contents.len();
        }

        let decompress = Compressor::decompress(contents);

        self.fragments_arrived = 0;
        self.fragments_total = -1;
        self.contents_length = 0;
        self.fragments.replace(BinaryHeap::new());

        if let Ok(ins) = Instruction::parse_from_bytes(&decompress) {
            Some(ins)
        } else {
            None
        }
    }

    fn get_at(&self, num: i16) -> Option<&Fragment> {
        for frag in self.fragments.as_ref().unwrap().iter() {
            if frag.fragment_num == num {
                return Some(frag);
            }
        }
        None
    }
}

#[cfg(test)]
mod tests {
    use crate::{crypto::Session, network::MoshPacket};

    use super::*;

    #[test]
    fn test_fragment() {
        let mut fragmenter = Fragmenter::new();
        let mtu = DEFAULT_SEND_MTU - MoshPacket::ADDED_BYTES - Session::ADDED_BYTES;
        let mut inst = Instruction::new();
        inst.set_protocol_version(0);
        inst.set_old_num(1);
        inst.set_new_num(2);
        inst.set_ack_num(3);
        inst.set_throwaway_num(4);
        inst.set_diff(vec![5u8; 20]);
        inst.set_chaff(vec![5u8; 20]);
        let fragments = fragmenter.make_fragments(inst.clone(), mtu);

        let mut fragment_assembly = FragmentAssembly::new();
        let mut flag = false;
        for frag in fragments {
            if fragment_assembly.add_fragment(frag) {
                let assembled_inst = fragment_assembly.get_assembly().unwrap();
                assert_eq!(inst, assembled_inst);
                flag = true;
            }
        }
        assert!(flag);
    }
}
