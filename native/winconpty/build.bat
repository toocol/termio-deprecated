@echo off
rd /s build
mkdir build
cd build
cmake .. -DCMAKE_INSTALL_PREFIX=C:\Windows\System32
cmake --build ./ --config Release
cmake --install ./
cd ..