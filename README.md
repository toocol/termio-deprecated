# SSH Terminal 

![Purely java project](https://img.shields.io/badge/Language-java-orange) ![License](https://img.shields.io/badge/License-Apache--2.0-red) ![Support](https://img.shields.io/badge/Support-Windows%2FLinux-%2320B2AA)   
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;![Copilot](https://raw.githubusercontent.com/Joezeo/ssh_terminal/19b8a4e5968604ef01dd2b62bf1c64bb0ae3d2ed/github-copilot.svg)

> The program is mainly based on Vert.x and JSch, it's a lightweight command line SSH terminal tool;   
>  
> Provide SSH login credential storage and fast connection login, as well as the async ftp function;  
> 
> Vim file editing function is not supported for the time now, and it may be accomplished in the future.  

### Usage
```
1. Execute maven command 'mvn package' to generate .jar file;

2. Execute the batch file: /starter/run.bat (Use 'Windows Terminal' to get better performance).
```

### Commands
```
Terminal commands:     [param] means optional param
        help            -- Show holistic executive command.
        clear           -- Clear the screen.
        exit            -- Exit ssh terminal.
        add             -- Add new ssh connection property. Pattern: 'add --host@user@password[@port]',
                        default port is 22
        delete          -- Delete ssh connection property. Pattern: 'delete --index', for example: 'delete --1'
        numbers         -- Select the connection properties.


Shell commands:        [param] means optional param
        exit            -- Exit current shell, close ssh connection and destroy connect channel.
        hang            -- Will not close the connection, exit shell with connection running in the background.
        clear           -- Clear the screen.
```