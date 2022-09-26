#include "conpty.h"
#include <Windows.h>
#include <io.h>
#include <process.h>

using namespace std;

HRESULT CreatePseudoConsoleAndPipes(HPCON*, HANDLE*, HANDLE*);
HRESULT InitializeStartupInfoAttachedToPseudoConsole(STARTUPINFOEX*, HPCON);
void __cdecl PipeListener(LPVOID);

void fdOfConPTY();
void pingByConPTY();

int main() {
  // fdOfConPTY();
  pingByConPTY();
  return 0;
}

void fdOfConPTY() {
  HANDLE hOut, hIn;
  HANDLE outPipeOurSide, inPipeOurSide;
  HANDLE outPipePseudoConsoleSide, inPipePseudoConsoleSide;
  HPCON hPC = 0;

  // Create the in/out pipes:
  CreatePipe(&inPipePseudoConsoleSide, &inPipeOurSide, NULL, 0);
  CreatePipe(&outPipeOurSide, &outPipePseudoConsoleSide, NULL, 0);

  // Create the Pseudo Console, using the pipes
  HRESULT hr = CreatePseudoConsole({80, 32}, inPipePseudoConsoleSide,
                                   outPipePseudoConsoleSide, 0, &hPC);

  ClosePseudoConsole(hPC);
}

void pingByConPTY() {
  LPSTR szCommand = (LPSTR) "ping localhost";
  HRESULT hr{E_UNEXPECTED};
  HANDLE hConsole = {GetStdHandle(STD_OUTPUT_HANDLE)};

  // Enable Console VT Processing
  DWORD consoleMode{};
  GetConsoleMode(hConsole, &consoleMode);
  hr =
      SetConsoleMode(hConsole, consoleMode | ENABLE_VIRTUAL_TERMINAL_PROCESSING)
          ? S_OK
          : GetLastError();
  if (S_OK == hr) {
    HRESULT hr{E_UNEXPECTED};
    // hpc was the handle of ConPTY
    HPCON hpc{INVALID_HANDLE_VALUE};

    // Create the Pseudo Console and pipes to it
    // In this project:
    //  1. ConPTY's data was read from hPipeIn
    //  2. Send data to ConPTY via hPipeOut
    HANDLE hPipeIn{INVALID_HANDLE_VALUE};
    HANDLE hPipeOut{INVALID_HANDLE_VALUE};

    hr = CreatePseudoConsoleAndPipes(&hpc, &hPipeIn, &hPipeOut);
    if (S_OK == hr) {
      //  Create & start thread to listen to the incoming pipe
      //  Note: Using CRT-safe _beginthread() rather than CreateThread()
      HANDLE hPipeListenerThread{
          reinterpret_cast<HANDLE>(_beginthread(PipeListener, 0, hPipeIn))};

      // Initialize the necessary startup info struct
      STARTUPINFOEX startupInfo{};
      if (S_OK ==
          InitializeStartupInfoAttachedToPseudoConsole(&startupInfo, hpc)) {
        // Launch ping to emit some text back via the pipe
        PROCESS_INFORMATION piClient{};
        hr = CreateProcess(NULL,         // No module name - use Command Line
                           szCommand,  // Command Line
                           NULL,       // Process handle not inheritable
                           NULL,       // Thread handle not inheritable
                           FALSE,      // Inherit handles
                           EXTENDED_STARTUPINFO_PRESENT,  // Creation flags
                           NULL,  // Use parent's environment block
                           NULL,  // Use parent's starting directory
                           &startupInfo.StartupInfo,  // Pointer to STARTUPINFO
                           &piClient)
                 ? S_OK
                 : GetLastError();
        
        if (S_OK == hr) {
          // Wait up to 10s for ping process to complete
          WaitForSingleObject(piClient.hThread, 10 * 1000);

          // Allow listening thread to catch-up with final output!
          Sleep(500);
        }

        // --- CLOSEDOWN ---

        // Cleanup attribute list
        DeleteProcThreadAttributeList(startupInfo.lpAttributeList);
        free(startupInfo.lpAttributeList);

        // Now safe to clean-up client app's process-info & thread
        CloseHandle(piClient.hThread);
        CloseHandle(piClient.hProcess);

        // Close ConPTY - this will terminate client process if running
        ClosePseudoConsole(hpc);

        // Clean-up the pipes
        if (INVALID_HANDLE_VALUE != hPipeOut) CloseHandle(hPipeOut);
        if (INVALID_HANDLE_VALUE != hPipeIn) CloseHandle(hPipeIn);
      }
    }
  }
}

