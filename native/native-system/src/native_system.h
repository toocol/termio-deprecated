#ifndef _NATIVE_SYSTEM_H
#define _NATIVE_SYSTEM_H

#define REXPORT __declspec(dllexport)
#define RCALL __stdcall

#ifdef _MSVC_LANG
#include <unistd.h>
#include <windows.h>
#else
#include <sys/param.h>
#include <unistd.h>
#endif
#include <sys/mman.h>

extern "C" {
REXPORT void* RCALL mmap_ffi(void* addr, size_t len, int prot, int flags,
                             int fildes, int64_t offset_type);

REXPORT int RCALL munmap_ffi(void* addr, size_t len);

REXPORT int RCALL chsize_ffi(int file_handle, long size);

REXPORT int RCALL wcwidth_ffi(wchar_t ucs);

REXPORT int RCALL string_width_ffi(const wchar_t* wstr);
}
#endif