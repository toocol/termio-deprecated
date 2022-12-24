#![allow(dead_code)]

use std::{collections::HashMap, rc::Rc};
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
impl HotSpot {
    /// Constructs a new hotspot which covers the area from (@p startLine, @p startColumn)
    /// to (@p endLine,@p endColumn) in a block of text.
    pub fn new(start_line: i32, start_column: i32, end_line: i32, end_column: i32) -> Self {
        todo!()
    }

    /// Returns the line when the hotspot area starts
    pub fn start_line(&self) -> i32 {
        todo!()
    }

    /// Returns the line where the hotspot area ends
    pub fn end_line(&self) -> i32 {
        todo!()
    }

    /// Returns the column on startLine() where the hotspot area starts
    pub fn start_column(&self) -> i32 {
        todo!()
    }

    /// Returns the column on endLine() where the hotspot area ends
    pub fn end_column(&self) -> i32 {
        todo!()
    }

    /// Returns the type of the hotspot.  This is usually used as a hint for
    /// views on how to represent the hotspot graphically.  eg.  Link hotspots
    /// are typically underlined when the user mouses over them
    pub fn type_(&self) -> HotSpotType {
        todo!()
    }

    /// Causes the an action associated with a hotspot to be triggered.
    ///
    /// @param action The action to trigger.  This is
    /// typically empty ( in which case the default action should be performed )
    /// or one of the object names from the actions() list.  In which case the
    /// associated action should be performed.
    pub fn activate(&self, action: &str) {
        todo!()
    }

    /// Sets the type of a hotspot.  This should only be set once
    fn set_type(&self, type_: HotSpotType) {
        todo!()
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
    hotspots: HashMap<i32, Vec<Rc<HotSpot>>>,
    hostspots_list: Vec<Rc<HotSpot>>,

    line_positions: Vec<Vec<i32>>,
    buffer: String,
}
pub trait Filter {
    /// Causes the filter to process the block of text currently in its internal buffer
    fn process(&mut self) {}

    /// Empties the filters internal buffer and resets the line count back to 0.
    /// All hotspots are deleted.
    fn reset(&mut self) {}

    /// Returns the hotspot which covers the given @p line and @p column, or 0 if
    /// no hotspot covers that area
    fn hotspot_at(&self) -> &HotSpot {
        todo!()
    }

    /// Returns the list of hotspots identified by the filter
    fn hotspots(&self) -> Vec<&HotSpot> {
        todo!()
    }

    /// Returns the list of hotspots identified by the filter which occur on a given line
    fn hotspots_at_line(&self, line: usize) -> Vec<HotSpot> {
        todo!()
    }

    /// Set the buffer
    fn set_buffer(&mut self, buffer: String, line_positions: &[i32]) {
        todo!()
    }
}
