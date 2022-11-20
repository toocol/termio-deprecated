use std::collections::HashMap;

pub struct BoundleMessage {
    message_map: HashMap<String, String>,
}

impl BoundleMessage {
    pub fn generate(_properties_file_name: &str) -> Self {
        todo!()
    }

    pub fn get(&self, key: &str) -> Option<&String> {
        self.message_map.get(key)
    }
}
