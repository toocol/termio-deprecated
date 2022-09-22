#!/bin/bash
gcc -shared ./src/termio_unix.c -I./src -I../jni -o libtermio.so
