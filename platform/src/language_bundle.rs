use utilities::{DynamicBundle, Locale};

pub struct LanguageBundle;

impl LanguageBundle {
    pub const KEY_TEXT_SESSION_MANAGEMENT: &str = "text.session.management";
    pub const KEY_TEXT_SESSION_DEFAULT_GROUP: &str = "text.session.default.group";
    pub const KEY_TEXT_BASIC_SSH_SETTING: &str = "text.basic.ssh.setting";
    pub const KEY_TEXT_REMOTE_HOST: &str = "text.remote.host";
    pub const KEY_TEXT_USERNAME: &str = "text.username";
    pub const KEY_TEXT_PASSWORD: &str = "text.password";
    pub const KEY_TEXT_PORT: &str = "text.port";

    pub const KEY_TOOLTIP_TOGGLE_LEFT_SIDE_BAR: &str = "tooltip.toggle.left.side.bar";
    pub const KEY_TOOLTIP_TOGGLE_STATUS_BAR: &str = "tooltip.toggle.status.bar";
    pub const KEY_TOOLTIP_TOGGLE_SESSION_MANAGEMENT: &str = "tooltip.toggle.session.management";
    pub const KEY_TOOLTIP_TOGGLE_PLUGIN_EXTENSION: &str = "tooltip.toggle.plugin.extension";
    pub const KEY_TOOLTIP_TOGGLE_SETTING: &str = "tooltip.toggle.setting";
}

impl DynamicBundle for LanguageBundle {
    const PROPERTY: &'static str = "language";

    fn locales() -> Vec<utilities::Locale> {
        Locale::all()
    }
}
