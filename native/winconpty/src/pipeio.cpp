#include "pipeio.h"
#include <process.h>
#include <codecvt>
#include <iostream>
#include <mutex>
#include <thread>

using namespace std;
using namespace _winconpty_;

void readPipeListener(int, function<void(char*, int)>);

void startReadListener(int fd, std::function<void(char*, int)> whenRecieve) {
  thread(&readPipeListener, fd, whenRecieve).detach();
}

void writeData(int fd, const char* data) {
  CONPTY* conpty = Storage::conptysMap[fd];
  if (!conpty || conpty->closed) return;

  DWORD dwBytesWritten{};
  BOOL fwrite{FALSE};

  // Write received text to the ConPTY.
  fwrite = WriteFile(conpty->pipeOutTerminalSide, data, (DWORD)strlen(data),
                     &dwBytesWritten, NULL);
}

void readPipeListener(int fd, function<void(char*, int)> whenRecieve) {
  CONPTY* conpty = Storage::conptysMap[fd];
  if (!conpty) return;

  const DWORD BUFF_SIZE{2 << 16};
  char szBuffer[BUFF_SIZE]{};
  DWORD dwBytesRead{};
  BOOL fRead{FALSE};

  do {
    // Read from the pipe of ConPTY.
    fRead = ReadFile(conpty->pipeInTerminalSide, szBuffer, BUFF_SIZE,
                     &dwBytesRead, NULL);

    if (fRead && dwBytesRead > 0) {
      whenRecieve(szBuffer, dwBytesRead + 1);
    }
    memset(szBuffer, 0, dwBytesRead);
  } while (!conpty->closed);

  delete conpty;
}
