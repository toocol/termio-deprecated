#ifndef _CONPTY_MAP_
#define _CONPTY_MAP_

#include <Windows.h>
#include <map>

#ifndef _WINCONTYPES_
typedef VOID* HPCON;
#endif

namespace _winconpty_ {

const int bufferSize = 2 << 18;

struct CONPTY {
  HPCON hpc{INVALID_HANDLE_VALUE};
  HANDLE pipeInTerminalSide{INVALID_HANDLE_VALUE};
  HANDLE pipeOutTerminalSide{INVALID_HANDLE_VALUE};

  PROCESS_INFORMATION pi{};

  int fd = 0;
  bool closed = false;
};

class Storage {
 public:
  static std::map<int, CONPTY*> conptysMap;
};
}  // namespace _winconpty_

#endif
