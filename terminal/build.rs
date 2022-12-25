use std::{path::PathBuf, env};

fn main() {
    let library_system = "native-system-bundle";
    let library_dir = PathBuf::from(env::var_os("CARGO_MANIFEST_DIR").unwrap());
    println!("cargo:rustc-link-lib=static={}", library_system);
    println!(
        "cargo:rustc-link-search=native={}/../",
        env::join_paths(&[library_dir]).unwrap().to_str().unwrap()
    );
}