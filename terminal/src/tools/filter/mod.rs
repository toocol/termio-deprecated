#![allow(dead_code)]
pub mod filter_chain;
pub mod regex_filter;
pub mod url_filter;

pub use filter_chain::*;
pub use regex_filter::*;
use tmui::tlib::emit;
use tmui::tlib::object::{ObjectSubclass, ObjectImpl};
use tmui::tlib::{signals, signal};
pub use url_filter::*;

use crate::tools::system_ffi::string_width;
use lazy_static::__Deref;
use lazy_static::lazy_static;
use regex::Regex;
use std::{cell::RefCell, collections::HashMap, rc::Rc};
use tmui::prelude::*;
use widestring::U16String;

lazy_static! {
    pub static ref FULL_URL_REGEX: Regex = Regex::new(r"[a-zA-z]+://[^\s]*").unwrap();
    pub static ref EMAIL_ADDRESS_REGEX: Regex =
        Regex::new(r"[\w!#$%&'*+/=?^_`{|}~-]+(?:\.[\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\w](?:[\w-]*[\w])?\.)+[\w](?:[\w-]*[\w])?").unwrap();
    pub static ref URL_AND_EMAIL_REGEX: Regex =
        Regex::new(r"([a-zA-z]+://[^\s]*)|([\w!#$%&'*+/=?^_`{|}~-]+(?:\.[\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\w](?:[\w-]*[\w])?\.)+[\w](?:[\w-]*[\w])?)")
            .unwrap();
}

#[derive(Debug, Default, PartialEq, Eq, Clone, Copy)]
pub enum HotSpotType {
    /// the type of the hotspot is not specified
    #[default]
    NotSpecified,
    /// this hotspot represents a clickable link
    Link,
    /// this hotspot represents a marker
    Marker,
}

/// Represents an area of text which matched the pattern a particular filter has been looking for.
///
/// Each hotspot has a type identifier associated with it ( such as a link or a
/// highlighted section ), and an action.  When the user performs some activity
/// such as a mouse-click in a hotspot area ( the exact action will depend on
/// what is displaying the block of text which the filter is processing ), the
/// hotspot's activate() method should be called.  Depending on the type of
/// hotspot this will trigger a suitable response.
///
/// For example, if a hotspot represents a URL then a suitable action would be
/// opening that URL in a web browser. Hotspots may have more than one action,
/// in which case the list of actions can be obtained using the actions()
/// method.  These actions may then be displayed in a popup menu or toolbar for example.
#[derive(Debug, Default, PartialEq, Eq, Clone, Copy)]
pub struct HotSpot {
    start_line: i32,
    start_column: i32,
    end_line: i32,
    end_column: i32,
    type_: HotSpotType,
}
pub trait HotSpotConstructer {
    /// Constructs a new hotspot which covers the area from (@p startLine, @p startColumn)
    /// to (@p endLine,@p endColumn) in a block of text.
    fn new(start_line: i32, start_column: i32, end_line: i32, end_column: i32) -> Self;
}
pub trait HotSpotImpl {
    /// Returns the line when the hotspot area starts
    fn start_line(&self) -> i32;

    /// Returns the line where the hotspot area ends
    fn end_line(&self) -> i32;

    /// Returns the column on startLine() where the hotspot area starts
    fn start_column(&self) -> i32;

    /// Returns the column on endLine() where the hotspot area ends
    fn end_column(&self) -> i32;

    /// Returns the type of the hotspot.  This is usually used as a hint for
    /// views on how to represent the hotspot graphically.  eg.  Link hotspots
    /// are typically underlined when the user mouses over them
    fn type_(&self) -> HotSpotType;

    /// Causes the an action associated with a hotspot to be triggered.
    ///
    /// @param action The action to trigger.  This is
    /// typically empty ( in which case the default action should be performed )
    /// or one of the object names from the actions() list.  In which case the
    /// associated action should be performed.
    fn activate(&self, action: &str);

    fn actions(&self) -> Vec<Action>;

