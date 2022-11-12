@echo off
rd /s build
mkdir build
cd build
cmake .. -DCMAKE_INSTALL_PREFIX=../
cmake --build ./ --config Release
cmake --install ./
cd ..