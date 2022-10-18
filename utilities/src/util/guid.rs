#![allow(dead_code)]
use std::sync::atomic::{AtomicU64, Ordering};

use super::time::TimeStamp;

const LONG_BIT: u32 = 64;
const UNIQUE_ID_BITS: u32 = 2;
const SEQUENCE_BITS: u32 = 16;
const TIMESTAMP_SHIFT_BITS: u32 = SEQUENCE_BITS + UNIQUE_ID_BITS;
const UNIQUE_ID_SHIFT_BITS: u32 = SEQUENCE_BITS;
const MAX_SEQUENCE_PER_MILLIS: u64 = 0xFFFFFFFFFFFF >> (LONG_BIT - SEQUENCE_BITS);

static SEQUENCE: AtomicU64 = AtomicU64::new(1);
static LAST_TIMESTAMP: AtomicU64 = AtomicU64::new(0);

/// Fetch a random global unique id by algorithm snowflake.
/// ## Usage
/// ```ignore
/// let id = SnowflakeGuidGenerator::next_id();
/// ```
pub struct SnowflakeGuidGenerator {}

impl SnowflakeGuidGenerator {
    pub fn next_id() -> u64 {
        let mut timestamp = SnowflakeGuidGenerator::time_gen();

        if timestamp == LAST_TIMESTAMP.load(Ordering::SeqCst) {
            SEQUENCE.fetch_add(1, Ordering::SeqCst);
            if SEQUENCE.load(Ordering::SeqCst) > MAX_SEQUENCE_PER_MILLIS {
                timestamp = SnowflakeGuidGenerator::til_next_millis(timestamp);
            }
        }
        if timestamp > LAST_TIMESTAMP.load(Ordering::SeqCst) {
            if let Err(_e) = SEQUENCE.fetch_update(Ordering::SeqCst, Ordering::SeqCst, |_x| Some(1))
            {
                return 0;
            }
        }

        if let Err(_e) =
            LAST_TIMESTAMP.fetch_update(Ordering::SeqCst, Ordering::SeqCst, move |_x| {
                Some(timestamp)
            })
        {
            return 0;
        }
        (timestamp << TIMESTAMP_SHIFT_BITS)
            | (1 << UNIQUE_ID_SHIFT_BITS)
            | SEQUENCE.load(Ordering::SeqCst)
    }

    fn time_gen() -> u64 {
        TimeStamp::timestamp()
    }

    fn til_next_millis(last_timestamp: u64) -> u64 {
        let mut timestamp: u64 = SnowflakeGuidGenerator::time_gen();
        while timestamp <= last_timestamp {
            timestamp = SnowflakeGuidGenerator::time_gen();
        }
        timestamp
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use std::collections::HashMap;

    #[test]
    fn test_snowflake_guid_generator() {
        let mut map: HashMap<u64, bool> = HashMap::new();

        for _i in 0..1000 {
            let id = SnowflakeGuidGenerator::next_id();
            assert_ne!(0, id);
            assert!(map.get(&id).is_none());
            map.insert(id, true);
        }
    }
}
