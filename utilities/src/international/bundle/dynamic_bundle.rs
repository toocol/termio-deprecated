#![allow(dead_code)]
use std::collections::HashMap;

use crate::{bundle_message::BoundleMessage, with_locale, Locale};

pub struct DynamicBundle {
    bundle_message: HashMap<&'static Locale, BoundleMessage>,
}

pub trait IsDynamicBundle: 'static {
    /// The name of .properties file, locate at /resources/bundle/
    const PROPERTY: &'static str;
    /// The locales of this bundle should have.
    const LOCALES: Vec<Locale>;
    /// The object of DynamicBundle
    const BUNDLE: DynamicBundle;

    fn initialize_bundle(&self) {}

    fn message<T>(&self, key: &str, params: T) -> String
    where
        T: PartialEq + Clone + 'static,
    {
        let _message: String = with_locale(move |locale| {
            Self::BUNDLE
                .bundle_message
                .get(&*locale)
                .expect(
                    format!(
                        "BoundleMessage of locale `{}` does not exist",
                        locale.name()
                    )
                    .as_str(),
                )
                .get(key)
        });
        self._message("".to_string(), params, |_| "".to_string())
    }

    fn _message<T, RS: ParamsResolver<T>>(&self, content: String, params: T, resolver: RS) -> String
    where
        T: PartialEq + Clone + 'static,
    {
        resolver.call(params, content)
    }
}

pub trait ParamsResolver<Params> {
    fn call(&self, params: Params, content: String) -> String;
}

impl<F, P1> ParamsResolver<P1> for F
where
    P1: PartialEq + Clone + 'static,
    F: Fn(P1) -> String,
{
    fn call(&self, params: P1, _content: String) -> String {
        let _p1 = params;
        "Hello Wrold".to_string()
    }
}

// impl<F, P1, P2> ParamsResolver<(P1, P2)> for F
// where
//     P1: PartialEq + Clone + 'static,
//     P2: PartialEq + Clone + 'static,
//     F: Fn((P1, P2)) -> String,
// {
//     fn call(&self, params: (P1, P2), _content: String) -> String {
//         let (p1, p2) = params;
//         "Hello Wrold".to_string()
//     }
// }

// macro_rules! impl_component {
//     ($($P:ident),*) => {
//         impl<F, $($P,)*> $crate::ParamsResolver<( $($P,)* )> for F
//             where F: Fn($($P,)*) -> String,
//                   $( $P: ::std::cmp::PartialEq + ::std::clone::Clone + 'static, )*
//         {

//             fn call(&self, params: ( $($P,)* ), content: String) -> String {
//                 #[allow(non_snake_case)]
//                 let ($($P,)*) = params;
//                 "Hello Wrold".to_string()
//             }
//         }
//     };
// }
//
// impl_component!(P1);
// impl_component!(P1, P2);
