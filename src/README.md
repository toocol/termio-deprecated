## Install GTK4 on Windows via vcpkg
```
vcpkg install gtk:x64-windows
vcpkg install librsvg:x64-windows
```

Copy `gdk-pixbuf-query-loader.exe` to `\vcpkg\installed\x64-windows`  
Copy `pixbufloader-svg.dll` to `\vcpkg\installed\x64-windows\lib\gdk-pixbuf-2.0\2.10.0\loaders`  
Execute `gdk-pixbuf-query-loaders.exe --update-cache`  
  
---
~~Execute `gdk-pixbuf-query-loader` to regenerate `loader.cache` manually, and set the enviroment path `GDK_PIXBUF_MODULE_FILE` to locate `loader.cache`'s path prefix.~~  