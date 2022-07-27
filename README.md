
<!--
<div align="center" >
    <img src="https://raw.githubusercontent.com/Joezeo/terminatio/8a94449d3ee343151a397fe7b7db4fad212fa00b/github.svg">
</div>
-->

<h1 align="center"> Termio </h1>

<div align="center">

<big>***A light and handy command-line ssh/mosh client.***</big>  
<big>***Keep your operation away from the mouse.***<big>
</div>


<div align="center" >

![Purely java project](https://img.shields.io/badge/Language-Java/OracleOpenJDK17-orange) ![License](https://img.shields.io/badge/License-Apache--2.0-red) ![Support](https://img.shields.io/badge/Support-Windows%2FLinux-%2320B2AA) 

</div>

### Profile
Termio it's a lightweight command line `SSH/Mosh` terminal tool operated based on some simple commands in terminal; 

Provide SSH/Mosh `credential storage`, `async sftp`, `quick session switch`, `advanced history command`, `function plugin extension` etc.

### Build with source code 
```
1. Download the Oracle OpenJDK 17, and add it's home path to System Variable "JAVA_HOME";

2. Execute maven command 'mvn package' to generate .jar file;

3. Execute the batch file: 
        /starter/run.bat (For Windows, running it with 'Windows Terminal' to get better performance)
        /starter/run.sh  (For Unix/Linux)
        /starter/winpty_run.sh (If you use Cygwin or Git-Bash which terminals base on mintty)
```

### Commands
```
 Termio commands:       [param] means optional param
 help                   Show holistic executive command.
 flush                  Flush the screen.
 exit                   Exit Termio.
 theme                  Change the Termio's color theme.
 add                    Add new ssh connection property.
 delete                 Delete ssh connection property.
 numbers                Select the connection properties.
 active                 Active the ssh connect session without enter the Shell.
 mosh                   Use mosh to connect remote device.

 Shell commands:        [param] means optional param
 exit                   Exit current shell, close ssh connection and destroy connect channel.
 hang                   Will not close the connection, exit shell with connection running in the background.
 uf                     Batch upload local files to remote connection.
 df                     Batch download remote files to local.
```

## License
[AGPL-3.0 license](LICENSE) © Joe Zane
