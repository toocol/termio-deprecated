fn main() {
    protobuf_codegen::Codegen::new()
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
