#!/bin/bash
gcc -shared ./src/nativenode.cpp -I./src -I../src -I/home/toocol/Downloads/boost_1_79_0 -L/home/toocol/Downloads/boost_1_79_0/stage/lib -o nativenode.so
