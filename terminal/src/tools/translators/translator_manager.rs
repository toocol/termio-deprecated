use super::{KeyboardTranslator, KeyboardTranslatorReader};
use crate::asset::Asset;
use log::warn;
use std::{collections::HashMap, rc::Rc, cell::RefCell};

const LAYOUT_PATH_PREFIX: &'static str = "kb-layouts/";
const LAYOUT_PATH_SUFFIX: &'static str = ".keytab";

/// Manages the keyboard translations available for use by terminal sessions
/// and loads the list of available keyboard translations.
///
/// The keyboard translations themselves are not loaded until they are
/// first requested via a call to find_translator()
pub struct KeyboardTranslatorManager {
    translators: HashMap<String, Rc<RefCell<Box<KeyboardTranslator>>>>,
    valid_translator_names: Vec<String>,
    have_load_all: bool,
}

impl KeyboardTranslatorManager {
    pub fn new() -> Self {
        Self {
            translators: HashMap::new(),
            valid_translator_names: vec![],
            have_load_all: false,
        }
    }

    /// Returns the default translator.
    pub fn default_translator(&self) -> Rc<RefCell<Box<KeyboardTranslator>>> {
        let translator = self.load_translator("default");
        Rc::new(RefCell::new(Box::new(translator.unwrap())))
    }

    /// Returns the keyboard translator with the given name or 0 if no translator
    /// with that name exists.
    ///
    /// The first time that a translator with a particular name is requested,
    /// the on-disk .keyboard file is loaded and parsed.
    pub fn find_translator(&mut self, name: String) -> Rc<RefCell<Box<KeyboardTranslator>>> {
        if name.is_empty() {
            return self.default_translator();
        }

        if self.translators.contains_key(&name) {
            return self.translators.get(&name).unwrap().clone();
        }

        let translator = self.load_translator(&name);
        if translator.is_some() {
            let translator = Rc::new(RefCell::new(Box::new(translator.unwrap())));
            self.translators.insert(name, translator.clone());
            translator
        } else {
            warn!(
                "Unable to load translator `{}`, use the default translator.",
                name
            );
            self.default_translator()
        }
    }

    /// Returns a list of the names of available keyboard translators.
    ///
    /// The first time this is called, a search for available translators is started.
    pub fn all_translators(&self) -> &[String] {
        &self.valid_translator_names
    }

    /// Locate the avaliable translators
    fn find_translators(&mut self) {
        for asset_name in Asset::iter() {
            if asset_name.ends_with(".keytab") {
                self.valid_translator_names.push(asset_name.to_string());
            }
        }
    }

    // Load the translator.
    fn load_translator(&self, name: &str) -> Option<KeyboardTranslator> {
        let mut full_name = LAYOUT_PATH_PREFIX.to_string();
        full_name.push_str(name);
        full_name.push_str(LAYOUT_PATH_SUFFIX);

        if let Some(asset) = Asset::get(&full_name) {
            let source = String::from_utf8(asset.data.to_vec())
                .expect("Parse keyboard layouts to utf-8 failed.");
            let mut translator = KeyboardTranslator::new(name.to_string());
            let mut reader = KeyboardTranslatorReader::new(source);
            translator.set_description(reader.description().to_string());
            while reader.has_next_entry() {
                translator.add_entry(reader.next_entry())
            }

            Some(translator)
        } else {
            None
        }
    }
}
