extern crate proc_macro;

use quote::quote;
use syn::{self, DeriveInput};

#[proc_macro_derive(Dispatchable)]
pub fn dispatchable_macro_derive(input: proc_macro::TokenStream) -> proc_macro::TokenStream {
    let input = proc_macro2::TokenStream::from(input);
    // Build AST grammer tree based on input
    let ast = syn::parse2::<DeriveInput>(input).unwrap();

    // build implement code
    let name = &ast.ident;
    let gen = quote! {
        impl Dispatchable for #name {}
    };
    gen.into()
}
