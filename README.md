
<!--
<div align="center" >
    <img src="https://raw.githubusercontent.com/Joezeo/terminatio/8a94449d3ee343151a397fe7b7db4fad212fa00b/github.svg">
</div>
-->

<h1 align="center"> Termio </h1>

<div align="center">

<big>***Terminal for modern time.***</big>  

</div>


<div align="center" >

![Language](https://img.shields.io/badge/Language-Rust/C++-FFF7E9) ![License](https://img.shields.io/badge/License-AGPL--3.0-B9E0FF) ![Support](https://img.shields.io/badge/Support-Windows%2FLinux%2FMacos-CD97F9) 

</div>

### About Termio
Provide `remote session management`, `async sftp`, `custom commands`, `custom workflow`, `advanced history command`, `plugin extensions` etc.  
Support various local shell startup, also remote protocol of Ssh/Mosh/Telnet/Rsh...   

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
