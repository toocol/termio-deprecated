#include "pipeio.h"
#include <process.h>
#include <mutex>

using namespace std;
using namespace _winconpty_;

void __cdecl readPipeListener(void*);

struct PASS_PARAM {
  int fd;
  function<void(char*)> whenRecieve;
};

void startReadListener(int fd, std::function<void(char*)> whenRecieve) {
  PASS_PARAM pp{fd, whenRecieve};
  _beginthread(readPipeListener, 0, &pp);
}

void writeData(int fd, char* data) {
  CONPTY* conpty = conptysMap[fd];
  HANDLE hPipe{conpty->pipeOutTerminalSide};

  DWORD dwBytesRead{sizeof(data)};
  DWORD dwBytesWritten{};
  BOOL fwrite{FALSE};

  // Write received text to the ConPTY.
  fwrite = WriteFile(hPipe, data, dwBytesRead, &dwBytesWritten, NULL);
}

void __cdecl readPipeListener(void* p) {
  PASS_PARAM* pp = (PASS_PARAM*)p;
  CONPTY* conpty = conptysMap[pp->fd];
  HANDLE hPipe{conpty->pipeInTerminalSide};

  const DWORD BUFF_SIZE{1024};
  char szBuffer[BUFF_SIZE]{};

  DWORD dwBytesWritten{};
  DWORD dwBytesRead{};
  BOOL fRead{FALSE};
  do {
    // Read from the pipe of ConPTY.
    fRead = ReadFile(hPipe, szBuffer, BUFF_SIZE, &dwBytesRead, NULL);
    pp->whenRecieve(szBuffer);

  } while (!conpty->closed);
}