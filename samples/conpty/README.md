# ConPTY sample app
This is a very simple sample application that illustrates how to use the new Win32 Pseudo Console 
(ConPTY) by:  

1. Creating an input and an output pipe  
2. Calling `CreatePseudoConsole()` to create a ConPTY instance attached to the other end of the pipes  
3. Spawning an instance of `ping.exe` connected to the ConPTY  
4. Running a thread that listens for output from `ping.exe`, writing received text to the Console  

[ConPTY Introduce](https://devblogs.microsoft.com/commandline/windows-command-line-introducing-the-windows-pseudo-console-conpty/)  

## Requirements
- Windows 10 Insider build 17733 or later
- [Latest Windows 10 Insider SDK](https://www.microsoft.com/en-us/software-download/windowsinsiderpreviewSDK)

