#![allow(dead_code)]
use std::{
    io::{prelude::*, Write},
    vec,
};
use flate2::{read::ZlibDecoder, write::ZlibEncoder, Compression};

pub struct Compressor;
impl Compressor {
    pub fn compress(bytes: Vec<u8>) -> Vec<u8> {
        let mut encoder = ZlibEncoder::new(Vec::new(), Compression::default());
        encoder.write(&bytes).expect("Zlib write failed.");
        encoder.finish().expect("Zlib compress failed.")
    }

    pub fn decompress(bytes: Vec<u8>) -> Vec<u8> {
        let mut decoder = ZlibDecoder::new(&bytes[..]);
        let mut read = String::new();
        let size = decoder
            .read_to_string(&mut read)
            .expect("Zlib decompress failed.");

        let mut ret = vec![0u8; size];
        ret[0..size].copy_from_slice(&read.as_bytes());
        ret
    }
}

#[cfg(test)]
mod tests {
    use super::Compressor;

    #[test]
    fn test_zlib() {
        let text = "Rust is a multi-paradigm, general-purpose programming language. Rust emphasizes performance, type safety, and concurrency.[11][12][13] 
Rust enforces memory safety—that is, that all references point to valid memory—without requiring the use of a garbage collector or reference counting present in other memory-safe languages.[13][14]";
        let bytes = text.as_bytes();
        let mut array = vec![0u8; bytes.len()];
        array[0..bytes.len()].copy_from_slice(&bytes);
        let array = Compressor::compress(array);
        let array = Compressor::decompress(array);
        let decompress = String::from_utf8(array).unwrap();
        assert_eq!(text, decompress);
    }

    #[test]
    fn test_decompress() {
        let origin = "An essay is, generally, a piece of writing that gives the author's own argument, but the definition is vague, overlapping with those of a letter, a paper, an article, a pamphlet, and a short story. Essays have been sub-classified as formal and informal: formal essays are characterized by \"serious purpose, dignity, logical organization, length,\" whereas the informal essay is characterized by \"the personal element (self-revelation, individual tastes and experiences, confidential manner), humor, graceful style, rambling structure, unconventionality or novelty of theme,\" etc.[1]

Essays are commonly used as literary criticism, political manifestos, learned arguments, observations of daily life, recollections, and reflections of the author. Almost all modern essays are written in prose, but works in verse have been dubbed essays (e.g., Alexander Pope's An Essay on Criticism and An Essay on Man). While brevity usually defines an essay, voluminous works like John Locke's An Essay Concerning Human Understanding and Thomas Malthus's An Essay on the Principle of Population are counterexamples.

In some countries (e.g., the United States and Canada), essays have become a major part of formal education.[2] Secondary students are taught structured essay formats to improve their writing skills; admission essays are often used by universities in selecting applicants, and in the humanities and social sciences essays are often used as a way of assessing the performance of students during final exams.

The concept of an \"essay\" has been extended to other media beyond writing. A film essay is a movie that often incorporates documentary filmmaking styles and focuses more on the evolution of a theme or idea. A photographic essay covers a topic with a linked series of photographs that may have accompanying text or captions.\n";

        let src = "eJx1VcmO20YQvesrCnOJDcgCkmNyGgwMJEEMGLCNHAwfSs0i2VFv6IUa+evzqluaxUhuZLHW914V7wNJKXwhW/a0SJDMzl32xJSsGKE40znbasNCdeVKi92k4FGIW11j/qlQPAfivDQvoe7p2Gr/PMlsAwJjQGraeGmyp7hJdpySpjvbusIzll6EyUmtkntlTv1B01ZrnAyjTyt81D7hvaB4pVJjvhzovY5QaOVN6CgSqLTjO+O4FDtbgXuhOWbPrgfbMF5+vRllhHMWMitnNmjEfkfc8UJ3Bc+xFUotJzS7p8kuGAwYubhYg/CYFw72O+uwsEpY6rq/o/MqWXiAdSv5BPZ/FFI/DF5iUD8niie9KeLmd1k2cdf8Nkx2s1ODU+VSwYbOJI8ItRKMgEcTw2wnhFs4eQ5g9e2e1uYjYF1QVubmgN1Foc3sj04JKTU3U1uGrQWk2DSBNoNhMSMFsOf0cdaRvGBEqebw9edvu937FwhG72NwF2plII94qCpfyKiQjC1+Tyk6fR7t2VnAY1HoOAcNuqoJpngE/lsfvWjliS1SO4SgczHROTH949BFlvlmuPZ51emB7p2PpRLkTT5OksNL2lXjFbqxgVLuLKuOzzGfitogW8j0WV5TOx7R5zXBGzkshz0KyCN6kEwfYxIsxn0YwiTswMNt9t7myy8fOLw90N+rdUgOohXtVpqu4ViizvCotactuuZtUEGO7pw9Cf0Z10B/RXN6VfYhQg45KLe/N+BMX7S7UtGB2rSRz2v0oOgDu7q28kPPit7HbIOxyfUlxVxtyPDKdAugFlN7OJTDbvcHNi/66xfI8QkbTfUFWwPQPlW+ifaBA08Macqr/TWagqGMf6C6hBugtW/7MzXTOzh8/eUbfYJvmFRbpTYV/GCzclvW+izoK1MjB3xqJOvBM6qhMZufTlw5WefKb8STt7gd8ZVG4qwK6arGurZgVRWIky4R7KkKT3FNyUHaXb7j3PTxV6VguKu1RKPbWcxY2v8pBG6YztyXDtcMTuMS90vRxwnjSD/NP7WsLtCNogVqlJfPq3IC19SxhBTuer07QF6GpOURRSeUBDgRBTJ5mSzj4wUQ3xDCGiG18893DDzFzcr4O4zOoZiIW5k7z1M0fZeVJI30fBrHBsdnIDHDA5Mhj84+wBLVeRda/zf0e6M3CFeNtYe0xhpxydJqzbUXoz8X7afGBGP/veCvYsMJQ+kRl34SniPL6NkjtguPDYSXOFw6xMBDCxpO/Zocdv8Cge6byg==";
        let decode = base64::decode(src).unwrap();
        let decompress = Compressor::decompress(decode);
        let decompress = String::from_utf8(decompress).unwrap();
        assert_eq!(origin, decompress);
    }
}
