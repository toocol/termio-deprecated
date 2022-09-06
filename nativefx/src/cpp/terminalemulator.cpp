#include "terminalemulator.h"

using namespace TConsole;

TerminalEmulator::TerminalEmulator(QWidget *parent) : QWidget(parent) {
  resize(1280, 800);

  // create screens with a default size
  _screen[0] = new Screen(40, 80);
  _screen[1] = new Screen(40, 80);
  _currentScreen = _screen[0];
  _useMouse = true;
}

TerminalEmulator::~TerminalEmulator() { delete _terminalView; }

void TerminalEmulator::initialize() {
  _mainLayout = new QVBoxLayout();
  setLayout(_mainLayout);

  _terminalView = new TerminalView(this);
  _terminalView->setScreenWindow(createWindow());
  _terminalView->setBellMode(BellMode::NOTIFY_BELL);
  _terminalView->setTerminalSizeHint(true);
  _terminalView->setTripleClickMode(TripleClickMode::SELECT_WHOLE_LINE);
  _terminalView->setTerminalSizeStartup(true);
  _terminalView->setRandomSeed(3L);
  _terminalView->setScrollBarPosition(ScrollBarPosition::NO_SCROLL_BAR);
  _terminalView->setCursorShape(CursorShape::BLOCK_CURSOR);
  _terminalView->setUsesMouse(programUseMouse());
  _terminalView->setBracketedPasteMode(programBracketedPasteMode());

  this->setFocus(Qt::OtherFocusReason);
  this->setFocusPolicy(Qt::WheelFocus);
  this->setFocusProxy(_terminalView);

  _mainLayout->addWidget(_terminalView);
  _terminalView->resize(this->size());
}

ScreenWindow *TerminalEmulator::createWindow() {
  ScreenWindow *window = new ScreenWindow();
  window->setScreen(_currentScreen);
  _windows << window;

  connect(window, SIGNAL(selectionChanged()), this, SLOT(bufferedUpdate()));

  connect(this, SIGNAL(outputChanged()), window, SLOT(notifyOutputChanged()));

  //    connect(this, &Emulation::handleCommandFromKeyboard,
  //            window, &ScreenWindow::handleCommandFromKeyboard);
  //    connect(this, &Emulation::outputFromKeypressEvent,
  //            window, &ScreenWindow::scrollToEnd);

  return window;
}

bool TerminalEmulator::programUseMouse() { return _useMouse; }

void TerminalEmulator::setUseMouse(bool on) { _useMouse = true; }

bool TerminalEmulator::programBracketedPasteMode() {
  return _bracketedPasteMode;
}

void TerminalEmulator::setBracketedPasteMode(bool on) {
  _bracketedPasteMode = on;
}

void TerminalEmulator::paintEvent(QPaintEvent *e) {
  qDebug() << "Emulator trigger";
}
