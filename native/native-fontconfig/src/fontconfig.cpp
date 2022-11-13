#include "fontconfig.h"
#include <iostream>

REXPORT void RCALL load_font(cstring font_path) {
#ifdef _MSVC_LANG
  int result = AddFontResource(font_path);
  if (result) {
    std::cout << "Load font success: " << font_path << std::endl;
  } else {
    std::cerr << "Load font failed: " << font_path << std::endl;
  }
#else
#endif
}

REXPORT void RCALL load_font_private(cstring font_path) {
#ifdef _MSVC_LANG
  int result = AddFontResourceEx(font_path, FR_PRIVATE, 0);
  if (result) {
    std::cout << "Load private font success: " << font_path << std::endl;
  } else {
    std::cerr << "Load private font failed: " << font_path << std::endl;
  }
#else
#endif
}

REXPORT void RCALL remove_font(cstring font_path) {
#ifdef _MSVC_LANG
  bool success = RemoveFontResource(font_path);
  if (success) {
    std::cout << "Remove font resource success: " << font_path << std::endl;
  } else {
    std::cerr << "Remove font resource failed: " << font_path << std::endl;
  }
#else
#endif
}