rem     This batch file was weired,
rem     but the memory of native executable file build by this batch file was only 40m.
rem     Otherwise the memory was up to 80m. And I don't what cause this.
@echo off
C:\Software\VisualStudio\VC\Auxiliary\Build\vcvars64.bat ^
& rd /s /q %MAVEN_HOME%\repository\com\gluonhq ^
& rd /s /q %MAVEN_HOME%\repository\org\graalvm ^
& mvn gluonfx:build -f pom-dirty.xml ^
& rd /s /q %MAVEN_HOME%\repository\com\gluonhq ^
& rd /s /q %MAVEN_HOME%\repository\org\graalvm ^
& mvn clean -f pom.xml ^
& cd .. ^
& mvn clean install -f pom.xml ^
& cd desktop ^
& mvn gluonfx:build gluonfx:nativerun -f pom.xml
