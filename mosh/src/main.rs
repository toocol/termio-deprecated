mod crypto;
mod frontend;
mod network;
mod proto;
mod statesync;
mod terminal;

use log::info;
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
        info!(
            "Parse startup parameter success, host = {}, port = {}, username = {}",
            host, port, username
        );
        Config {
            host,
            port,
            username,
            password,
        }
    }
}

fn main() {
    log4rs::init_file("log4rs.yaml", Default::default()).unwrap();

    let config = Config::new(env::args());
    let (port, key) = ssh_touch::ssh_touch(
        &config.host,
        &config.port,
        &config.username,
        &config.password,
    );
    let mut client = STMClient::new(config.host, port, key);

    client.main();
}
