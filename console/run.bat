@echo off
chcp 65001
%JAVA17_HOME%/bin/java ^
-jar ^
-Xdebug -Xrunjdwp:transport=dt_socket,suspend=n,server=y,address=5500 ^
-XX:G1PeriodicGCInterval=1000 -XX:G1PeriodicGCSystemLoadThreshold=0 ^
target/termio.jar

