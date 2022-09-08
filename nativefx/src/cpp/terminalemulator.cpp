#include "terminalemulator.h"

using namespace TConsole;

TerminalEmulator::TerminalEmulator(QWidget *parent) : QWidget(parent) {
  resize(1280, 800);
  _emulation = new Emulation(this);
}

TerminalEmulator::~TerminalEmulator() {}

void TerminalEmulator::initialize() {
  _mainLayout = new QVBoxLayout();
  setLayout(_mainLayout);

  createTerminalView();

  _mainLayout->addWidget(_terminalView);
  _terminalView->resize(this->size());

  this->setFocus(Qt::OtherFocusReason);
  this->setFocusPolicy(Qt::WheelFocus);
  this->setFocusProxy(_terminalView);
}

void TerminalEmulator::createTerminalView() {
  _terminalView = new TerminalView(this);
  _terminalView->setScreenWindow(_emulation->createWindow());
  _terminalView->setBellMode(BellMode::NOTIFY_BELL);
  _terminalView->setTerminalSizeHint(true);
  _terminalView->setTripleClickMode(TripleClickMode::SELECT_WHOLE_LINE);
  _terminalView->setTerminalSizeStartup(true);
  _terminalView->setRandomSeed(3L);
  _terminalView->setScrollBarPosition(ScrollBarPosition::NO_SCROLL_BAR);
  _terminalView->setCursorShape(CursorShape::BLOCK_CURSOR);

  _terminalView->setUsesMouse(_emulation->programUseMouse());
  _terminalView->setBracketedPasteMode(_emulation->programBracketedPasteMode());
}
