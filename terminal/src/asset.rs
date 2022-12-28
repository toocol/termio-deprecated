use rust_embed::RustEmbed;

#[derive(RustEmbed)]
#[folder = "../src/resources/"]
#[include = "*.keytab"]
#[include = "*.colorscheme"]
#[include = "*.schema"]
pub struct Asset;