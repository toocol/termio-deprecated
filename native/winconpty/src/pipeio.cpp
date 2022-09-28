#include "pipeio.h"
#include <process.h>
#include <mutex>

using namespace std;
using namespace _winconpty_;

const mutex mtx{};

void __cdecl readPipeListener(void*);
void __cdecl writePipeListener(void*);

void startReadPipeListener(int fd) { _beginthread(readPipeListener, 0, &fd); }

void startWritePipeListener(int fd) { _beginthread(writePipeListener, 0, &fd); }

void writeToRingBuffer(CONPTY*, char);
char* readFromRingBuffer(CONPTY*);
int available(CONPTY*);

void writeData(int fd, char* data) {
  CONPTY* conpty = conptysMap[fd];
  for (int i = 0; i < sizeof(data) / sizeof(char); i++) {
    writeToRingBuffer(conpty, data[i]);
  }
}

char* readData(int fd) {
  CONPTY* conpty = conptysMap[fd];
  return readFromRingBuffer(conpty);
}

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

void writeToRingBuffer(CONPTY* conpty, char ch) {
  conpty->ringBuffer[conpty->writeIndicator] = ch;
  conpty->writeIndicator =
      conpty->writeIndicator + 1 >= bufferSize ? 0 : conpty->writeIndicator + 1;
}

char* readFromRingBuffer(CONPTY* conpty) {
  char* buf = (char*)malloc(sizeof(char) * available(conpty));
  int idx = 0;
  while (conpty->readIndicator != conpty->writeIndicator) {
    buf[idx++] = conpty->ringBuffer[conpty->readIndicator];
    conpty->ringBuffer[conpty->readIndicator] = -1;
    conpty->readIndicator =
        conpty->readIndicator + 1 >= bufferSize ? 0 : conpty->readIndicator + 1;
  }
  return buf;
}

int available(CONPTY* conpty) {
  return conpty->writeIndicator < conpty->readIndicator
             ? bufferSize - conpty->readIndicator + conpty->writeIndicator
             : conpty->writeIndicator - conpty->readIndicator;
}
