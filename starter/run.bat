@echo off
chcp 65001
%JAVA_HOME%\\bin\\java ^
-jar ^
-Xdebug -Xrunjdwp:transport=dt_socket,suspend=n,server=y,address=5500 ^
..\\target\\termio.jar cmd

