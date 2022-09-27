#include "winconpty.h"
#include <io.h>
#include <process.h>
#include <map>
#include <mutex>

using namespace std;

static map<int, CONPTY*> conptysMap;
static mutex mtx;

volatile static int counter = 1;

void __cdecl pipeListener(LPVOID);

int openConPty(int lines, int columns) {
  HRESULT hr{E_UNEXPECTED};
  CONPTY conpty{};

  if (CreatePipe(&conpty.pipeInPtySide, &conpty.pipeOutTerminalSide, NULL, 0) &&
      CreatePipe(&conpty.pipeInTerminalSide, &conpty.pipeOutPtySide, NULL, 0)) {
    COORD consoleSize{SHORT(columns), SHORT(lines)};
    hr = CreatePseudoConsole(consoleSize,            // ConPty Dimensions
                             conpty.pipeInPtySide,   // ConPty Input
                             conpty.pipeOutPtySide,  // ConPty Output
                             0,                      // ConPty Flags
                             &conpty.hpc             // ConPty Reference
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
  if (INVALID_HANDLE_VALUE != conpty->pipeOutTerminalSide)
    CloseHandle(conpty->pipeOutTerminalSide);
  if (INVALID_HANDLE_VALUE != conpty->pipeInTerminalSide)
    CloseHandle(conpty->pipeInTerminalSide);

  conptysMap.erase(fd);
}

CONPTY* getConPty(int fd) { return conptysMap[fd]; }

void setUTF8Mode(bool on) {
  if (on) {
    SetConsoleCP(CP_UTF8);
    SetConsoleOutputCP(CP_UTF8);
  } else {
    SetConsoleCP(CP_ACP);
    SetConsoleOutputCP(CP_ACP);
  }
}

void resizeConPty(int fd, int lines, int columns) {
  CONPTY* conpty = getConPty(fd);
  COORD size{SHORT(columns), SHORT(lines)};
  ResizePseudoConsole(conpty->hpc, size);
}

bool startSubProcess(int fd, LPWSTR command) {
  CONPTY* conpty = getConPty(fd);
  HRESULT hr{E_UNEXPECTED};
  if (fd > 0 && conpty) {
    // Initialize the necessary startup info struct
    STARTUPINFOEX startupInfo{};
    size_t attrListSize{};

    startupInfo.StartupInfo.cb = sizeof(STARTUPINFOEX);

    // Get the size of the thread attribute list.
    InitializeProcThreadAttributeList(NULL, 1, 0, &attrListSize);

    // Allocate a thread attribute list of the correct size
    startupInfo.lpAttributeList =
        reinterpret_cast<LPPROC_THREAD_ATTRIBUTE_LIST>(malloc(attrListSize));

    // Initialize thread attribute list
    if (startupInfo.lpAttributeList &&
        InitializeProcThreadAttributeList(startupInfo.lpAttributeList, 1, 0,
                                          &attrListSize)) {
      // Set Pseudo Console attribute
      // This means major process read bytes from hpc(ConPTY)
      hr = UpdateProcThreadAttribute(startupInfo.lpAttributeList, 0,
                                     PROC_THREAD_ATTRIBUTE_PSEUDOCONSOLE,
                                     conpty->hpc, sizeof(HPCON), NULL, NULL)
               ? S_OK
               : HRESULT_FROM_WIN32(GetLastError());
    }
    if (S_OK == hr) {
      hr = CreateProcess(NULL,     // No module name - use Command Line
                         command,  // Command Line
                         NULL,     // Process handle not inheritable
                         NULL,     // Thread handle not inheritable
                         FALSE,    // Inherit handles
                         EXTENDED_STARTUPINFO_PRESENT,  // Creation flags
                         NULL,  // Use parent's environment block
                         NULL,  // Use parent's starting directory
                         &startupInfo.StartupInfo,  // Pointer to STARTUPINFO
                         &conpty->pi)
               ? S_OK
               : GetLastError();
    } else
      return false;

    // Cleanup attribute list
    DeleteProcThreadAttributeList(startupInfo.lpAttributeList);
    free(startupInfo.lpAttributeList);
  }
  return S_OK == hr;
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
