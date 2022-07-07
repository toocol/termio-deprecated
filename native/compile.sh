#!/bin/bash
gcc -shared ./src/termio_unix.c -I ./src -o libtermio.so
