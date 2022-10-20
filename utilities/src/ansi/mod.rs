pub mod ansi_string;
pub mod escape_sequence;
pub mod color;

pub use self::ansi_string::*;
pub use self::escape_sequence::*;
pub use self::color::*;

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_ansi_string() {
        let mut ansi_str = AnsiString::new();

        ansi_str
            .foreground_256(15)
            .background_rgb(112, 112, 112)
            .italic()
            .underline()
            .append("Hello World! ")
            .bold()
            .foreground_rgb(175, 0, 0)
            .background_256(32)
            .append(" Hello you!")
            .clear_style();
        println!("{}", ansi_str.to_string());
    }
}
