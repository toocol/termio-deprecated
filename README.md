
<div align="center" >
    <img src="https://raw.githubusercontent.com/Joezeo/terminatio/8a94449d3ee343151a397fe7b7db4fad212fa00b/github.svg">
</div>

<h1 align="center"> Terminatio </h1>

<div align="center">

<big>***A light and handy command-line ssh client.***</big>

</div>


<div align="center" >

![Purely java project](https://img.shields.io/badge/Language-Java-orange) ![License](https://img.shields.io/badge/License-Apache--2.0-red) ![Support](https://img.shields.io/badge/Support-Windows%2FLinux-%2320B2AA) ![Vert.x](https://img.shields.io/badge/Vert.x-3.5.4-%236699CC) ![JSch](https://img.shields.io/badge/JSch-0.1.55-%23CCCCFF)   

</div>

### Profile
> The program is mainly based on Vert.x and JSch, it's a lightweight command line SSH terminal tool;   
>  
> Provide SSH login **credential storage** and **fast login**, as well as the **async ftp function**;  
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
                        default port is 22.
        delete          -- Delete ssh connection property. Pattern: 'delete --index', 
                        for example: 'delete --1'.
        numbers         -- Select the connection properties.


Shell commands:        [param] means optional param
        exit            -- Exit current shell, close ssh connection and destroy connect channel.
        hang            -- Will not close the connection, exit shell with connection running 
                        in the background.
        clear           -- Clear the screen.
```

### Maven
If you want to run this program through source code, you need configure your maven's setting.xml to get the jnativehook dependency.  
Add flowing configuration to your local maven's settings.xml.
```xml
<?xml version="1.0"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
    
    <profiles>
        
        <profile>
         <id>github-maven</id>
            
         <activation>
             <activeByDefault>true</activeByDefault>
             <jdk>1.8</jdk>
         </activation>
            
         <repositories>
             <repository>
                 <id>github-maven-snapshot-repository</id>
                 <name>github-maven-snapshot-repository</name>
                 <url>https://oss.sonatype.org/content/repositories/snapshots/com</url>
             </repository>
         </repositories>
        </profile>
        
    </profiles>
 
    <pluginGroups>
        <pluginGroup>org.apache.maven.plugins</pluginGroup>
    </pluginGroups>
 
    <activeProfiles>
        <activeProfile>github-maven</activeProfile>
    </activeProfiles>
 
</settings>
```

## License
[Apache-2.0](LICENSE) © Joe Zane