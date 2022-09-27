#include "pipeio.h"
#include <process.h>

using namespace _winconpty_;

void __cdecl readPipeListener(void*);
void __cdecl writePipeListener(void*);

void startReadPipeListener(int fd) { _beginthread(readPipeListener, 0, &fd); }

void startWritePipeListener(int fd) { _beginthread(writePipeListener, 0, &fd); }

void writeData(int fd, std::wstring data) {}

std::wstring readData(int fd) { return std::wstring(); }

void __cdecl readPipeListener(void* pfd) {
  CONPTY* conpty = conptysMap[*(int*)pfd];
  HANDLE hPipe{conpty->pipeInTerminalSide};
  HANDLE hConsole{GetStdHandle(STD_OUTPUT_HANDLE)};

  const DWORD BUFF_SIZE{512};
  char szBuffer[BUFF_SIZE]{};

  DWORD dwBytesWritten{};
  DWORD dwBytesRead{};
  BOOL fRead{FALSE};
  do {
    // Read from the pipe
    fRead = ReadFile(hPipe, szBuffer, BUFF_SIZE, &dwBytesRead, NULL);

    // Write received text to the Console
    // Note: Write to the Console using WriteFile(hConsole...), not
    // printf()/puts() to prevent partially-read VT sequences from corrupting
    // output
    WriteFile(hConsole, szBuffer, dwBytesRead, &dwBytesWritten, NULL);

  } while (fRead && dwBytesRead >= 0);
}

void __cdecl writePipeListener(void* pfd) {}
