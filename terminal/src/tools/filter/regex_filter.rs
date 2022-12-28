use regex::Regex;
use std::{rc::Rc, cell::RefCell};
use tmui::prelude::*;

use super::{HotSpot, HotSpotConstructer, HotSpotImpl, HotSpotType, BaseFilter, BaseFilterImpl, Filter};

/// Type of hotspot created by RegExpFilter.  The capturedTexts() method can be
/// used to find the text matched by the filter's regular expression.
pub struct RegexFilterHotSpot {
    hotspot: HotSpot,
    captured_texts: Vec<String>,
}
pub trait RegexFilterHotSpotImpl {
    /// Sets the captured texts associated with this hotspot.
    fn set_captured_texts(&mut self, texts: Vec<String>);

    /// Returns the texts found by the filter when matching the filter's regular expression.
    fn captured_texts(&self) -> &Vec<String>;
}
impl RegexFilterHotSpotImpl for RegexFilterHotSpot {
    fn set_captured_texts(&mut self, texts: Vec<String>) {
        self.captured_texts = texts;
    }

    fn captured_texts(&self) -> &Vec<String> {
        &self.captured_texts
    }
}
impl HotSpotConstructer for RegexFilterHotSpot {
    fn new(start_line: i32, start_column: i32, end_line: i32, end_column: i32) -> Self {
        Self {
            hotspot: HotSpot::new(start_line, start_column, end_line, end_column),
            captured_texts: vec![],
        }
    }
}
impl HotSpotImpl for RegexFilterHotSpot {
    fn start_line(&self) -> i32 {
        self.hotspot.start_line()
    }

    fn end_line(&self) -> i32 {
        self.hotspot.end_line()
    }

    fn start_column(&self) -> i32 {
        self.hotspot.start_column()
    }

    fn end_column(&self) -> i32 {
        self.hotspot.end_column()
    }

    fn type_(&self) -> HotSpotType {
        self.hotspot.type_()
    }

    fn activate(&self, _action: &str) {}

    fn set_type(&mut self, type_: HotSpotType) {
        self.hotspot.set_type(type_)
    }

    fn actions(&self) -> Vec<Action> {
        vec![]
    }
}

/// A filter which searches for sections of text matching a regular expression
/// and creates a new RegExpFilter::HotSpot instance for them.
///
/// Subclasses can reimplement newHotSpot() to return custom hotspot types when
/// matches for the regular expression are found.
pub struct RegexFilter {
    filter: BaseFilter,
}
impl RegexFilter {
    pub fn new() -> Self {
        Self {
            filter: BaseFilter::new(),
        }
    }
}
impl BaseFilterImpl for RegexFilter {
    fn add_hotspot(&mut self, hotspot: Box<dyn HotSpotImpl>) {
        self.filter.add_hotspot(hotspot)
    }

    fn get_line_column(&self, position: i32) -> (i32, i32) {
        self.filter.get_line_column(position)
    }
}
pub trait RegexFilterImpl {
    /// Sets the regular expression which the filter searches for in blocks of text.
    ///
    /// Regular expressions which match the empty string are treated as not matching anything.
    fn set_regex(&mut self, regex: Regex);

    /// Returns the regular expression which the filter searches for in blocks of text.
    fn regex(&self) -> &Regex;
}
impl Filter for RegexFilter {
    fn process(&mut self, regex: &Regex) {
        let mut pos;
        let text = self.buffer().borrow().to_string();
        assert!(!text.is_empty());

        let iter = regex.captures_iter(&text);
        for cap in iter {
            for i in 0..cap.len() {
                let matched = cap.get(i).unwrap();
                pos = matched.start() as i32;

                let (start_line, start_column) = self.get_line_column(pos);
                let (end_line, end_column) =
                    self.get_line_column(pos + matched.range().len() as i32);

                let mut spot =
                    RegexFilterHotSpot::new(start_line, start_column, end_line, end_column);
                let mut captured_texts = vec![];
                for matched in cap.iter() {
                    if let Some(m) = matched {
                        captured_texts.push(m.as_str().to_string());
                    }
                }
                spot.set_captured_texts(captured_texts);

                self.add_hotspot(Box::new(spot));
            }
        }
    }

    fn reset(&mut self) {
        self.filter.reset()
    }

    fn hotspot_at(&self, line: i32, column: i32) -> Option<Rc<Box<dyn HotSpotImpl>>> {
        self.filter.hotspot_at(line, column)
    }

    fn hotspots(&self) -> &Vec<Rc<Box<dyn HotSpotImpl>>> {
        self.filter.hotspots()
    }

    fn hotspots_at_line(&self, line: i32) -> Option<&Vec<Rc<Box<dyn HotSpotImpl>>>> {
        self.filter.hotspots_at_line(line)
    }

    fn set_buffer(&mut self, buffer: Rc<RefCell<String>>, line_positions: Rc<RefCell<Vec<i32>>>) {
        self.filter.set_buffer(buffer, line_positions)
    }

    fn buffer(&mut self) -> Rc<RefCell<String>> {
        self.filter.buffer()
    }
}
