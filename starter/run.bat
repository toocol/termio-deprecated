@echo off
set /p dir=<configuration.properties
start "%dir:" ./jar.sh