    /// Sets the type of a hotspot.  This should only be set once
    fn set_type(&mut self, type_: HotSpotType);
}
pub fn unsafe_as_hotspot_ref<T: HotSpotImpl>(hotspot: &mut dyn HotSpotImpl) -> &mut T {
    unsafe { &mut *(hotspot as *mut dyn HotSpotImpl as *mut T) }
}
impl HotSpotImpl for HotSpot {
    #[inline]
    fn start_line(&self) -> i32 {
        self.start_line
    }

    #[inline]
    fn end_line(&self) -> i32 {
        self.end_line
    }

    #[inline]
    fn start_column(&self) -> i32 {
        self.start_column
    }

    #[inline]
    fn end_column(&self) -> i32 {
        self.end_column
    }

    #[inline]
    fn type_(&self) -> HotSpotType {
        self.type_
    }

    #[inline]
    fn activate(&self, _action: &str) {}

    #[inline]
    fn set_type(&mut self, type_: HotSpotType) {
        self.type_ = type_
    }

    #[inline]
    fn actions(&self) -> Vec<Action> {
        vec![]
    }
}
impl HotSpotConstructer for HotSpot {
    fn new(start_line: i32, start_column: i32, end_line: i32, end_column: i32) -> Self {
        Self {
            start_line,
            start_column,
            end_line,
            end_column,
            type_: HotSpotType::NotSpecified,
        }
    }
}

/// A filter processes blocks of text looking for certain patterns (such as URLs
/// or keywords from a list) and marks the areas which match the filter's
/// patterns as 'hotspots'.
///
/// Each hotspot has a type identifier associated with it ( such as a link or a
/// highlighted section ), and an action.  When the user performs some activity
/// such as a mouse-click in a hotspot area ( the exact action will depend on
/// what is displaying the block of text which the filter is processing ), the
/// hotspot's activate() method should be called.  Depending on the type of
/// hotspot this will trigger a suitable response.
///
/// For example, if a hotspot represents a URL then a suitable action would be
/// opening that URL in a web browser. Hotspots may have more than one action, in
/// which case the list of actions can be obtained using the actions() method.
///
/// Different subclasses of filter will return different types of hotspot.
/// Subclasses must reimplement the process() method to examine a block of text
/// and identify sections of interest. When processing the text they should
/// create instances of Filter::HotSpot subclasses for sections of interest and
/// add them to the filter's list of hotspots using addHotSpot()
pub struct BaseFilter {
    hotspots: HashMap<i32, Vec<Rc<Box<dyn HotSpotImpl>>>>,
    hostspots_list: Vec<Rc<Box<dyn HotSpotImpl>>>,

    line_positions: Rc<RefCell<Vec<i32>>>,
    buffer: Rc<RefCell<String>>,
}
impl BaseFilter {
    pub fn new() -> Self {
        Self {
            hotspots: HashMap::new(),
            hostspots_list: vec![],
            line_positions: Rc::new(RefCell::new(vec![])),
            buffer: Rc::new(RefCell::new(String::new())),
        }
    }
}
trait BaseFilterImpl {
    fn add_hotspot(&mut self, hotspot: Box<dyn HotSpotImpl>);

    fn get_line_column(&self, position: i32) -> (i32, i32);
}
pub trait Filter {
    /// Causes the filter to process the block of text currently in its internal buffer
    fn process(&mut self, regex: &Regex);

    /// Empties the filters internal buffer and resets the line count back to 0.
    /// All hotspots are deleted.
    fn reset(&mut self);

    /// Returns the hotspot which covers the given @p line and @p column, or 0 if
    /// no hotspot covers that area
    fn hotspot_at(&self, line: i32, column: i32) -> Option<Rc<Box<dyn HotSpotImpl>>>;

    /// Returns the list of hotspots identified by the filter
    fn hotspots(&self) -> &Vec<Rc<Box<dyn HotSpotImpl>>>;

    /// Returns the list of hotspots identified by the filter which occur on a given line
    fn hotspots_at_line(&self, line: i32) -> Option<&Vec<Rc<Box<dyn HotSpotImpl>>>>;

