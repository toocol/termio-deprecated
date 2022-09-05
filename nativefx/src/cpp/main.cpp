#include <QApplication>

#include "terminalemulator.h"

int main(int argc, char *argv[]) {
  QApplication a(argc, argv);
  TConsole::TerminalEmulator w;
  w.show();
  w.draw();
  return a.exec();
}
