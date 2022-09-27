#ifndef _CONPTY_MAP_
#define _CONPTY_MAP_

#include <Windows.h>
#include <map>

#ifndef _WINCONTYPES_
typedef VOID* HPCON;
#endif

namespace _winconpty_ {
struct CONPTY {
  HPCON hpc{INVALID_HANDLE_VALUE};
  HANDLE pipeInTerminalSide{INVALID_HANDLE_VALUE};
  HANDLE pipeOutTerminalSide{INVALID_HANDLE_VALUE};
  HANDLE pipeInPtySide{INVALID_HANDLE_VALUE};
  HANDLE pipeOutPtySide{INVALID_HANDLE_VALUE};

  PROCESS_INFORMATION pi{};

  int fd = 0;
};

static std::map<int, CONPTY*> conptysMap;
}  // namespace _winconpty_

#endif
