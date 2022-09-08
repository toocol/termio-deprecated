#include "emulation.h"

#define BULK_TIMEOUT1 10
#define BULK_TIMEOUT2 40

using namespace TConsole;

Emulation::Emulation(QWidget *parent) : QObject(parent) {
  // create screens with a default size
  _screen[0] = new Screen(40, 80);
  _screen[1] = new Screen(40, 80);
  _currentScreen = _screen[0];
  _useMouse = true;

  QObject::connect(&_bulkTimer1, SIGNAL(timeout()), this, SLOT(showBulk()));
  QObject::connect(&_bulkTimer2, SIGNAL(timeout()), this, SLOT(showBulk()));

  // listen for mouse status changes
  connect(this, SIGNAL(programUsesMouseChanged(bool)),
          SLOT(usesMouseChanged(bool)));
  connect(this, SIGNAL(programBracketedPasteModeChanged(bool)),
          SLOT(bracketedPasteModeChanged(bool)));

  connect(
      this, &Emulation::cursorChanged, this,
      [this](CursorShape cursorShape, bool blinkingCursorEnabled) {
        emit titleChanged(
            50,
            QString(QLatin1String("CursorShape=%1;BlinkingCursorEnabled=%2"))
                .arg(static_cast<int>(cursorShape))
                .arg(blinkingCursorEnabled));
      });
}

ScreenWindow *Emulation::createWindow() {
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

bool Emulation::programUseMouse() { return _useMouse; }

void Emulation::setUseMouse(bool on) { _useMouse = true; }

bool Emulation::programBracketedPasteMode() { return _bracketedPasteMode; }

void Emulation::bufferedUpdate() {
  _bulkTimer1.setSingleShot(true);
  _bulkTimer1.start(BULK_TIMEOUT1);
  if (!_bulkTimer2.isActive()) {
    _bulkTimer2.setSingleShot(true);
    _bulkTimer2.start(BULK_TIMEOUT2);
  }
}

void Emulation::showBulk() {
  _bulkTimer1.stop();
  _bulkTimer2.stop();

  emit outputChanged();

  _currentScreen->resetScrolledLines();
  _currentScreen->resetDroppedLines();
}

void Emulation::usesMouseChanged(bool usesMouse) { _useMouse = usesMouse; }

void Emulation::bracketedPasteModeChanged(bool bracketedPasteMode) {
  _bracketedPasteMode = bracketedPasteMode;
}

void Emulation::setBracketedPasteMode(bool on) { _bracketedPasteMode = on; }

bool Emulation::programUsesMouse() const { return _useMouse; }

bool Emulation::programBracketedPasteMode() const {
  return _bracketedPasteMode;
}
