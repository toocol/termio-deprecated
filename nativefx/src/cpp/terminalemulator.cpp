#include "terminalemulator.h"

#include "vt102emulation.h"

using namespace TConsole;

TerminalEmulator::TerminalEmulator(QWidget *parent) : QWidget(parent) {
  resize(1280, 800);
  _emulation = new Vt102Emulation();
  _emulation->setParent(this);
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

void TerminalEmulator::bindViewToEmulation(TerminalView *terminalView) {
  if (_emulation != nullptr) {
    // connect emulation - view signals and slots
    connect(terminalView, &TerminalView::keyPressedSignal, _emulation,
            &Emulation::sendKeyEvent);
    connect(terminalView, SIGNAL(mouseSignal(int, int, int, int)), _emulation,
            SLOT(sendMouseEvent(int, int, int, int)));
    connect(terminalView, SIGNAL(sendStringToEmu(const char *)), _emulation,
            SLOT(sendString(const char *)));

    // allow emulation to notify view when the foreground process
    // indicates whether or not it is interested in mouse signals
    connect(_emulation, SIGNAL(programUsesMouseChanged(bool)), terminalView,
            SLOT(setUsesMouse(bool)));

    terminalView->setUsesMouse(_emulation->programUsesMouse());

    connect(_emulation, SIGNAL(programBracketedPasteModeChanged(bool)),
            terminalView, SLOT(setBracketedPasteMode(bool)));

    terminalView->setBracketedPasteMode(
        _emulation->programBracketedPasteMode());

    terminalView->setScreenWindow(_emulation->createWindow());
  }
}
