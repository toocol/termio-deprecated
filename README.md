<!--
<div align="center" >
    <img src="https://raw.githubusercontent.com/Joezeo/terminatio/8a94449d3ee343151a397fe7b7db4fad212fa00b/github.svg">
</div>
-->

<h1 align="center"> Termio </h1>

<div align="center">

<big>**_Terminal for modern time._**</big>

</div>

<div align="center" >

![Language](https://img.shields.io/badge/Language-Rust-FFF7E9) ![License](https://img.shields.io/badge/License-AGPL--3.0-B9E0FF) ![Support](https://img.shields.io/badge/Support-Windows%2FLinux%2FMacos-CD97F9)

</div>

### About Termio

Provide `remote session management`, `sftp`, `commands panel`, `workflow`, `plugin extensions` etc.  
Support various local shell startup, also remote protocol of Ssh/Mosh/Telnet/Rsh...

### Road Map

| Feature                                   | Status       |
| ----------------------------------------- | ------------ |
| Terminal Emulator                         | ✔Done        |
| Remote Session mangement                  | ✔Done        |
| Remote protocol support: SSH              | ✔Done        |
| Remote protocol support: Mosh             | 🚀Processing |
| Remote protocol support: Telnet           | 📌Waiting    |
| Remote protocol support: Rsh              | 📌Waiting    |
| UI Frontend                               | 🚀Processing |
| Customize Settings                        | 🚀Processing |
| Command Panel                             | 🚀Processing |
| Sftp                                      | 📌Waiting    |
| Workflow                                  | 📌Waiting    |
| Plug Extension                            | 📌Waiting    |

### Commands

```
 Termio commands:       [param] means optional param
 help                   Show holistic executive command.
 flush                  Flush the screen.
 exit                   Exit Termio.
 theme                  Change the Termio's color theme.
 add                    Add new ssh session property.
 delete                 Delete ssh session property.
 numbers                Select the session to active.
 active                 Active the mutiple session.

 Shell commands:        [param] means optional param
 exit                   Exit current shell, close ssh connection and destroy connect channel.
 hang                   Will not close the connection, exit shell with connection running in the background.
 uf                     Batch upload local files to remote connection.
 df                     Batch download remote files to local.
```

## License

[AGPL-3.0 license](LICENSE) © Joe Zane
