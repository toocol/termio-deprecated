use crate::tools::character::{Character, LineProperty, LINE_DEFAULT, LINE_WRAPPED};
use crate::tools::terminal_character_decoder::{PlainTextDecoder, TerminalCharacterDecoder};
use crate::tools::text_stream::TextStream;
use regex::Regex;
use std::ops::DerefMut;
use std::{cell::RefCell, rc::Rc};
use tmui::prelude::*;

use super::regex_filter::{RegexFilter, RegexFilterHotSpot, RegexFilterHotSpotImpl};
use super::{
    filter_equals, BaseFilterImpl, Filter, FilterObject, HotSpotConstructer, HotSpotImpl,
    HotSpotType, EMAIL_ADDRESS_REGEX, FULL_URL_REGEX, URL_AND_EMAIL_REGEX,
};

#[repr(C)]
#[derive(Debug, PartialEq, Eq, Clone, Copy)]
pub enum UrlType {
    StandardUrl,
    Email,
    Unknown,
}
pub struct UrlFilterHotSpot {
    hotspot: RegexFilterHotSpot,
    url_object: RefCell<FilterObject>,
}
impl UrlFilterHotSpot {
    pub fn url_type(&self) -> UrlType {
        let url = self.captured_texts().first();

        if let Some(url) = url {
            if FULL_URL_REGEX.is_match(url) {
                UrlType::StandardUrl
            } else if EMAIL_ADDRESS_REGEX.is_match(url) {
                UrlType::Email
            } else {
                UrlType::Unknown
            }
        } else {
            UrlType::Unknown
        }
    }
}
impl RegexFilterHotSpotImpl for UrlFilterHotSpot {
    fn set_captured_texts(&mut self, texts: Vec<String>) {
        self.hotspot.set_captured_texts(texts);
    }

    fn captured_texts(&self) -> &Vec<String> {
        self.hotspot.captured_texts()
    }
}
impl HotSpotConstructer for UrlFilterHotSpot {
    fn new(start_line: i32, start_column: i32, end_line: i32, end_column: i32) -> Self {
        let mut hotspot = Self {
            hotspot: RegexFilterHotSpot::new(start_line, start_column, end_line, end_column),
            url_object: RefCell::new(FilterObject::new()),
        };
        hotspot.set_type(HotSpotType::Link);
        let ptr = &mut hotspot as *mut UrlFilterHotSpot as *mut dyn HotSpotImpl;
        hotspot.url_object.borrow_mut().set_filter(ptr);
        hotspot
    }
}
impl HotSpotImpl for UrlFilterHotSpot {
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

    fn activate(&self, action: &str) {
        let mut url = self.captured_texts().first().unwrap().clone();
        let kind = self.url_type();
        if action == FilterObject::ACTION_COPY {
            // TODO: Save `url` to the system clipboard;
            return;
        }

        if action.is_empty()
            || action == FilterObject::ACTION_OPEN
            || action == FilterObject::ACTION_CLICK
        {
            match kind {
                UrlType::StandardUrl => {
                    if !url.contains("://") {
                        let mut new_url = "http://".to_string();
                        new_url.push_str(&url);
                        url = new_url;
                    }
                }
                UrlType::Email => {
                    let mut new_url = "mailto:".to_string();
                    new_url.push_str(&url);
                    url = new_url;
                }
                _ => {}
            }

            self.url_object
                .borrow()
                .emit_activated(url, action != FilterObject::ACTION_CLICK);
        }
    }

    fn set_type(&mut self, type_: HotSpotType) {
        self.hotspot.set_type(type_)
    }

    fn actions(&self) -> Vec<Action> {
        let mut list = vec![];
        let kind = self.url_type();

        assert!(kind == UrlType::StandardUrl || kind == UrlType::Email);

        match kind {
            UrlType::StandardUrl => {
                let open_action = Action::with_param(FilterObject::ACTION_OPEN, "Open link");
                let copy_action =
                    Action::with_param(FilterObject::ACTION_COPY, "Copy link address");
                list.push(open_action);
                list.push(copy_action);
            }
            UrlType::Email => {
                let open_action = Action::with_param(FilterObject::ACTION_OPEN, "Send email to...");
                let copy_action =
                    Action::with_param(FilterObject::ACTION_COPY, "Copy email address");
                list.push(open_action);
                list.push(copy_action);
            }
            _ => {}
        }

        list
    }
}

