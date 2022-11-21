use rust_embed::RustEmbed;

#[derive(RustEmbed)]
#[folder = "../src/resources/"]
#[include = "*.properties"]
#[include = "*.svg"]
pub struct Asset;
