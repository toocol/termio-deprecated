#![allow(dead_code)]
use super::{
    character::{Character, LineProperty, DEFAULT_RENDITION},
    character_color::{CharacterColor, ColorEntry, BASE_COLOR_TABLE},
    text_stream::TextStream,
};
use crate::tools::{
    character::{RE_BOLD, RE_UNDERLINE},
    character_color::FontWeight,
    system_ffi::wcwidth,
};
use wchar::wch;
use widestring::U16String;

const TRANSMIT_U16STRING_ERROR: &'static str =
    "Trasmit `U16String` to `String` failed, unvalid u16 datas.";

/// Base struct for terminal character decoders
///
/// The decoder converts lines of terminal characters which consist of a unicode
/// character, foreground and background colours and other appearance-related
/// properties into text strings.
///
/// Derived classes may produce either plain text with no other colour or
/// appearance information, or they may produce text which incorporates these
/// additional properties.
pub trait TerminalCharacterDecoder<'a> {
    /// Begin decoding characters.  The resulting text is appended to @p output.
    fn begin(&mut self, output: &'a mut TextStream<'a>);

    /// End decoding.
    fn end(&mut self);

    /// Converts a line of terminal characters with associated properties into a
    /// text string and writes the string into an output TextStream.
    ///
    /// @param characters An array of characters of length @p count.
    /// @param count The number of characters
    /// @param properties Additional properties which affect all characters in the line.
    fn decode_line(&mut self, character: &[Character], count: i32, properties: LineProperty);
}

/////////////////////////////////////////////////////////////////////////////////////////
/// A terminal character decoder which produces plain text, ignoring colours and
/// other appearance-related properties of the original characters.
////////////////////////////////////////////////////////////////////////////////////////
pub struct PlainTextDecoder<'a> {
    output: Option<&'a mut TextStream<'a>>,

    include_trailing_whitespace: bool,
    record_line_positions: bool,
    line_positions: Vec<i32>,
}

impl<'a> PlainTextDecoder<'a> {
    pub fn new() -> Self {
        Self {
            output: None,
            include_trailing_whitespace: true,
            record_line_positions: false,
            line_positions: vec![],
        }
    }

    /// Set whether trailing whitespace at the end of lines should be included in the output.
    /// Defaults to true.
    pub fn set_trailing_whitespace(&mut self, enable: bool) {
        self.include_trailing_whitespace = enable
    }

    /// Returns whether trailing whitespace at the end of lines is included in the output.
    pub fn trailing_whitespace(&self) -> bool {
        self.include_trailing_whitespace
    }

    /// Returns of character positions in the output stream at which new lines where added.  Returns an empty if
    /// setTrackLinePositions() is false or if the output device is not a string.
    pub fn line_position(&self) -> &Vec<i32> {
        &self.line_positions
    }

    /// Enables recording of character positions at which new lines are added.
    /// @See linePositions()
    pub fn set_record_line_position(&mut self, record: bool) {
        self.record_line_positions = record
    }

    /// Append a '\n' to the end of output TextStream
    pub fn new_line(&mut self) {
        if self.output.is_none() {
            return;
        }
        self.output.as_mut().unwrap().append("\n")
    }
}

impl<'a> TerminalCharacterDecoder<'a> for PlainTextDecoder<'a> {
    fn begin(&mut self, output: &'a mut TextStream<'a>) {
        self.output = Some(output)
    }

    fn end(&mut self) {
        self.output = None
    }

    fn decode_line(&mut self, character: &[Character], count: i32, _: LineProperty) {
        let mut count = count;
        assert!(self.output.is_some());
        let output = self.output.as_deref_mut().unwrap();

        if self.record_line_positions && !output.is_empty() {
            let pos = output.count();
            self.line_positions.push(pos as i32);
        }

        // check the real length
        for i in 0..count {
            if character.get(i as usize).is_none() {
                count = i;
                break;
            }
        }

        let mut plain_text = U16String::new();

        let mut output_count = count;

        if !self.include_trailing_whitespace {
            let mut i = count - 1;
            loop {
                if i < 0 {
                    break;
                }

                if character
                    .get(i as usize)
                    .unwrap()
                    .character_union
                    .equals(wch!(' '))
                {
                    break;
                } else {
                    output_count -= 1;
                }

                i -= 1;
            }
        }

        let mut i = 0;
        loop {
            if i >= output_count {
                break;
            }
            plain_text.push_slice([character[i as usize].character_union.data()]);
            i += 1.max(wcwidth(character[i as usize].character_union.data()));
        }
        output.append(&plain_text.to_string().expect(TRANSMIT_U16STRING_ERROR))
    }
}

