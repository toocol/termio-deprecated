#include "terminalemulator.h"

using namespace TConsole;

TerminalEmulator::TerminalEmulator(QWidget *parent) : QWidget(parent) {
  resize(1280, 800);
  terminalView = new TerminalView(parent);
  terminalView->resize(1280, 800);
  terminalView->setBellMode(BellMode::NOTIFY_BELL);
  terminalView->setTerminalSizeHint(true);
  terminalView->setTripleClickMode(TripleClickMode::SELECT_WHOLE_LINE);
  terminalView->setTerminalSizeStartup(true);
  terminalView->setRandomSeed(3L);
}

TerminalEmulator::~TerminalEmulator() { delete terminalView; }

void TerminalEmulator::draw() { terminalView->show(); }

void TerminalEmulator::paintEvent(QPaintEvent *e) {
  qDebug() << "Emulator trigger";
}
