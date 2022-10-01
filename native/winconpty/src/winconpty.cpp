#include "winconpty.h"
#include <io.h>
#include <process.h>
#include <mutex>

using namespace std;
using namespace _winconpty_;

static mutex mtx;

volatile static int counter = 1;

/**
 * Get Windows pseudo console by fd.
 */
CONPTY* getConPty(int);

int openConPty(int lines, int columns) {
  HRESULT hr{E_UNEXPECTED};
  CONPTY* conpty = new CONPTY{};
  HANDLE pipeInPtySide{INVALID_HANDLE_VALUE};
  HANDLE pipeOutPtySide{INVALID_HANDLE_VALUE};

  SECURITY_ATTRIBUTES security = {0};
  if (CreatePipe(&pipeInPtySide, &conpty->pipeOutTerminalSide, &security, 0) &&
      CreatePipe(&conpty->pipeInTerminalSide, &pipeOutPtySide, &security, 0)) {
    COORD consoleSize{SHORT(columns), SHORT(lines)};
    hr = CreatePseudoConsole(consoleSize,     // ConPty Dimensions
                             pipeInPtySide,   // ConPty Input
                             pipeOutPtySide,  // ConPty Output
                             0,               // ConPty Flags
                             &conpty->hpc     // ConPty Reference
    );

    // Note: We can close the handles to the PTY-end of the pipes here
    // because the handles are dup'ed into the ConHost and will be released
    // when the ConPTY is destroyed.
    if (INVALID_HANDLE_VALUE != pipeInPtySide) CloseHandle(pipeInPtySide);
    if (INVALID_HANDLE_VALUE != pipeOutPtySide) CloseHandle(pipeOutPtySide);

    if (S_OK == hr) {
      mtx.lock();
      conpty->fd = counter++;
      Storage::conptysMap.insert(pair<int, CONPTY*>(conpty->fd, conpty));
      mtx.unlock();
    }
  }
  return S_OK == hr ? conpty->fd : -1;
}

void closeConPty(int fd) {
  CONPTY* conpty = Storage::conptysMap[fd];
  if (!conpty) return;
  // Close ConPTY - this will terminate client process if running
  ClosePseudoConsole(conpty->hpc);

  // Clean-up the pipes
  if (INVALID_HANDLE_VALUE != conpty->pipeOutTerminalSide)
    CloseHandle(conpty->pipeOutTerminalSide);
  if (INVALID_HANDLE_VALUE != conpty->pipeInTerminalSide)
    CloseHandle(conpty->pipeInTerminalSide);

  conpty->closed = true;
  mtx.lock();
  Storage::conptysMap.erase(fd);
  mtx.unlock();
}

CONPTY* getConPty(int fd) { return Storage::conptysMap[fd]; }

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
  if (!conpty) return;
  COORD size{SHORT(columns), SHORT(lines)};
  ResizePseudoConsole(conpty->hpc, size);
}

bool startSubProcess(int fd, LPWSTR command) {
  CONPTY* conpty = getConPty(fd);
  HRESULT hr{E_UNEXPECTED};
  if (fd > 0 && conpty) {
    // Initialize the necessary startup info struct
    STARTUPINFOEX* startupInfo = new STARTUPINFOEX{};
    size_t attrListSize{};

    startupInfo->StartupInfo.cb = sizeof(STARTUPINFOEX);
    startupInfo->StartupInfo.dwFlags =
        STARTF_USESHOWWINDOW | STARTF_USESTDHANDLES;
    startupInfo->StartupInfo.wShowWindow = SW_HIDE;

    // Get the size of the thread attribute list.
    InitializeProcThreadAttributeList(nullptr, 1, 0, &attrListSize);

    // Allocate a thread attribute list of the correct size
    startupInfo->lpAttributeList =
        reinterpret_cast<LPPROC_THREAD_ATTRIBUTE_LIST>(malloc(attrListSize));

    // Initialize the necessary startup info struct
    // Initialize thread attribute list
    if (startupInfo->lpAttributeList &&
        InitializeProcThreadAttributeList(startupInfo->lpAttributeList, 1, 0,
                                          &attrListSize)) {
      // Initialize the necessary startup info struct
      // Set Pseudo Console attribute
      hr = UpdateProcThreadAttribute(startupInfo->lpAttributeList, 0,
                                     PROC_THREAD_ATTRIBUTE_PSEUDOCONSOLE,
                                     conpty->hpc, sizeof(HPCON), nullptr,
                                     nullptr)
               ? S_OK
               : HRESULT_FROM_WIN32(GetLastError());
    }
    if (S_OK == hr) {
      hr = CreateProcess(nullptr,  // No module name - use Command Line
                         command,  // Command Line
                         nullptr,  // Process handle not inheritable
                         nullptr,  // Thread handle not inheritable
                         true,     // Inherit handles
                         EXTENDED_STARTUPINFO_PRESENT,  // Creation flags
                         nullptr,  // Use parent's environment block
                         nullptr,  // Use parent's starting directory
                         &startupInfo->StartupInfo,  // Pointer to STARTUPINFO
                         &conpty->pi)
               ? S_OK
               : GetLastError();

    } else
      return false;

    // Cleanup attribute list
    // DeleteProcThreadAttributeList(startupInfo->lpAttributeList);
    // free(startupInfo->lpAttributeList);
  }
  WaitForSingleObject(conpty->pi.hThread, INFINITE);
  return S_OK == hr;
}
