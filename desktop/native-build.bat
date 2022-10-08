@echo off
%VCVARS%\vcvars64.bat ^
mvn gluonfx:build gluonfx:nativerun -f pom.xml
