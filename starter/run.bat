@echo off
chcp 65001
%JAVA_HOME%\\bin\\java ^
-Djavafx.verbose=true ^
-Dprism.verbose=true ^
--module-path .\\libs\\javafx-sdk-17.0.2-windows-x64\\lib ^
--add-modules javafx.controls,javafx.fxml ^
-jar -Xdebug -Xrunjdwp:transport=dt_socket,suspend=n,server=y,address=5500 ..\\target\\terminatio.jar cmd

