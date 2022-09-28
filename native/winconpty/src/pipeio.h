#ifndef __PIPEIO_H_
#define __PIPEIO_H_

#include "conptytypes.h"

/**
 * Starting a thread to listen the read pipe to get data from conpty.
 */
__declspec(dllexport) void startReadPipeListener(int);
/**
 * Starting a thread to listen the write pipe to send data to conpty.
 */
__declspec(dllexport) void startWritePipeListener(int);
/**
 * Writing data to ConPty.
 */
__declspec(dllexport) void writeData(int, char*);
/**
 * Reading data from ConPty.
 */
__declspec(dllexport) char* readData(int);

#endif
