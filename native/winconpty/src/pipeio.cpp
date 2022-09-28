#include "pipeio.h"
#include <process.h>

using namespace _winconpty_;

void __cdecl readPipeListener(void*);
void __cdecl writePipeListener(void*);

void startReadPipeListener(int fd) { _beginthread(readPipeListener, 0, &fd); }

void startWritePipeListener(int fd) { _beginthread(writePipeListener, 0, &fd); }

void writeData(int, char*) {}

char* readData(int) { return nullptr; }

void __cdecl readPipeListener(void* pfd) {
  CONPTY* conpty = conptysMap[*(int*)pfd];
  HANDLE hPipe{conpty->pipeInTerminalSide};

  const DWORD BUFF_SIZE{512};
  char szBuffer[BUFF_SIZE]{};

  DWORD dwBytesWritten{};
  DWORD dwBytesRead{};
  BOOL fRead{FALSE};
  do {
    // Read from the pipe of ConPTY.
    fRead = ReadFile(hPipe, szBuffer, BUFF_SIZE, &dwBytesRead, NULL);

  } while (fRead && dwBytesRead >= 0);
}

void __cdecl writePipeListener(void* pfd) {
  CONPTY* conpty = conptysMap[*(int*)pfd];
  HANDLE hPipe{conpty->pipeOutTerminalSide};

  const DWORD BUFF_SIZE{512};
  char szBuffer[BUFF_SIZE]{};

  DWORD dwBytesRead{};
  DWORD dwBytesWritten{};
  BOOL fwrite{FALSE};

  do {
    // Write received text to the ConPTY.
    fwrite = WriteFile(hPipe, szBuffer, dwBytesRead, &dwBytesWritten, NULL);

  } while (fwrite && dwBytesRead >= 0);
}
