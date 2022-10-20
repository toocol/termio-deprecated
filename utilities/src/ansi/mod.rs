pub mod ansi_string;

pub use self::ansi_string::*;

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_ansi_string() {
        let ansi_str = AnsiString::new();

        println!("{}", ansi_str.to_string());
    }
}