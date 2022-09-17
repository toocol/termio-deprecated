gcc -shared ./src/nativenode.cpp -I./java -I./src -I../src -ID:/download/boost_1_80_0/ -LD:/download/boost_1_80_0/stage/lib -mwindows -o nativenode.dll
