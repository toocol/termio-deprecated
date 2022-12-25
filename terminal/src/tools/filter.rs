#![allow(dead_code)]

use std::{cell::RefCell, collections::HashMap, rc::Rc, ptr::null_mut};

use lazy_static::__Deref;

use super::character::{Character, LineProperty};
#[derive(Debug, PartialEq, Eq, Clone, Copy)]
pub enum HotSpotType {
    /// the type of the hotspot is not specified
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
#[derive(Debug, PartialEq, Eq, Clone, Copy)]
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

    /// Sets the type of a hotspot.  This should only be set once
    fn set_type(&mut self, type_: HotSpotType);
}
impl HotSpotImpl for HotSpot {
    fn start_line(&self) -> i32 {
        self.start_line
    }

    fn end_line(&self) -> i32 {
        self.end_line
    }

    fn start_column(&self) -> i32 {
        self.start_column
    }

    fn end_column(&self) -> i32 {
        self.end_column
    }

    fn type_(&self) -> HotSpotType {
        self.type_
    }

    fn activate(&self, _action: &str) {}

    fn set_type(&mut self, type_: HotSpotType) {
        self.type_ = type_
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
pub struct FilterStruct {
    hotspots: HashMap<i32, Vec<Rc<Box<dyn HotSpotImpl>>>>,
    hostspots_list: Vec<Rc<Box<dyn HotSpotImpl>>>,

    line_positions: Vec<i32>,
    buffer: Vec<String>,
}
impl FilterStruct {
    pub fn new() -> Self {
        Self {
            hotspots: HashMap::new(),
            hostspots_list: vec![],
            line_positions: vec![],
            buffer: vec![],
        }
    }
}
pub trait Filter {
    /// Causes the filter to process the block of text currently in its internal buffer
    fn process(&mut self);

    /// Empties the filters internal buffer and resets the line count back to 0.
    /// All hotspots are deleted.
    fn reset(&mut self);

    /// Returns the hotspot which covers the given @p line and @p column, or 0 if
    /// no hotspot covers that area
    fn hotspot_at(&self, line: usize, colum: usize) -> &Box<dyn HotSpotImpl>;

    /// Returns the list of hotspots identified by the filter
    fn hotspots(&self) -> Vec<&Box<dyn HotSpotImpl>>;

    /// Returns the list of hotspots identified by the filter which occur on a given line
    fn hotspots_at_line(&self, line: usize) -> Vec<&Box<dyn HotSpotImpl>>;

    /// Set the buffer
    fn set_buffer(&mut self, buffer: String, line_positions: &[i32]);

    /// Judge whether two filters are the same
    fn equals(self: Rc<Self>, other: Rc<dyn Filter>) -> bool {
        self.deref() as *const Self as *const u8 as i32
            == other.deref() as *const dyn Filter as *const u8 as i32
    }
}
impl Filter for FilterStruct {
    fn process(&mut self) {
        todo!()
    }

    fn reset(&mut self) {
        todo!()
    }

    fn hotspot_at(&self, line: usize, colum: usize) -> &Box<dyn HotSpotImpl> {
        todo!()
    }

    fn hotspots(&self) -> Vec<&Box<dyn HotSpotImpl>> {
        todo!()
    }

    fn hotspots_at_line(&self, line: usize) -> Vec<&Box<dyn HotSpotImpl>> {
        todo!()
    }

    fn set_buffer(&mut self, buffer: String, line_positions: &[i32]) {
        todo!()
    }
}

/// Type of hotspot created by RegExpFilter.  The capturedTexts() method can be
/// used to find the text matched by the filter's regular expression.
pub struct RegexFilterHotSpot {
    hotspot: HotSpot,
    captured_texts: Vec<String>,
}
impl RegexFilterHotSpot {
    /// Sets the captured texts associated with this hotspot.
    pub fn set_captured_texts(&mut self, texts: Vec<String>) {
        self.captured_texts = texts;
    }

    /// Returns the texts found by the filter when matching the filter's regular expression.
    pub fn captured_texts(&self) -> &[String] {
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
}

/// A filter which searches for sections of text matching a regular expression
/// and creates a new RegExpFilter::HotSpot instance for them.
///
/// Subclasses can reimplement newHotSpot() to return custom hotspot types when
/// matches for the regular expression are found.
pub struct RegexFilter {
    filter: FilterStruct,
}
impl RegexFilter {
    pub fn new() -> Self {
        Self {
            filter: FilterStruct::new(),
        }
    }
}
impl Filter for RegexFilter {
    fn process(&mut self) {
        self.filter.process()
    }

    fn reset(&mut self) {
        self.filter.reset()
    }

    fn hotspot_at(&self, line: usize, colum: usize) -> &Box<dyn HotSpotImpl> {
        self.filter.hotspot_at(line, colum)
    }

    fn hotspots(&self) -> Vec<&Box<dyn HotSpotImpl>> {
        self.filter.hotspots()
    }

    fn hotspots_at_line(&self, line: usize) -> Vec<&Box<dyn HotSpotImpl>> {
        self.filter.hotspots_at_line(line)
    }

    fn set_buffer(&mut self, buffer: String, line_positions: &[i32]) {
        self.filter.set_buffer(buffer, line_positions)
    }
}

pub struct FilterObject {
    filter: Option<*mut dyn HotSpotImpl>,
}
impl FilterObject {
    const ACTION_FILTER_ACTIVATED: &'static str = "action-filter-activated";

    pub fn new() -> Self {
        Self { filter: None }
    }

    pub fn emit_activated(&self, url: String, from_context_menu: bool) {
        todo!()
    }

    pub fn activate(&self) {
        todo!()
    }

    pub fn set_filter(&mut self, filter: *mut dyn HotSpotImpl) {
        self.filter = Some(filter)
    }
}

pub struct UrlFilterHotSpot {
    hotspot: HotSpot,
    url_object: RefCell<FilterObject>,
}
impl UrlFilterHotSpot {}
impl HotSpotConstructer for UrlFilterHotSpot {
    fn new(start_line: i32, start_column: i32, end_line: i32, end_column: i32) -> Self {
        let mut hotspot = Self {
            hotspot: HotSpot { start_line, start_column, end_line, end_column, type_: HotSpotType::Link },
            url_object: RefCell::new(FilterObject::new()),
        };
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

    fn activate(&self, action: &str) {}

    fn set_type(&mut self, type_: HotSpotType) {
        self.hotspot.set_type(type_)
    }
}

/// A filter which matches URLs in blocks of text
pub struct UrlFilter {
    filter: FilterStruct,
}
impl UrlFilter {
    pub fn new() -> Self {
        Self {
            filter: FilterStruct::new(),
        }
    }
}
impl Filter for UrlFilter {
    fn process(&mut self) {
        self.filter.process()
    }

    fn reset(&mut self) {
        self.filter.reset()
    }

    fn hotspot_at(&self, line: usize, colum: usize) -> &Box<dyn HotSpotImpl> {
        self.filter.hotspot_at(line, colum)
    }

    fn hotspots(&self) -> Vec<&Box<dyn HotSpotImpl>> {
        self.filter.hotspots()
    }

    fn hotspots_at_line(&self, line: usize) -> Vec<&Box<dyn HotSpotImpl>> {
        self.filter.hotspots_at_line(line)
    }

    fn set_buffer(&mut self, buffer: String, line_positions: &[i32]) {
        self.filter.set_buffer(buffer, line_positions)
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
pub type FilterChain = RefCell<Vec<Rc<dyn Filter>>>;
pub trait FilterChainImpl {
    /// Adds a new filter to the chain.  The chain will delete this filter when it is destroyed.
    fn add_filter(&self, filter: Rc<dyn Filter>);

    /// Removes a filter from the chain.  The chain will no longer delete the filter when destroyed.
    fn remove_filter(&self, filter: Rc<dyn Filter>);

    /// Returns true if the chain contains @p filter.
    fn contains_filter(&self, filter: Rc<dyn Filter>);

    /// Removes all filters from the chain.
    fn clear(&self);

    /// Resets each filter in the chain.
    fn reset(&self);

    /// Processes each filter in the chain.
    fn process(&self);

    /// Sets the buffer for each filter in the chain to process.
    fn set_buffer(&self, buffer: Vec<String>, line_position: Vec<i32>);

    /// Returns the first hotspot which occurs at @p line, @p column or None if no hotspot was found
    fn hotspot_at(&self, line: i32, column: i32) -> Option<&Box<dyn HotSpotImpl>>;

    /// Returns a list of all the hotspots in all the chain's filters.
    fn hotspots(&self) -> Vec<&Box<dyn HotSpotImpl>>;

    /// Returns a list of all hotspots at the given line in all the chain's filters.
    fn hotspots_at_line(&self) -> Vec<&Box<dyn HotSpotImpl>>;
}
impl FilterChainImpl for RefCell<Vec<Rc<dyn Filter>>> {
    fn add_filter(&self, filter: Rc<dyn Filter>) {
        todo!()
    }

    fn remove_filter(&self, filter: Rc<dyn Filter>) {
        todo!()
    }

    fn contains_filter(&self, filter: Rc<dyn Filter>) {
        todo!()
    }

    fn clear(&self) {
        todo!()
    }

    fn reset(&self) {
        todo!()
    }

    fn process(&self) {
        todo!()
    }

    fn set_buffer(&self, buffer: Vec<String>, line_position: Vec<i32>) {
        todo!()
    }

    fn hotspot_at(&self, line: i32, column: i32) -> Option<&Box<dyn HotSpotImpl>> {
        todo!()
    }

    fn hotspots(&self) -> Vec<&Box<dyn HotSpotImpl>> {
        todo!()
    }

    fn hotspots_at_line(&self) -> Vec<&Box<dyn HotSpotImpl>> {
        todo!()
    }
}

pub struct TerminalImageFilterChain {
    filter_chain: FilterChain,

    buffer: Vec<String>,
    line_positions: Vec<i32>,
}
impl TerminalImageFilterChain {
    pub fn new() -> Self {
        Self {
            filter_chain: RefCell::new(vec![]),
            buffer: vec![],
            line_positions: vec![],
        }
    }
    fn set_image(
        &self,
        image: &[Character],
        lines: i32,
        columns: i32,
        line_propeerties: &[LineProperty],
    ) {
        todo!()
    }
}
impl FilterChainImpl for TerminalImageFilterChain {
    fn add_filter(&self, filter: Rc<dyn Filter>) {
        self.filter_chain.add_filter(filter)
    }

    fn remove_filter(&self, filter: Rc<dyn Filter>) {
        self.filter_chain.remove_filter(filter)
    }

    fn contains_filter(&self, filter: Rc<dyn Filter>) {
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

    fn set_buffer(&self, buffer: Vec<String>, line_position: Vec<i32>) {
        self.filter_chain.set_buffer(buffer, line_position)
    }

    fn hotspot_at(&self, line: i32, column: i32) -> Option<&Box<dyn HotSpotImpl>> {
        self.filter_chain.hotspot_at(line, column)
    }

    fn hotspots(&self) -> Vec<&Box<dyn HotSpotImpl>> {
        self.filter_chain.hotspots()
    }

    fn hotspots_at_line(&self) -> Vec<&Box<dyn HotSpotImpl>> {
        self.filter_chain.hotspots_at_line()
    }
}

#[cfg(test)]
mod tests {
    use std::rc::Rc;

    use lazy_static::__Deref;

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
