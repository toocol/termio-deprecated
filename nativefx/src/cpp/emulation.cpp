#include "emulation.h"

#define BULK_TIMEOUT1 10
#define BULK_TIMEOUT2 40

using namespace TConsole;

Emulation::Emulation()
    : _currentScreen(nullptr),
      _codec(nullptr),
      _decoder(nullptr),
      _keyTranslator(nullptr),
      _useMouse(false),
      _bracketedPasteMode(false) {
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

Emulation::~Emulation() {
  QListIterator<ScreenWindow *> windowIter(_windows);

  while (windowIter.hasNext()) {
    delete windowIter.next();
  }

  delete _screen[0];
  delete _screen[1];
  delete _decoder;
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

QSize Emulation::imageSize() const {
  return {_currentScreen->getColumns(), _currentScreen->getLines()};
}

int Emulation::lineCount() const {
  // sum number of lines currently on _screen plus number of lines in history
  return _currentScreen->getLines() + _currentScreen->getHistLines();
}

void Emulation::setHistory(const HistoryType &t) {
  _screen[0]->setScroll(t);

  showBulk();
}

const HistoryType &Emulation::history() const {
  return _screen[0]->getScroll();
}

void Emulation::clearHistory() {
  _screen[0]->setScroll(_screen[0]->getScroll(), false);
}

void Emulation::writeToStream(TerminalCharacterDecoder *_decoder, int startLine,
                              int endLine) {
  _currentScreen->writeLinesToStream(_decoder, startLine, endLine);
}

void Emulation::setCodec(const QTextCodec *qtc) {
  if (qtc)
    _codec = qtc;
  else
    setCodec(LocaleCodec);

  delete _decoder;
  _decoder = _codec->makeDecoder();

  emit useUtf8Request(utf8());
}

char Emulation::eraseChar() const { return '\b'; }

void Emulation::setKeyBindings(const QString &name) {
  _keyTranslator = KeyboardTranslatorManager::instance()->findTranslator(name);
  if (!_keyTranslator) {
    _keyTranslator = KeyboardTranslatorManager::instance()->defaultTranslator();
  }
}

QString Emulation::keyBindings() const { return _keyTranslator->name(); }

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

void Emulation::receiveChar(wchar_t c) {
  c &= 0xff;
  switch (c) {
    case '\b':
      _currentScreen->backspace();
      break;
    case '\t':
      _currentScreen->tab();
      break;
    case '\n':
      _currentScreen->newLine();
      break;
    case '\r':
      _currentScreen->toStartOfLine();
      break;
    case 0x07:
      emit stateSet(NOTIFYBELL);
      break;
    default:
      _currentScreen->displayCharacter(c);
      break;
  };
}

void Emulation::setScreen(int index) {
  Screen *old = _currentScreen;
  _currentScreen = _screen[index & 1];
  if (_currentScreen != old) {
    // tell all windows onto this emulation to switch to the newly active screen
    for (ScreenWindow *window : qAsConst(_windows))
      window->setScreen(_currentScreen);
  }
}

void Emulation::setCodec(EmulationCodec codec) {
  if (codec == Utf8Codec)
    setCodec(QTextCodec::codecForName("utf8"));
  else if (codec == LocaleCodec)
    setCodec(QTextCodec::codecForLocale());
}

void Emulation::setImageSize(int lines, int columns) {
  if ((lines < 1) || (columns < 1)) return;

  QSize screenSize[2] = {
      QSize(_screen[0]->getColumns(), _screen[0]->getLines()),
      QSize(_screen[1]->getColumns(), _screen[1]->getLines())};
  QSize newSize(columns, lines);

  if (newSize == screenSize[0] && newSize == screenSize[1]) return;

  _screen[0]->resizeImage(lines, columns);
  _screen[1]->resizeImage(lines, columns);

  emit imageSizeChanged(lines, columns);

  bufferedUpdate();
}

void Emulation::sendKeyEvent(QKeyEvent *ev, bool fromPaste) {
  emit stateSet(NOTIFYNORMAL);

  if (!ev->text().isEmpty()) {  // A block of text
    // Note that the text is proper unicode.
    // We should do a conversion here
    emit sendData(ev->text().toUtf8().constData(), ev->text().length());
  }
}

void Emulation::sendMouseEvent(int buttons, int column, int line,
                               int eventType) {
  // default implementation does nothing
}

void Emulation::sendString(const char *, int) {
  // default implementation does nothing
}

void Emulation::receiveData(const char *text, int length) {
  emit stateSet(NOTIFYACTIVITY);

  bufferedUpdate();

  /* XXX: the following code involves encoding & decoding of "UTF-16
   * surrogate pairs", which does not work with characters higher than
   * U+10FFFF
   * https://unicodebook.readthedocs.io/unicode_encodings.html#surrogates
   */
  QString utf16Text = _decoder->toUnicode(text, length);
  std::wstring unicodeText = utf16Text.toStdWString();

  // send characters to terminal emulator
  for (size_t i = 0; i < unicodeText.length(); i++) receiveChar(unicodeText[i]);

  // look for z-modem indicator
  //-- someone who understands more about z-modems that I do may be able to move
  // this check into the above for loop?
  for (int i = 0; i < length; i++) {
    if (text[i] == '\030') {
      if ((length - i - 1 > 3) && (strncmp(text + i + 1, "B00", 3) == 0))
        emit zmodemDetected();
    }
  }
}
