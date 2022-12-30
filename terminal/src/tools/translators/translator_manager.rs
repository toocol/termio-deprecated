use log::warn;

use crate::asset::Asset;

use super::KeyboardTranslator;
use std::{collections::HashMap, rc::Rc};

/// Manages the keyboard translations available for use by terminal sessions
/// and loads the list of available keyboard translations.
///
/// The keyboard translations themselves are not loaded until they are
/// first requested via a call to find_translator()
pub struct KeyboardTranslatorManager {
    translators: HashMap<String, Rc<Box<KeyboardTranslator>>>,
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

    /// Adds a new translator.  If a translator with the same name already exists,
    /// it will be replaced by the new translator.
    pub fn add_translator(&mut self, translator: KeyboardTranslator) {
        todo!()
    }

    /// Deletes a translator.  Returns true on successful deletion or false otherwise.
    pub fn delete_translator(&mut self, name: String) -> bool {
        todo!()
    }

    /// Returns the default translator.
    pub fn default_translator(&self) -> Rc<Box<KeyboardTranslator>> {
        todo!()
    }

    /// Returns the keyboard translator with the given name or 0 if no translator
    /// with that name exists.
    ///
    /// The first time that a translator with a particular name is requested,
    /// the on-disk .keyboard file is loaded and parsed.
    pub fn find_translator(&mut self, name: String) -> Rc<Box<KeyboardTranslator>> {
        if name.is_empty() {
            return self.default_translator()
        }

        if self.translators.contains_key(&name) {
            return self.translators.get(&name).unwrap().clone()
        }

        let translator = self.load_translator(&name);
        if translator.is_some() {
            let translator = Rc::new(Box::new(translator.unwrap()));
            self.translators.insert(name, translator.clone());
            translator
        } else {
            warn!("Unable to load translator `{}`, use the default translator.", name);
            self.default_translator()
        }
    }

    /// Returns a list of the names of available keyboard translators.
    ///
    /// The first time this is called, a search for available translators is started.
    pub fn all_translators(&self) -> Vec<String> {
        todo!()
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
        todo!()
    }

    fn save_translator(&self, translator: KeyboardTranslator) -> bool {
        todo!()
    }

    fn find_translator_path(&self, name: String) -> String {
        todo!()
    }
}
