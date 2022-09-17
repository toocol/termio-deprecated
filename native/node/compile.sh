#!/bin/bash
g++ -shared ./src/jnitypeconverter.cpp ./src/nativenode.cpp -I./src -I./java -I../src -I/home/toocol/Downloads/boost_1_79_0 -L/home/toocol/Downloads/boost_1_79_0/stage/lib -o nativenode.so
