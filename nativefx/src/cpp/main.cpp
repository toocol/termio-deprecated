#include <QApplication>

#include "terminalemulator.h"

int main(int argc, char *argv[]) {
  QApplication a(argc, argv);
  TConsole::TerminalEmulator emulator;
  emulator.show();
  emulator.initialize();
  return a.exec();
}
