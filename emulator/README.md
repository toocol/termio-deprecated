# Native Terminal Emulator 

## Compiler
MSVC2019 for Windows  
GNU for Linux

## Requirements

### [mman](https://github.com/alitrack/mman-win32)
```
cmake .. -DCMAKE_INSTALL_PREFIX=C:\Windows\System32 -DBUILD_SHARED_LIBS=OFF
cmake --build ./ --config Release
cmake --install ./
```

### [utf8proc](https://github.com/JuliaStrings/utf8proc)
```
cmake .. -DCMAKE_INSTALL_PREFIX=C:\Windows\System32 -DBUILD_SHARED_LIBS=ON
cmake --build ./ --config Release 
cmake --install ./
```

### [boost](https://www.boost.org/users/history/version_1_80_0.html)

`Install:`
- click `bootstrap.bat` to generate `b2.exe`;
- ```./b2 [toolset=msvc-14.1] link=static runtime-link=shared threading=multi variant=debug/release```