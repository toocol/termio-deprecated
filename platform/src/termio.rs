use std::path::PathBuf;
use gtk::glib;
use utilities::{DynamicBundle, Locale};

pub const APP_COMMUNITY_ID: &str = "termio.community";
pub const APP_PERSONAL_ID: &str = "termio.personal";
pub const APP_PROFESSIONAL_ID: &str = "termio.professional";
pub const APP_ULTIMATE_ID: &str = "termio.ultimate";

pub struct LanguageBundle;
impl DynamicBundle for LanguageBundle {
    const PROPERTY: &'static str = "language";

    fn locales() -> Vec<utilities::Locale> {
        Locale::all()
    }
}

#[derive(Debug)]
pub enum Termio {
    TermioCommunity,
    TermioPersonal,
    TermioProfessional,
    TermioUltimate,
}

impl Termio {
    pub fn app_id(&self) -> &str {
        match self {
            Termio::TermioCommunity => APP_COMMUNITY_ID,
            Termio::TermioPersonal => APP_PERSONAL_ID,
            Termio::TermioProfessional => APP_PROFESSIONAL_ID,
            Termio::TermioUltimate => APP_ULTIMATE_ID,
        }
    }
}

pub fn data_path(file_name: &str, termio: &Termio) -> PathBuf {
    let mut path = glib::user_data_dir();
    path.push(termio.app_id());
    std::fs::create_dir_all(&path).expect("Could not create data directory.");
    path.push(file_name);
    path
}