/// A filter which matches URLs in blocks of text
pub struct UrlFilter {
    filter: RegexFilter,
}
impl UrlFilter {
    pub fn new() -> Self {
        Self {
            filter: RegexFilter::new(),
        }
    }
}
impl BaseFilterImpl for UrlFilter {
    fn add_hotspot(&mut self, hotspot: Box<dyn HotSpotImpl>) {
        self.filter.add_hotspot(hotspot)
    }

    fn get_line_column(&self, position: i32) -> (i32, i32) {
        self.filter.get_line_column(position)
    }
}
impl Filter for UrlFilter {
    fn process(&mut self, regex: &Regex) {
        self.filter.process(regex)
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

/// A chain which allows a group of filters to be processed as one.
/// The chain owns the filters added to it and deletes them when the chain itself
/// is destroyed.
///
/// Use addFilter() to add a new filter to the chain.
/// When new text to be filtered arrives, use addLine() to add each additional
/// line of text which needs to be processed and then after adding the last line,
/// use process() to cause each filter in the chain to process the text.
///
/// After processing a block of text, the reset() method can be used to set the
/// filter chain's internal cursor back to the first line.
///
/// The hotSpotAt() method will return the first hotspot which covers a given
/// position.
///
/// The hotSpots() and hotSpotsAtLine() method return all of the hotspots in the
/// text and on a given line respectively.
pub type FilterChain = RefCell<Vec<Rc<RefCell<dyn Filter>>>>;
pub trait FilterChainImpl {
    /// Adds a new filter to the chain.  The chain will delete this filter when it is destroyed.
    fn add_filter(&self, filter: Rc<RefCell<dyn Filter>>);

    /// Removes a filter from the chain.  The chain will no longer delete the filter when destroyed.
    fn remove_filter(&self, filter: Rc<RefCell<dyn Filter>>);

    /// Returns true if the chain contains @p filter.
    fn contains_filter(&self, filter: Rc<RefCell<dyn Filter>>) -> bool;

    /// Removes all filters from the chain.
    fn clear(&self);

    /// Resets each filter in the chain.
    fn reset(&self);

    /// Processes each filter in the chain.
    fn process(&self);

    /// Sets the buffer for each filter in the chain to process.
    fn set_buffer(&self, buffer: Rc<RefCell<String>>, line_position: Rc<RefCell<Vec<i32>>>);

    /// Returns the first hotspot which occurs at @p line, @p column or None if no hotspot was found
    fn hotspot_at(&self, line: i32, column: i32) -> Option<Rc<Box<dyn HotSpotImpl>>>;

    /// Returns a list of all the hotspots in all the chain's filters.
    fn hotspots(&self) -> Vec<Rc<Box<dyn HotSpotImpl>>>;

    /// Returns a list of all hotspots at the given line in all the chain's filters.
    fn hotspots_at_line(&self, line: i32) -> Vec<Rc<Box<dyn HotSpotImpl>>>;
}
impl FilterChainImpl for RefCell<Vec<Rc<RefCell<dyn Filter>>>> {
    fn add_filter(&self, filter: Rc<RefCell<dyn Filter>>) {
        self.borrow_mut().push(filter)
    }

    fn remove_filter(&self, filter: Rc<RefCell<dyn Filter>>) {
        self.borrow_mut()
            .retain(|f| !filter_equals(filter.clone(), f.clone()))
    }

    fn contains_filter(&self, filter: Rc<RefCell<dyn Filter>>) -> bool {
        for f in self.borrow().iter() {
            if filter_equals(f.clone(), filter.clone()) {
                return true;
            }
        }
        false
    }

    fn clear(&self) {
        self.borrow_mut().clear()
    }

    fn reset(&self) {
        for filter in self.borrow_mut().iter_mut() {
            filter.borrow_mut().reset()
        }
    }

    fn process(&self) {
        for filter in self.borrow_mut().iter_mut() {
            filter.borrow_mut().process(&URL_AND_EMAIL_REGEX)
        }
    }

    fn set_buffer(&self, buffer: Rc<RefCell<String>>, line_position: Rc<RefCell<Vec<i32>>>) {
        for filter in self.borrow_mut().iter_mut() {
            filter
                .borrow_mut()
                .set_buffer(buffer.clone(), line_position.clone())
        }
    }

    fn hotspot_at(&self, line: i32, column: i32) -> Option<Rc<Box<dyn HotSpotImpl>>> {
        for filter in self.borrow().iter() {
            let spot = filter.borrow().hotspot_at(line, column);
            if spot.is_some() {
                return spot;
            }
        }
        None
    }

    fn hotspots(&self) -> Vec<Rc<Box<dyn HotSpotImpl>>> {
        let mut hotspots = vec![];
        for filter in self.borrow().iter() {
            for spot in filter.borrow().hotspots().iter() {
                hotspots.push(spot.clone())
            }
        }
        hotspots
    }

    fn hotspots_at_line(&self, line: i32) -> Vec<Rc<Box<dyn HotSpotImpl>>> {
        let mut hotspots = vec![];
        for filter in self.borrow().iter() {
            let borrowed = filter.borrow();
            let spots = borrowed.hotspots_at_line(line);
            if let Some(spots) = spots {
                for spot in spots.iter() {
                    hotspots.push(spot.clone())
                }
            }
        }
        hotspots
    }
}

pub struct TerminalImageFilterChain {
    filter_chain: FilterChain,

    buffer: Rc<RefCell<String>>,
    line_positions: Rc<RefCell<Vec<i32>>>,
}
impl TerminalImageFilterChain {
    pub fn new() -> Self {
        Self {
            filter_chain: RefCell::new(vec![]),
            buffer: Rc::new(RefCell::new(String::new())),
            line_positions: Rc::new(RefCell::new(vec![])),
        }
    }
    fn set_image(
        &mut self,
        image: &[Character],
        lines: i32,
        columns: i32,
        line_propeerties: &[LineProperty],
    ) {
        if self.filter_chain.borrow().is_empty() {
            return;
        }

        // Reset all filters and hotspots
        self.filter_chain.reset();

        let mut decoder = PlainTextDecoder::new();
        decoder.set_trailing_whitespace(false);

        // Setup new shared buffers for the filters to process
        self.buffer = Rc::new(RefCell::new(String::new()));
        self.line_positions = Rc::new(RefCell::new(vec![]));
        self.set_buffer(self.buffer.clone(), self.line_positions.clone());

        let mut buffer_ref_mut = self.buffer.borrow_mut();
        let mut line_stream = TextStream::new(buffer_ref_mut.deref_mut());
        decoder.begin(&mut line_stream);

        for i in 0..lines as usize {
            self.line_positions
                .borrow_mut()
                .push(self.buffer.borrow().chars().count() as i32);
            decoder.decode_line(&image[i * columns as usize..], columns, LINE_DEFAULT);

            // pretend that each line ends with a newline character.
            // this prevents a link that occurs at the end of one line
            // being treated as part of a link that occurs at the start of the next line
            //
            // the downside is that links which are spread over more than one line are
            // not highlighted.
            //
            // TODO - Use the "line wrapped" attribute associated with lines in a
            // terminal image to avoid adding this imaginary character for wrapped
            // lines
            let get = line_propeerties.get(i);
            let line_property = if get.is_some() {
                *get.unwrap()
            } else {
                LINE_WRAPPED
            };
            if line_property & LINE_WRAPPED <= 0 {
                decoder.new_line();
            }
        }
        decoder.end()
    }
}
impl FilterChainImpl for TerminalImageFilterChain {
    fn add_filter(&self, filter: Rc<RefCell<dyn Filter>>) {
        self.filter_chain.add_filter(filter)
    }

    fn remove_filter(&self, filter: Rc<RefCell<dyn Filter>>) {
        self.filter_chain.remove_filter(filter)
    }

    fn contains_filter(&self, filter: Rc<RefCell<dyn Filter>>) -> bool {
        self.filter_chain.contains_filter(filter)
    }

    fn clear(&self) {
        self.filter_chain.clear()
    }

    fn reset(&self) {
        self.filter_chain.reset()
    }

    fn process(&self) {
        self.filter_chain.process()
    }

    fn set_buffer(&self, buffer: Rc<RefCell<String>>, line_position: Rc<RefCell<Vec<i32>>>) {
        self.filter_chain.set_buffer(buffer, line_position)
    }

    fn hotspot_at(&self, line: i32, column: i32) -> Option<Rc<Box<dyn HotSpotImpl>>> {
        self.filter_chain.hotspot_at(line, column)
    }

    fn hotspots(&self) -> Vec<Rc<Box<dyn HotSpotImpl>>> {
        self.filter_chain.hotspots()
    }

    fn hotspots_at_line(&self, line: i32) -> Vec<Rc<Box<dyn HotSpotImpl>>> {
        self.filter_chain.hotspots_at_line(line)
    }
}
