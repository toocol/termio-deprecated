use rust_embed::RustEmbed;

#[derive(RustEmbed)]
#[folder = "resources/"]
#[include = "*.keytab"]
#[include = "*.colorscheme"]
#[include = "*.schema"]
pub struct Asset;