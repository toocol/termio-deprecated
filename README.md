
<div align="center" >
    <img src="https://raw.githubusercontent.com/Joezeo/terminatio/8a94449d3ee343151a397fe7b7db4fad212fa00b/github.svg">
</div>

<h1 align="center"> Terminatio </h1>

<div align="center">

<big>***A light and handy command-line ssh client.***</big>

</div>


<div align="center" >

![Purely java project](https://img.shields.io/badge/Language-Java/OracleOpenJDK17-orange) ![License](https://img.shields.io/badge/License-Apache--2.0-red) ![Support](https://img.shields.io/badge/Support-Windows%2FLinux-%2320B2AA) ![Vert.x](https://img.shields.io/badge/Vert.x-3.5.4-%236699CC) ![JSch](https://img.shields.io/badge/JSch-0.1.55-%23CCCCFF)   

</div>

### Profile
> The program is mainly based on Java OpenJDK 17 with Vert.x and JSch, it's a lightweight command line SSH terminal tool;   
>  
> Provide SSH login **credential storage** and **fast login**, as well as the **async ftp function**;  
> 
> Vim file editing function is not supported for the time now, and it may be accomplished in the future.  

### Build with source code 
```
1. Download the Oracle OpenJDK 17, and add it's home path to System Variable "%JAVA17_HOME%";

2. Execute maven command 'mvn package' to generate .jar file;

3. Execute the batch file: 
        /starter/run.bat (For Windows, running it with 'Windows Terminal' to get better performance)
        /starter/run.sh  (For Unix/Linux)
        /starter/winpty_run.sh (If you use Cygwin or Git-Bash which terminals base on mintty)
```

### Commands
```
Terminal commands:     [param] means optional param
        help            -- Show holistic executive command.
        clear           -- Clear the screen.
        exit            -- Exit terminatio.
        add             -- Add new ssh connection property. Pattern: 'add --host@user -c=password [-p=port]',
                        default port is 22.
        delete          -- Delete ssh connection property. Pattern: 'delete --index', 
                        for example: 'delete --1'.
        numbers         -- Select the connection properties.


Shell commands:        [param] means optional param
        exit            -- Exit current shell, close ssh connection and destroy connect channel.
        hang            -- Will not close the connection, exit shell with connection running 
                        in the background.
```

## License
[Apache-2.0](LICENSE) © Joe Zane