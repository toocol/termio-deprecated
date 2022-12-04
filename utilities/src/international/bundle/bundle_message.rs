use encoding::label::encoding_from_whatwg_label;
use java_properties::PropertiesIter;
use std::{collections::HashMap, io::BufReader};

use crate::Asset;

pub struct BundleMessage {
    message_map: HashMap<String, String>,
}

impl BundleMessage {
    pub fn generate(properties_file_path: &str) -> Self {
        let mut bundle_message = BundleMessage {
            message_map: HashMap::new(),
        };

        let asset = Asset::get(properties_file_path)
            .expect(format!("Get embed asset `{}` failed.", properties_file_path).as_str());

        PropertiesIter::new_with_encoding(
            BufReader::new(asset.data.as_ref()),
            encoding_from_whatwg_label("utf-8").expect("Get encoding utf-8 failed"),
        )
        .read_into(|k, v| {
            bundle_message.insert(k, v);
        })
        .expect(format!("Read properties {} failed.", properties_file_path).as_str());

        bundle_message
    }

    pub fn get(&self, key: &str) -> Option<&String> {
        self.message_map.get(key)
    }

    fn insert(&mut self, key: String, val: String) {
        self.message_map.insert(key, val);
    }
}

#[cfg(test)]
mod tests {
    use crate::{change_locale, Locale};

    use super::*;

    #[test]
    fn test_bundle_message() {
        let mut path = String::new();
        path.push_str("bundle/language.en.properties");

        let message = BundleMessage::generate(&path);

        change_locale(Locale::LOCALE_EN);
        assert_eq!(message.get("text.session.default.group").unwrap(), "DEFAULT");
    }
}
