use std::{path::PathBuf, env};

fn main() {
    let library_adapter = "native-adapter";
    let library_fontconfig = "native-fontconfig";
    let library_dir = PathBuf::from(env::var_os("CARGO_MANIFEST_DIR").unwrap());
    println!("cargo:rustc-link-lib=static={}", library_adapter);
    println!("cargo:rustc-link-lib=static={}", library_fontconfig);
    println!(
        "cargo:rustc-link-search=native={}/../",
        env::join_paths(&[library_dir]).unwrap().to_str().unwrap()
    );
}