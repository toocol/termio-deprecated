#include "winconpty.h"

using namespace std;

static map<int, CONPTY*> conptysMap;
static mutex mtx;

volatile static int  counter = 1;
volatile static bool utf8    = false;

void __cdecl pipeListener(LPVOID);

int openConPty(int lines, int columns) {
  HRESULT hr{E_UNEXPECTED};
  CONPTY conpty{};

  if (CreatePipe(&conpty.pipeInPtySide, &conpty.pipeOutTerminalSide, NULL, 0) &&
      CreatePipe(&conpty.pipeInTerminalSide, &conpty.pipeOutPtySide, NULL, 0)) {
    COORD consoleSize{SHORT(columns), SHORT(lines)};
    hr = CreatePseudoConsole(consoleSize,             // ConPty Dimensions
                             conpty.pipeInPtySide,    // ConPty Input
                             conpty.pipeOutPtySide,   // ConPty Output
                             0,                       // ConPty Flags
                             &conpty.hpc              // ConPty Reference
    );
    if (S_OK == hr) {
      mtx.lock();
      conptysMap.insert(pair<int, CONPTY*>(counter, &conpty));
      conpty.fd = counter++;
      mtx.unlock();
    }
  }
  return S_OK == hr ? conpty.fd : -1;
}

void closeConPty(int fd) { 
  CONPTY* conpty = conptysMap[fd];
  // Close ConPTY - this will terminate client process if running
  ClosePseudoConsole(conpty->hpc);

  // Clean-up the pipes
  if (INVALID_HANDLE_VALUE != conpty->pipeOutTerminalSide) CloseHandle(conpty->pipeOutTerminalSide);
  if (INVALID_HANDLE_VALUE != conpty->pipeInTerminalSide) CloseHandle(conpty->pipeInTerminalSide);
  
  conptysMap.erase(fd);
}

CONPTY* getConPty(int fd) { 
  return conptysMap[fd]; 
}

void resizeConPty(int fd, int lines, int columns) { 
  CONPTY* conpty = getConPty(fd);
  COORD size{SHORT(columns), SHORT(lines)};
  ResizePseudoConsole(conpty->hpc, size);
}

void startSubProcess(int fd, LPSTR command) { 
  CONPTY* conpty = getConPty(fd); 
}

void __cdecl pipeListener(LPVOID pipe) {
  HANDLE hPipe{pipe};
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
