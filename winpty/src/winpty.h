#pragma once

#include <Windows.h>
#include <io.h>
#include <process.h>
#include <vector>

using namespace std;

struct CONPTY {
  int fd = 0;
  int winid = 0;
  HPCON hpc{INVALID_HANDLE_VALUE};
  HANDLE hPipeIn{INVALID_HANDLE_VALUE};
  HANDLE hPipeOut{INVALID_HANDLE_VALUE};
  HANDLE ptyPipeIn{INVALID_HANDLE_VALUE};
  HANDLE ptyPipeOut{INVALID_HANDLE_VALUE};
};

static const vector<CONPTY> conptys;

int openConPty();
