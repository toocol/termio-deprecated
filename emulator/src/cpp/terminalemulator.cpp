#include "terminalemulator.h"

#include <QApplication>

#include "vt102emulation.h"

#ifdef Q_OS_MACOS
// Qt does not support fontconfig on macOS, so we need to use a "real" font
// name.
#define DEFAULT_FONT_FAMILY "Menlo"
#else
#define DEFAULT_FONT_FAMILY "Monospace"
#endif

#define STEP_ZOOM 1

using namespace TConsole;

TerminalEmulator::TerminalEmulator(QWidget *parent)
    : QWidget(parent), image(nullptr) {}

TerminalEmulator::~TerminalEmulator() {}

void TerminalEmulator::initialize() {
  _mainLayout = new QVBoxLayout();
  setLayout(_mainLayout);

  createEmulation();
  createTerminalView();

  UrlFilter *urlFilter = new UrlFilter();
  connect(urlFilter, &UrlFilter::activated, this,
          &TerminalEmulator::urlActivated);
  _terminalView->filterChain()->addFilter(urlFilter);

  _mainLayout->addWidget(_terminalView);
  _terminalView->resize(this->size());

  this->setFocus(Qt::OtherFocusReason);
  this->setFocusPolicy(Qt::WheelFocus);
  this->setFocusProxy(_terminalView);

  connect(_terminalView, SIGNAL(copyAvailable(bool)), this,
          SLOT(selectionChanged(bool)));
  connect(_terminalView, SIGNAL(termGetFocus()), this, SIGNAL(termGetFocus()));
  connect(_terminalView, SIGNAL(termLostFocus()), this,
          SIGNAL(termLostFocus()));
  connect(_terminalView, &TerminalView::keyPressedSignal, this,
          [this](QKeyEvent *e, bool) { Q_EMIT termKeyPressed(e); });
  //  _terminalView->setSize(80, 40);

  QFont font = QApplication::font();
  font.setFamily(QLatin1String(DEFAULT_FONT_FAMILY));
  font.setPointSize(10);
  font.setStyleHint(QFont::TypeWriter);
  setTerminalFont(font);

  _terminalView->setScrollBarPosition(ScrollBarPosition::SCROLL_BAR_RIGHT);
  _terminalView->setKeyboardCursorShape(KeyboardCursorShape::BLOCK_CURSOR);
  bindViewToEmulation(_terminalView);
}

void TerminalEmulator::createEmulation() {
  _emulation = new Vt102Emulation();
  _emulation->setParent(this);
  _emulation->setCodec(QTextCodec::codecForName("UTF-8"));
  _emulation->setHistory(HistoryTypeBuffer(50000));
  _emulation->setKeyBindings(QString());

  connect(_emulation, SIGNAL(imageResizeRequest(QSize)), this,
          SLOT(onEmulationSizeChange(QSize)));
  connect(_emulation, SIGNAL(imageSizeChanged(int, int)), this,
          SLOT(onViewSizeChange(int, int)));
  connect(_emulation, &Vt102Emulation::cursorChanged, this,
          &TerminalEmulator::onCursorChanged);
}

void TerminalEmulator::createTerminalView() {
  _terminalView = new TerminalView(this);
  _terminalView->setBellMode(BellMode::NOTIFY_BELL);
  _terminalView->setTerminalSizeHint(true);
  _terminalView->setTripleClickMode(TripleClickMode::SELECT_WHOLE_LINE);
  _terminalView->setTerminalSizeStartup(true);
  _terminalView->setRandomSeed(3L);
}

void TerminalEmulator::bindViewToEmulation(TerminalView *terminalView) {
  if (_emulation != nullptr) {
    terminalView->setUsesMouse(_emulation->programUseMouse());
    terminalView->setBracketedPasteMode(
        _emulation->programBracketedPasteMode());

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

    // connect view signals and slots
    connect(terminalView, SIGNAL(changedContentSizeSignal(int, int)), this,
            SLOT(onViewSizeChange(int, int)));

    connect(terminalView, SIGNAL(destroyed(QObject *)), this,
            SLOT(viewDestroyed(QObject *)));
    // slot for close
    connect(this, SIGNAL(finished()), terminalView, SLOT(close()));
  }
}

