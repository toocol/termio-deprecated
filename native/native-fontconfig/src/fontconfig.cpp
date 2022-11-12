#include "fontconfig.h"
#include <iostream>

REXPORT void RCALL load_font(cstring font_path) {
#ifdef _MSVC_LANG
  int result = AddFontResourceEx((LPCWSTR)font_path, FR_PRIVATE, 0);
  if (result) {
    std::cout<< "Load font success: " << font_path << std::endl;
  } else {
    std::cerr<< "Load font failed: " << font_path << std::endl;
  }
#else
#endif
}