#include "native_system.h"
#include <utf8proc.h>
#include <string>

REXPORT void* RCALL mmap_ffi(void* addr, size_t len, int prot, int flags,
                             int fildes, int64_t offset_type) {
  return mmap(addr, len, prot, flags, fildes, offset_type);
}

REXPORT int RCALL munmap_ffi(void* addr, size_t len) {
  return munmap(addr, len);
}

REXPORT int RCALL chsize_ffi(int file_handle, long size) {
#ifdef _MSVC_LANG
  return _chsize(file_handle, size);
#else
  return ftruncate(file_handle, size);
#endif
}

REXPORT int RCALL wcwidth_ffi(wchar_t ucs) {
  utf8proc_category_t cat = utf8proc_category(ucs);
  if (cat == UTF8PROC_CATEGORY_CO) {
    // Co: Private use area. libutf8proc makes them zero width, while tmux
    // assumes them to be width 1, and glibc's default width is also 1
    return 1;
  }
  return utf8proc_charwidth(ucs);
}

REXPORT int RCALL string_width_ffi(const wchar_t* wstr) {
  int w = 0;
  std::wstring str(wstr);
  for (size_t i = 0; i < str.length(); ++i) {
    w += wcwidth_ffi(str[i]);
  }
  return w;
}