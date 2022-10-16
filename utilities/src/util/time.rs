use std::time::SystemTime;

pub struct TimeRecorder {
    start: u64,
}

impl TimeRecorder {
    pub fn new() -> TimeRecorder {
        TimeRecorder { start: SystemTime::now().duration_since(SystemTime::UNIX_EPOCH).unwrap().as_millis() as u64 }
    }

    pub fn end(&self) -> u64 {
        let end = SystemTime::now().duration_since(SystemTime::UNIX_EPOCH).unwrap().as_millis() as u64;
        end - self.start
    }
}

pub struct TimeStamp {}

impl TimeStamp {
    pub fn timestamp() -> u64 {
        SystemTime::now().duration_since(SystemTime::UNIX_EPOCH).unwrap().as_millis() as u64
    }

    pub fn timestamp_16() -> u16 {
        let mut ts = (TimeStamp::timestamp() % 65536) as u16;
        if ts == u16::MAX {
            ts += 1;
        }
        ts
    }
}

#[cfg(test)]
mod tests {
    use std::thread;
    use std::time::Duration;
    use super::*;

    #[test]
    fn test_time_recorder() {
        let recorder = TimeRecorder::new();
        thread::sleep(Duration::from_secs(2));
        let consume = recorder.end();
        assert_eq!(consume / 1000, 2);
    }

    #[test]
    fn test_time_stamp() {
        let ts = TimeStamp::timestamp_16();
        println!("{}", TimeStamp::timestamp());
        assert_ne!(ts, u16::MAX)
    }
}