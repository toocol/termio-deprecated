#ifndef __WINCONPTY_H_
#define __WINCONPTY_H_

#include <Windows.h>
#include <io.h>
#include <process.h>
#include <map>
#include <mutex>

struct CONPTY {
  HPCON hpc{INVALID_HANDLE_VALUE};
  HANDLE pipeInTerminalSide{INVALID_HANDLE_VALUE};
  HANDLE pipeOutTerminalSide{INVALID_HANDLE_VALUE};
  HANDLE pipeInPtySide{INVALID_HANDLE_VALUE};
  HANDLE pipeOutPtySide{INVALID_HANDLE_VALUE};

  PROCESS_INFORMATION pi{};

  int fd = 0;
};

int openConPty(int lines, int columns);

CONPTY* getConPty(int fd);

void setUTF8Mode(bool on);

void closeConPty(int fd);

void resizeConPty(int fd, int lines, int columns);

bool startSubProcess(int fd, LPSTR command);

#endif
