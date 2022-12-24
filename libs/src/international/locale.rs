use std::{
    cell::{Ref, RefCell},
    sync::Mutex,
};

pub static CURRENT_LOCALE: Mutex<RefCell<Locale>> = Mutex::new(RefCell::new(Locale::LOCALE_EN));
#[derive(PartialEq, Eq, Hash, Clone, Copy)]
pub struct Locale {
    name: &'static str,
    suffix: &'static str,
}

impl Locale {
    pub const LOCALE_CH: Locale = Locale { name: "Chinese.ch", suffix: ".ch" };
    pub const LOCALE_EN: Locale = Locale { name: "English.en", suffix: ".en" };

    pub fn all() -> Vec<Self> {
        vec![Self::LOCALE_CH, Self::LOCALE_EN]
    }

    pub fn name(&self) -> &str {
        self.name
    }

    pub fn suffix(&self) -> &str {
        self.suffix
    }
}

pub fn with_locale<F, R>(f: F) -> R
where
    F: FnOnce(Ref<Locale>) -> R,
{
    f(CURRENT_LOCALE.lock().expect("Lock `LOCALE` error.").borrow())
}

pub fn change_locale(locale: Locale) {
    *CURRENT_LOCALE.lock().expect("Lock `LOCALE` error.").borrow_mut() = locale;
}


#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_locate() {
        with_locale(|locale| {
            assert_eq!(".en", locale.suffix());
        });
        change_locale(Locale::LOCALE_CH);
        with_locale(|locale| {
            assert_eq!(".ch", locale.suffix());
        });
    }
}