/////////////////////////////////////////////////////////////////////////////////////////
/// A terminal character decoder which produces pretty HTML markup
////////////////////////////////////////////////////////////////////////////////////////
pub struct HtmlDecoder<'a> {
    output: Option<&'a mut TextStream<'a>>,

    /// the colour table which the decoder uses to produce
    /// the HTML colour codes in its output
    color_table: &'a [ColorEntry],
    inner_span_open: bool,
    last_rendition: u16,
    last_fore_color: Option<CharacterColor>,
    last_back_color: Option<CharacterColor>,
}

impl<'a> HtmlDecoder<'a> {
    pub fn new() -> Self {
        Self {
            output: None,
            color_table: &BASE_COLOR_TABLE,
            inner_span_open: false,
            last_rendition: DEFAULT_RENDITION,
            last_fore_color: None,
            last_back_color: None,
        }
    }

    pub fn set_color_table(&mut self, table: &'a [ColorEntry]) {
        self.color_table = table;
    }

    fn open_span(text: &mut U16String, style: &str) {
        let str = format!("<span style=\"{}\">", style);
        text.push(U16String::from_str(&str))
    }

    fn close_span(text: &mut U16String) {
        text.push(U16String::from_str("</span>"))
    }
}

impl<'a> TerminalCharacterDecoder<'a> for HtmlDecoder<'a> {
    fn begin(&mut self, output: &'a mut TextStream<'a>) {
        let mut text = U16String::new();
        HtmlDecoder::open_span(&mut text, "font-family:monospace;");
        output.append(&text.to_string().expect(TRANSMIT_U16STRING_ERROR));

        self.output = Some(output);
    }

    fn end(&mut self) {
        assert!(self.output.is_some());

        let mut text = U16String::new();
        HtmlDecoder::close_span(&mut text);
        self.output
            .as_mut()
            .unwrap()
            .append(&text.to_string().expect(TRANSMIT_U16STRING_ERROR));

        self.output = None
    }

    fn decode_line(&mut self, character: &[Character], count: i32, _: LineProperty) {
        assert!(self.output.is_some());
        let output = self.output.as_deref_mut().unwrap();

        let mut text = U16String::new();

        let space_count = 0;

        for i in 0..count as usize {
            let ch = character[i].character_union.data();

            // check if appearance of character is different from previous char
            if character[i].rendition != self.last_rendition
                || (self.last_fore_color.is_none()
                    || character[i].foreground_color != self.last_fore_color.unwrap())
                || (self.last_back_color.is_none()
                    || character[i].background_color != self.last_back_color.unwrap())
            {
                if self.inner_span_open {
                    HtmlDecoder::close_span(&mut text)
                }

                self.last_rendition = character[i].rendition;
                self.last_fore_color = Some(character[i].foreground_color);
                self.last_back_color = Some(character[i].background_color);

                let mut style = String::new();
                let use_bold;
                let weight = character[i].font_weight(self.color_table);
                if weight == FontWeight::UseCurrentFormat {
                    use_bold = self.last_rendition & RE_BOLD > 0;
                } else {
                    use_bold = weight == FontWeight::Bold;
                }

                if use_bold {
                    style.push_str("font-weight:bold;")
                }

                if self.last_rendition & RE_UNDERLINE > 0 {
                    style.push_str("font-decoration:underline;")
                }

                if self.last_fore_color.is_some() {
                    let color = self.last_fore_color.unwrap().color(self.color_table);
                    style.push_str(&format!("color:{};", color.hexcode()))
                }

                if !character[i].is_transparent(self.color_table) && self.last_back_color.is_some()
                {
                    let color = self.last_back_color.unwrap().color(self.color_table);
                    style.push_str(&format!("background-color:{};", color.hexcode()))
                }

                HtmlDecoder::open_span(&mut text, &style);
                self.inner_span_open = true;
            }

            // output current character
            if space_count < 2 {
                if ch == wch!('<') {
                    text.push_str("&lt;")
                } else if ch == wch!('>') {
                    text.push_str("&gt;")
                } else {
                    text.push_slice([ch])
                }
            } else {
                text.push_str("&nbsp;")
            }
        }

        // close any remaining open inner spans
        if self.inner_span_open {
            HtmlDecoder::close_span(&mut text)
        }

        // start new line
        text.push_str("<br>");

        output.append(&text.to_string().expect(TRANSMIT_U16STRING_ERROR))
    }
}
