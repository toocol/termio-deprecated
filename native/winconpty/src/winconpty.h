#ifndef __WINCONPTY_H_
#define __WINCONPTY_H_

#include <map>
#include "pipeio.h"

/**
 * Open a Windows pseudo console, and return the fd.
 */
__declspec(dllexport) int openConPty(int, int);
/**
 * Set global utf8 mode.
 */
__declspec(dllexport) void setUTF8Mode(bool);
/**
 * Close Windows pseudo console by fd.
 */
__declspec(dllexport) void closeConPty(int);
/**
 * Resize Windows pseudo console by fd.
 */
__declspec(dllexport) void resizeConPty(int, int, int);
/**
 * Start an sub process by command and combine it to Windows pseudo console by
 * fd.
 */
__declspec(dllexport) bool startSubProcess(int, LPWSTR);

#endif
