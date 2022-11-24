#![allow(dead_code)]

use rand::Rng;
pub struct Prng;
impl Prng {
    pub fn uint8() -> u8 {
        let mut x= [0u8];
        Prng::fill(&mut x, 1);
        x[0]
    }

    pub fn fill(dest: &mut [u8], size: usize) {
        if size == 0 {
            return;
        }

        let random = Prng::next_bytes(size);
        for i in 0..random.len() {
            dest[i] = random[i];
        }
    }

    fn next_bytes(count: usize) -> Vec<u8> {
        let mut bytes: Vec<u8> = vec![];
        let mut random = rand::thread_rng();
        for _ in 0..count {
            bytes.push(random.gen::<u8>())
        }
        bytes
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_prng() {
        for _ in 0..100 {
            let _d = Prng::uint8();
        }
    }
}
