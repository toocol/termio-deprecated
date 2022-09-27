#ifndef __WINCONPTY_H_
#define __WINCONPTY_H_

#include <Windows.h>

#ifndef _WINCONTYPES_
typedef VOID* HPCON;
#endif

struct CONPTY {
  HPCON hpc{INVALID_HANDLE_VALUE};
  HANDLE pipeInTerminalSide{INVALID_HANDLE_VALUE};
  HANDLE pipeOutTerminalSide{INVALID_HANDLE_VALUE};
  HANDLE pipeInPtySide{INVALID_HANDLE_VALUE};
  HANDLE pipeOutPtySide{INVALID_HANDLE_VALUE};

  PROCESS_INFORMATION pi{};

  int fd = 0;
};

__declspec(dllexport) int openConPty(int, int);

__declspec(dllexport) CONPTY* getConPty(int);

__declspec(dllexport) void setUTF8Mode(bool);

__declspec(dllexport) void closeConPty(int);

__declspec(dllexport) void resizeConPty(int, int, int);

__declspec(dllexport) bool startSubProcess(int, LPWSTR);

#endif