HRESULT CreatePseudoConsoleAndPipes(HPCON* hPC, HANDLE* pipeIn,
                                    HANDLE* pipeOut) {
  HRESULT hr{E_UNEXPECTED};
  HANDLE ptyPipeIn{INVALID_HANDLE_VALUE};
  HANDLE ptyPipeOut{INVALID_HANDLE_VALUE};

  // Create the pipes to which the ConPTY will connect.
  // ConPTY's pipe is connecting to Console's pipe out:
  //    Write data to pipeOut    then Read data from ptyPipeIn
  //    Write data to ptyPipeOut then read data from pipeIn
  // In this sample project, ConPTY's pipe out was ConHost's pipe in
  if (CreatePipe(&ptyPipeIn, pipeOut, NULL, 0) &&
      CreatePipe(pipeIn, &ptyPipeOut, NULL, 0)) {
    COORD consoleSize{};
    CONSOLE_SCREEN_BUFFER_INFO csbi{};
    HANDLE hConsole{GetStdHandle(STD_OUTPUT_HANDLE)};
    if (GetConsoleScreenBufferInfo(hConsole, &csbi)) {
      cout << "Get console size success." << endl;
      consoleSize.X = csbi.srWindow.Right - csbi.srWindow.Left + 1;
      consoleSize.Y = csbi.srWindow.Bottom - csbi.srWindow.Top + 1;
    }

    // Create the Pseudo Console of the required size, attached to the PTY-end
    // of the pipes.
    // consoleSize in this sample project is bind to ConHost's real size
    // In third party Terminal, it's should be your Terminal's size
    hr = CreatePseudoConsole(consoleSize,  // ConPty Dimensions
                             ptyPipeIn,    // ConPty Input
                             ptyPipeOut,   // ConPty Output
                             0,            // ConPty Flags
                             hPC           // ConPty Reference
    );

    // Note: We can close the handles to the PTY-end of the pipes here
    // because the handles are dup'ed into the ConHost and will be released
    // when the ConPTY is destroyed.
    if (INVALID_HANDLE_VALUE != ptyPipeIn) CloseHandle(ptyPipeIn);
    if (INVALID_HANDLE_VALUE != ptyPipeOut) CloseHandle(ptyPipeOut);
  }

  return hr;
}

HRESULT InitializeStartupInfoAttachedToPseudoConsole(
    STARTUPINFOEX* pStartupInfo, HPCON hPC) {
  HRESULT hr{E_UNEXPECTED};
  if (pStartupInfo) {
    size_t attrListSize{};

    pStartupInfo->StartupInfo.cb = sizeof(STARTUPINFOEX);

    // Get the size of the thread attribute list.
    InitializeProcThreadAttributeList(NULL, 1, 0, &attrListSize);

    // Allocate a thread attribute list of the correct size
    pStartupInfo->lpAttributeList =
        reinterpret_cast<LPPROC_THREAD_ATTRIBUTE_LIST>(malloc(attrListSize));

    // Initialize thread attribute list
    if (pStartupInfo->lpAttributeList &&
        InitializeProcThreadAttributeList(pStartupInfo->lpAttributeList, 1, 0,
                                          &attrListSize)) {
      // Set Pseudo Console attribute
      // This means major process read bytes from hpc(ConPTY)
      hr = UpdateProcThreadAttribute(pStartupInfo->lpAttributeList, 0,
                                     PROC_THREAD_ATTRIBUTE_PSEUDOCONSOLE, hPC,
                                     sizeof(HPCON), NULL, NULL)
               ? S_OK
               : HRESULT_FROM_WIN32(GetLastError());
    } else {
      hr = HRESULT_FROM_WIN32(GetLastError());
    }
  }

  return hr;
}

void __cdecl PipeListener(LPVOID pipe) {
  HANDLE hPipe{pipe};
  HANDLE hConsole{GetStdHandle(STD_OUTPUT_HANDLE)};

  const DWORD BUFF_SIZE{512};
  char szBuffer[BUFF_SIZE]{};

  DWORD dwBytesWritten{};
  DWORD dwBytesRead{};
  BOOL fRead{FALSE};
  do {
    // Read from the pipe
    // In this project, means read data from ConPTY
    fRead = ReadFile(hPipe, szBuffer, BUFF_SIZE, &dwBytesRead, NULL);

    // Write received text to the Console
    // Note: Write to the Console using WriteFile(hConsole...), not
    // printf()/puts() to prevent partially-read VT sequences from corrupting
    // output
    WriteFile(hConsole, szBuffer, dwBytesRead, &dwBytesWritten, NULL);

  } while (fRead && dwBytesRead >= 0);
}
