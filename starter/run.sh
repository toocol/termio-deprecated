#!/bin/bash
$JAVA_HOME/bin/java \
-jar \
-Xdebug -Xrunjdwp:transport=dt_socket,suspend=n,server=y,address=5500 \
../core/target/termio.jar bash
