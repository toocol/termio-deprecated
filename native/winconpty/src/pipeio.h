#ifndef __PIPEIO_H_
#define __PIPEIO_H_

#include <string>
#include "conptytypes.h"

/**
 * Detach a thread to listen the read pipe to get data from conpty.
 * This method can only
 */
__declspec(dllexport) void startReadPipeListener(int);
/**
 * Detach a thread to listen the write pipe to send data to conpty.
 */
__declspec(dllexport) void startWritePipeListener(int);
/**
 * Writing data to conpty.
 */
__declspec(dllexport) void writeData(int, std::wstring);
/**
 * Reading data from conpty.
 */
__declspec(dllexport) std::wstring readData(int);

#endif