QSize TerminalEmulator::size() { return _emulation->imageSize(); }

void TerminalEmulator::setSize(const QSize &size) {
  if ((size.width() <= 1) || (size.height() <= 1)) {
    return;
  }
  _terminalView->setSize(size.width(), size.height());
}

void TerminalEmulator::setCursorShape(KeyboardCursorShape shape) {
  _terminalView->setKeyboardCursorShape(shape);
}

void TerminalEmulator::setBlinkingCursor(bool blink) {
  _terminalView->setBlinkingCursor(blink);
}

void TerminalEmulator::setTerminalFont(const QFont &font) {
  _terminalView->setVTFont(font);
}

void TerminalEmulator::sendText(QString text) { _emulation->sendText(text); }

void TerminalEmulator::clear() {
  _emulation->reset();
  _emulation->clearHistory();
}

void TerminalEmulator::requestRedrawImage(QImage *image) {
  this->image = image;
}

bool TerminalEmulator::eventFilter(QObject *obj, QEvent *ev) {
  if (ev->type() == QEvent::Paint) {
    _terminalView->update();
  }
  if (ev->type() == QEvent::UpdateRequest) {
    if (image != nullptr) {
      QPainter painter(image);
      _terminalView->render(&painter);
      painter.end();
    }
    nativeRedrawCallback();
  }
  return QWidget::eventFilter(obj, ev);
}

void TerminalEmulator::setBackgroundColor(const QColor &color) {
  _terminalView->setBackgroundColor(color);
}

void TerminalEmulator::setForegroundColor(const QColor &color) {
  _terminalView->setForegroundColor(color);
}

void TerminalEmulator::updateTerminalSize() {
  int minLines = -1;
  int minColumns = -1;

  // minimum number of lines and columns that views require for
  // their size to be taken into consideration ( to avoid problems
  // with new view widgets which haven't yet been set to their correct size )
  const int VIEW_LINES_THRESHOLD = 2;
  const int VIEW_COLUMNS_THRESHOLD = 2;

  // select largest number of lines and columns that will fit in all visible
  // views
  TerminalView *view = _terminalView;
  if (view->isHidden() == false && view->lines() >= VIEW_LINES_THRESHOLD &&
      view->columns() >= VIEW_COLUMNS_THRESHOLD) {
    minLines = (minLines == -1) ? view->lines() : qMin(minLines, view->lines());
    minColumns = (minColumns == -1) ? view->columns()
                                    : qMin(minColumns, view->columns());
  }

  // backend emulation must have a _terminal of at least 1 column x 1 line in
  // size
  if (minLines > 0 && minColumns > 0) {
    _emulation->setImageSize(minLines, minColumns);
  }
}

void TerminalEmulator::setNativeRedrawCallback(
    const std::function<void()> &newNativeRedrawCallback) {
  nativeRedrawCallback = newNativeRedrawCallback;
}

void TerminalEmulator::requestFocus() { _terminalView->setFocus(); }

void TerminalEmulator::selectionChanged(bool textSelected) {
  emit copyAvailable(textSelected);
}

void TerminalEmulator::onViewSizeChange(int height, int width) {
  updateTerminalSize();
}

void TerminalEmulator::onEmulationSizeChange(QSize size) { setSize(size); }

void TerminalEmulator::onCursorChanged(KeyboardCursorShape cursorShape,
                                       bool blinkingCursorEnabled) {
  // TODO: A switch to enable/disable DECSCUSR?
  setCursorShape(cursorShape);
  setBlinkingCursor(blinkingCursorEnabled);
}

void TerminalEmulator::viewDestroyed(QObject *obj) {
  TerminalView *view = (TerminalView *)obj;
  disconnect(view, nullptr, this, nullptr);

  if (_emulation != nullptr) {
    // disconnect
    //  - key presses signals from widget
    //  - mouse activity signals from widget
    //  - string sending signals from widget
    //
    //  ... and any other signals connected in addView()
    disconnect(view, nullptr, _emulation, nullptr);

    // disconnect state change signals emitted by emulation
    disconnect(_emulation, nullptr, view, nullptr);
  }
}
