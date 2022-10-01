#ifndef __PIPEIO_H_
#define __PIPEIO_H_

#include <functional>
#include "conptytypes.h"

/**
 * Starting a thread to listen the read pipe to get data from conpty.
 */
__declspec(dllexport) void startReadListener(int, std::function<void(char*)>);
/**
 * Writing data to ConPty.
 */
__declspec(dllexport) void writeData(int, const char*);

#endif
