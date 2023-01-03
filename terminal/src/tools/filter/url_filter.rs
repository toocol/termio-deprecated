use regex::Regex;
use tmui::tlib::object::{ObjectSubclass, ObjectImpl};
use std::{cell::RefCell, rc::Rc};
use tmui::prelude::*;

use super::regex_filter::{RegexFilter, RegexFilterHotSpot, RegexFilterHotSpotImpl};
use super::{
    BaseFilterImpl, Filter, FilterObject, HotSpotConstructer, HotSpotImpl, HotSpotType,
    EMAIL_ADDRESS_REGEX, FULL_URL_REGEX,
};

#[repr(C)]
#[derive(Debug, PartialEq, Eq, Clone, Copy)]
pub enum UrlType {
    StandardUrl,
    Email,
    Unknown,
}

#[extends_object]
#[derive(Default)]
pub struct UrlFilterHotSpot {
    hotspot: RegexFilterHotSpot,
    url_object: RefCell<FilterObject>,
}
impl ObjectSubclass for UrlFilterHotSpot {
    const NAME: &'static str = "UrlFilterHotSpot";

    type Type = UrlFilterHotSpot;

    type ParentType = Object;
}
impl ObjectImpl for UrlFilterHotSpot {}

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
        let mut hotspot: UrlFilterHotSpot = Object::new(&[]);

        hotspot.hotspot = RegexFilterHotSpot::new(start_line, start_column, end_line, end_column);
        hotspot.set_type(HotSpotType::Link);

        let ptr = &mut hotspot as *mut UrlFilterHotSpot as *mut dyn HotSpotImpl;
        hotspot.url_object.borrow().activate();
        hotspot.url_object.borrow_mut().set_filter(ptr);

        hotspot
    }
}
impl HotSpotImpl for UrlFilterHotSpot {
    #[inline]
    fn start_line(&self) -> i32 {
        self.hotspot.start_line()
    }

    #[inline]
    fn end_line(&self) -> i32 {
        self.hotspot.end_line()
    }

    #[inline]
    fn start_column(&self) -> i32 {
        self.hotspot.start_column()
    }

    #[inline]
    fn end_column(&self) -> i32 {
        self.hotspot.end_column()
    }

    #[inline]
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
                let open_action = self
                    .create_action_with_param(self.url_object.borrow().action_open(), "Open link");
                let copy_action = self.create_action_with_param(
                    self.url_object.borrow().action_open(),
                    "Copy link address",
                );
                list.push(open_action);
                list.push(copy_action);
            }
            UrlType::Email => {
                let open_action = self.create_action_with_param(
                    self.url_object.borrow().action_open(),
                    "Send email to...",
                );
                let copy_action = self.create_action_with_param(
                    self.url_object.borrow().action_copy(),
                    "Copy email address",
                );
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
    #[inline]
    fn process(&mut self, regex: &Regex) {
        self.filter.process(regex)
    }

    #[inline]
    fn reset(&mut self) {
        self.filter.reset()
    }

    #[inline]
    fn hotspot_at(&self, line: i32, column: i32) -> Option<Rc<Box<dyn HotSpotImpl>>> {
        self.filter.hotspot_at(line, column)
    }

    #[inline]
    fn hotspots(&self) -> &Vec<Rc<Box<dyn HotSpotImpl>>> {
        self.filter.hotspots()
    }

    #[inline]
    fn hotspots_at_line(&self, line: i32) -> Option<&Vec<Rc<Box<dyn HotSpotImpl>>>> {
        self.filter.hotspots_at_line(line)
    }

    #[inline]
    fn set_buffer(&mut self, buffer: Rc<RefCell<String>>, line_positions: Rc<RefCell<Vec<i32>>>) {
        self.filter.set_buffer(buffer, line_positions)
    }

    #[inline]
    fn buffer(&mut self) -> Rc<RefCell<String>> {
        self.filter.buffer()
    }
}
