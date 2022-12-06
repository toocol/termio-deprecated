pub struct CommandFeedback {
    pub command: String,
    pub param: Option<String>,
    pub comment: String,
    pub shortcuts: Vec<&'static str>,
}

#[derive(Default)]
pub struct DynamicFeedback;
impl DynamicFeedback {
    pub fn new() -> Self {
        DynamicFeedback {}
    }

    pub fn dynamic_feedback(&self, _input: &str) -> Vec<CommandFeedback> {
        let feedbacks = vec![];
        feedbacks
    }
}
