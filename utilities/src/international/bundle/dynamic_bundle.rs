#![allow(dead_code)]
use std::{
    collections::HashMap,
    sync::{Mutex, MutexGuard},
};

use crate::{bundle_message::BundleMessage, with_locale, Locale};
use lazy_static::lazy_static;

const PROPERTIES_PATH_PREFIX: &str = "bundle/";

lazy_static! {
    static ref BUNDLES: Mutex<DynamicBundleStorage> = Mutex::new(DynamicBundleStorage {
        bundle_message: HashMap::new()
    });
}

struct DynamicBundleStorage {
    bundle_message: HashMap<&'static str, HashMap<Locale, BundleMessage>>,
}

/// ## Use DynamicBundle to realizing multilingual text display.
/// DynamicBundle get multilingual text from .properties multilingual bundle files locate in **src/resouces/bundle/**,
/// base on system current locale **locale::CURRENT_LOCALE**. You can change the system current local by **local::change_locale()**
///   
/// ### Usage:
/// ```
/// use utilities::DynamicBundle;
/// use utilities::Locale;
///
/// pub struct TestDynamicBundle;
/// impl DynamicBundle for TestDynamicBundle {
///    // The name of .properties file, locate at /resources/bundle/
///    // The actual bundle properties file should be "test.dynamic.bundle.cn.properties, test.dynamic.bundle.en.properties"
///    const PROPERTY: &'static str = "test.dynamic.bundle";
///
///    // The locales of this bundle should have.
///    fn locales() -> Vec<Locale> {
///        vec![Locale::LOCALE_CH, Locale::LOCALE_EN]
///    }
/// }
/// ```
/// then
/// ``` ignore
/// let val = TestDynamicBundle::message("key", None);
/// // This will replace all the `{}` in origin text by replace params.
/// let val = TestDynamicBundle::message("key", Some(vec!["replace_param", "replace_param"]));
/// ```
pub trait DynamicBundle: 'static {
    /// The name of .properties file, locate at /resources/bundle/
    const PROPERTY: &'static str;

    /// The locales of this bundle should have.
    fn locales() -> Vec<Locale>;

    fn message(key: &str, params: Option<Vec<&dyn ToString>>) -> String {
        let mut bundles = BUNDLES.lock().expect("Lock static field `BUNDLE` error.");
        if !bundles.bundle_message.contains_key(Self::PROPERTY) {
            initialize(&mut bundles, &Self::locales(), Self::PROPERTY)
        }

        let mut message: String =
            with_locale(
                move |locale| match bundles.bundle_message.get(Self::PROPERTY) {
                    Some(map_bundle) => match map_bundle.get(&locale) {
                        Some(bundle_message) => match bundle_message.get(key) {
                            Some(message) => message.clone(),
                            None => key.to_string(),
                        },
                        None => key.to_string(),
                    },
                    None => key.to_string(),
                },
            );

        if let Some(params) = params.as_ref() {
            params.iter().for_each(|param| {
                message = message.replacen("{}", param.to_string().as_str(), 1);
            });
        }

        message
    }
}

fn initialize(
    bundles: &mut MutexGuard<DynamicBundleStorage>,
    locales: &Vec<Locale>,
    property_name: &'static str,
) {
    let prefix = PROPERTIES_PATH_PREFIX.to_string();

    locales.iter().for_each(|locale| {
        let mut file_name = property_name.to_string();
        file_name.push_str(locale.suffix());
        file_name.push_str(".properties");

        let mut path = prefix.clone();
        path.push_str(file_name.as_str());

        let bundle_message = BundleMessage::generate(&path);

        bundles
            .bundle_message
            .entry(property_name)
            .or_insert(HashMap::new())
            .insert(locale.clone(), bundle_message);
    });
}

#[cfg(test)]
mod tests {
    use crate::change_locale;

    use super::*;

    struct TestDynamicBundle;
    impl DynamicBundle for TestDynamicBundle {
        const PROPERTY: &'static str = "language";

        fn locales() -> Vec<Locale> {
            vec![Locale::LOCALE_CH, Locale::LOCALE_EN]
        }
    }

    #[test]
    fn test_string_replace() {
        let mut str = "{} {}".to_string();
        let arr = ["Hello", "world"];

        arr.iter().for_each(|param| {
            str = str.replacen("{}", param, 1);
        });
        assert_eq!("Hello world", str);
    }

    #[test]
    fn test_dynamic_bundle() {
        change_locale(Locale::LOCALE_EN);
        assert_eq!(
            "DEFAULT",
            TestDynamicBundle::message("text.session.default.group", None)
        );

        change_locale(Locale::LOCALE_CH);
        assert_eq!(
            "默认",
            TestDynamicBundle::message("text.session.default.group", None)
        );
    }
}
