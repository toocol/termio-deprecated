#![allow(dead_code)]
pub const MOSH_PROTOCOL_VERSION: u32 = 2;

pub const SRIT: f64 = 1000.0;
pub const RTTVAR: f64 = 500.0;

pub const MIN_RTO: u64 = 50; /* ms */
pub const MAX_RTO: u64 = 1000; /* ms */

pub const SEND_INTERVAL_MIN: u32 = 20; /* ms between frames */
pub const SEND_INTERVAL_MAX: u32 = 20; /* ms between frames */
pub const ACK_INTERVAL: u32 = 3000; /* ms between empty acks */
pub const ACK_DELAY: u32 = 100; /* ms before delayed ack */
pub const SHUTDOWN_RETRIES: u32 = 16; /* number of shutdown packets to send before giving up */
pub const ACTIVE_RETRY_TIMEOUT: u32 = 10000; /* attempt to resend at frame rate */

/**
 * Application datagram MTU. For constructors and fallback.
 */
pub const DEFAULT_SEND_MTU: usize = 500;
