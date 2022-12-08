#![allow(dead_code)]
pub struct SemanticToken {
    start: i32,
    len: i32,
}
pub struct SemanticMarkupBuilder;
impl SemanticMarkupBuilder {
    pub fn parse_markup(text: &str, rule: &str) -> String {
        let _tokens = Self::parse_tokens(text, rule);
        todo!()
    }

    fn parse_tokens(_text: &str, _rule: &str) -> Vec<SemanticToken> {
        todo!()
    }
}