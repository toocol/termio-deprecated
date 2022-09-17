g++ -shared ./src/jnitypeconverter.cpp ./src/nativenode.cpp -I./src -I./java -I../src -ID:/download/boost_1_80_0/ -LD:/download/boost_1_80_0/stage/lib -o nativenode.dll
