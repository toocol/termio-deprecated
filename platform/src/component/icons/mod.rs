pub mod font_icon;
pub mod svg_icon;

pub use font_icon::*;
pub use svg_icon::*;

#[cfg(test)]
mod tests {
    use super::*;
    use gtk::{prelude::*, subclass::prelude::ObjectSubclassIsExt, Application};

    #[test]
    pub fn test_font_awesome() {
        let app = Application::builder()
            .application_id("com.termio.test_font_awesome")
            .build();

        app.connect_activate(|_| {
            let icon = FontIcon::new("f075", FontType::FontAwesomeFreeRegular);
            icon.set_color("#323232");
            icon.set_size(15);
            let markup = icon.imp().format_markup();
            assert_eq!("<span foreground=\"#323232\" font_desc=\"Font Awesome 6 Free Regular 15\" font_features=\"dlig=1\">\u{f075};</span>", markup);
        });

        app.run();
    }
}
