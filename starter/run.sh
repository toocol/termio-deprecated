#!/bin/bash
java -jar -Xdebug -Xrunjdwp:transport=dt_socket,suspend=n,server=y,address=5500 ../target/terminal.jar bash