    /// Set the buffer
    fn set_buffer(&mut self, buffer: Rc<RefCell<String>>, line_positions: Rc<RefCell<Vec<i32>>>);

    /// Get the buffer of filter
    fn buffer(&mut self) -> Rc<RefCell<String>>;
}
/// Judge whether two filters are the same
fn filter_equals(one: Rc<RefCell<dyn Filter>>, other: Rc<RefCell<dyn Filter>>) -> bool {
    one.deref().borrow().deref() as *const dyn Filter as *const u8 as i32
        == other.deref().borrow().deref() as *const dyn Filter as *const u8 as i32
}
impl BaseFilterImpl for BaseFilter {
    fn add_hotspot(&mut self, hotspot: Box<dyn HotSpotImpl>) {
        let spot = Rc::new(hotspot);
        self.hostspots_list.push(spot.clone());

        for i in spot.start_line()..spot.end_line() {
            let values = self.hotspots.entry(i).or_insert(vec![]);
            values.push(spot.clone());
        }
    }

    #[allow(unused_assignments)]
    fn get_line_column(&self, position: i32) -> (i32, i32) {
        assert!(position > 0);
        assert!(!self.buffer.deref().borrow().is_empty());

        let mut line_col = (0, 0);
        for i in 0..self.line_positions.deref().borrow().len() {
            let mut next_line = 0;

            if i as usize == self.line_positions.deref().borrow().len() - 1 {
                next_line = self.buffer.deref().borrow().len() + 1;
            } else {
                next_line = *self.line_positions.deref().borrow().get(i as usize + 1).unwrap() as usize;
            }

            if *self.line_positions.deref().borrow().get(i).unwrap() <= position && position < next_line as i32 {
                line_col.0 = i as i32;
                let line_position = *self.line_positions.deref().borrow().get(i).unwrap() as usize;
                let mut u16string =
                    U16String::from_str(&self.buffer.deref().borrow()[line_position..position as usize]);
                u16string.push_char('\0');
                line_col.1 = string_width(u16string.as_slice()) as i32;
                return line_col;
            }
        }
        line_col
    }
}
impl Filter for BaseFilter {
    fn process(&mut self, _: &Regex) {}

    fn reset(&mut self) {
        self.hotspots.clear();
        self.hostspots_list.clear();
    }

    fn hotspot_at(&self, line: i32, column: i32) -> Option<Rc<Box<dyn HotSpotImpl>>> {
        if let Some(lines) = self.hotspots.get(&line) {
            for spot in lines.iter() {
                if spot.start_line() == line && spot.start_column() > column {
                    continue;
                }
                if spot.end_line() == line && spot.end_column() < column {
                    continue;
                }

                return Some(spot.clone());
            }
            None
        } else {
            None
        }
    }

    #[inline]
    fn hotspots(&self) -> &Vec<Rc<Box<dyn HotSpotImpl>>> {
        &self.hostspots_list
    }

    #[inline]
    fn hotspots_at_line(&self, line: i32) -> Option<&Vec<Rc<Box<dyn HotSpotImpl>>>> {
        self.hotspots.get(&line)
    }

    #[inline]
    fn set_buffer(&mut self, buffer: Rc<RefCell<String>>, line_positions: Rc<RefCell<Vec<i32>>>) {
        self.buffer = buffer;
        self.line_positions = line_positions;
    }

    #[inline]
    fn buffer(&mut self) -> Rc<RefCell<String>> {
        self.buffer.clone()
    }
}

#[extends_object]
#[derive(Default)]
pub struct FilterObject {
    filter: Option<*mut dyn HotSpotImpl>,
}
impl ObjectSubclass for FilterObject {
    const NAME: &'static str = "FilterObject";

    type Type = FilterObject;

    type ParentType = Object;
}
impl ObjectImpl for FilterObject {}

