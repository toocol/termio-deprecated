use protobuf_codegen::Customize;

fn main() {
    protobuf_codegen::Codegen::new()
        .customize(Customize::default().lite_runtime(false))
        // Use `protoc` parser, optional.
        .protoc()
        // Use `protoc-bin-vendored` bundled protoc command, optional.
        .protoc_path(&protoc_bin_vendored::protoc_bin_path().unwrap())
        // All inputs and imports from the inputs must reside in `includes` directories.
        .includes(&["proto"])
        // Inputs must reside in some of include paths.
        .input("proto/hostinput.proto")
        .input("proto/transportinstruction.proto")
        .input("proto/userinput.proto")
        // Specify output directory relative to Cargo output directory.
        .cargo_out_dir("target")
        .run_from_script();
}
