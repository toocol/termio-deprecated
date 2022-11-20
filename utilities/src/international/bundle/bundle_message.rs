use std::collections::HashMap;

pub struct BoundleMessage {
    message_map: HashMap<String, String>,
}

impl BoundleMessage {
    pub fn generate(_properties_file_name: &str) -> Self {
        todo!()
    }

    pub fn get(&self, key: &str) -> String {
        self.message_map.get(key)
            .expect(format!("Key `{}` of Boundle message is not exist, please check the properties bundles.", key).as_str())
            .clone()
    }
}