impl FilterObject {
    pub const ACTION_FILTER_ACTIVATED: &'static str = "action-filter-activated";
    pub const ACTION_OPEN: &'static str = "action-open";
    pub const ACTION_COPY: &'static str = "action-copy";
    pub const ACTION_CLICK: &'static str = "action-click";

    signals!{
        /// Signal to activate ation `filter activated`.
        action_filter_activated();

        /// Signal to activate action `open`.
        action_open();

        /// Signal to activate action `copy`.
        action_copy();

        /// Signal to activate action `click`.
        action_click();
    }

    pub fn new() -> Self {
        Object::new(&[])
    }

    #[inline]
    pub fn emit_activated(&self, url: String, from_context_menu: bool) {
        emit!(self.action_filter_activated(), (url, from_context_menu))
    }

    pub fn activate(&self) {
        let filter_ref = self.filter.as_ref();
        if let Some(filter) = filter_ref {
            let copy = filter.clone();
            self.connect_action(self.action_open(), move |_| unsafe {
                copy.as_ref().unwrap().activate(Self::ACTION_OPEN)
            });

            let copy = filter.clone();
            self.connect_action(self.action_copy(), move |_| unsafe {
                copy.as_ref().unwrap().activate(Self::ACTION_COPY)
            });
        }
    }

    #[inline]
    pub fn set_filter(&mut self, filter: *mut dyn HotSpotImpl) {
        self.filter = Some(filter)
    }
}

#[cfg(test)]
mod tests {
    use std::rc::Rc;

    use lazy_static::__Deref;

    use crate::tools::filter::{EMAIL_ADDRESS_REGEX, URL_AND_EMAIL_REGEX};

    use super::FULL_URL_REGEX;

    #[test]
    fn test_regex() {
        assert!(FULL_URL_REGEX.is_match("http://www.google.com"));
        assert!(FULL_URL_REGEX.is_match("https://www.google.com"));
        assert!(!FULL_URL_REGEX.is_match("www.google.com"));
        assert!(!FULL_URL_REGEX.is_match("wwwgooglecom"));

        assert!(EMAIL_ADDRESS_REGEX.is_match("joezeo.cn@gmail.com"));
        assert!(EMAIL_ADDRESS_REGEX.is_match("xxx.d@kk.com"));
        assert!(!EMAIL_ADDRESS_REGEX.is_match("xxx.@kk.com"));
        assert!(!EMAIL_ADDRESS_REGEX.is_match("dsfsdkfjl.com"));

        assert!(URL_AND_EMAIL_REGEX.is_match("http://www.google.com"));
        assert!(URL_AND_EMAIL_REGEX.is_match("https://www.google.com"));
        assert!(!URL_AND_EMAIL_REGEX.is_match("www.google.com"));
        assert!(!URL_AND_EMAIL_REGEX.is_match("wwwgooglecom"));

        assert!(URL_AND_EMAIL_REGEX.is_match("joezeo.cn@gmail.com"));
        assert!(URL_AND_EMAIL_REGEX.is_match("xxx.d@kk.com"));
        assert!(!URL_AND_EMAIL_REGEX.is_match("xxx.@kk.com"));
        assert!(!URL_AND_EMAIL_REGEX.is_match("dsfsdkfjl.com"));
        assert!(!URL_AND_EMAIL_REGEX.is_match(""));
    }

    #[test]
    fn test_regex_caps() {
        for cap in URL_AND_EMAIL_REGEX
            .captures_iter("https://www.google.com joezeo.cn@gmail.com joezeo.cn@gmail.com")
        {
            for matched in cap.iter() {
                if let Some(m) = matched {
                    println!("{}", m.as_str());
                }
            }
            println!("---")
        }
    }

    #[test]
    fn test_equals() {
        let o1 = Rc::new("".to_string());
        let o2 = Rc::new("".to_string());
        let o1c = o1.clone();

        assert_eq!(
            o1.deref() as *const String as i32,
            o1c.deref() as *const String as i32
        );
        assert_ne!(
            o1.deref() as *const String as i32,
            o2.deref() as *const String as i32
        );
    }
}
