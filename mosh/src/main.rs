mod crypto;
mod frontend;
mod network;
mod proto;
mod statesync;
mod terminal;

use std::env::{self, Args};

use frontend::STMClient;

use crate::network::ssh_touch;

struct Config {
    pub host: String,
    pub port: String,
    pub username: String,
    pub password: String,
}
impl Config {
    pub fn new(mut args: Args) -> Self {
        args.next();

        let host = args.next().expect("Get host config failed.");
        let port = args.next().expect("Get port config failed.");
        let username = args.next().expect("Get username config failed.");
        let password = args.next().expect("Get password config failed.");
        Config {
            host,
            port,
            username,
            password,
        }
    }
}

fn main() {
    let config = Config::new(env::args());
    let (port, key) = ssh_touch::ssh_touch(
        &config.host,
        &config.port,
        &config.username,
        &config.password,
    );
    let client = STMClient::new(config.host, port, key);

    loop {
        client.tick();
    }
}
