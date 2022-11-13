#ifndef _NATIVE_FONTCONFIG_H_
#define _NATIVE_FONTCONFIG_H_

#define REXPORT __declspec(dllexport)
#define RCALL __stdcall

typedef const char* cstring;

#ifdef _MSVC_LANG
#include <Windows.h>
#include <wingdi.h>
#else
#include <fontconfig/fontconfig.h>
#endif

extern "C" {
REXPORT void RCALL load_font(cstring);
REXPORT void RCALL load_font_private(cstring);
REXPORT void RCALL remove_font(cstring);
}

#endif