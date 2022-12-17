#include "terminal_emulator.h"

#include <QApplication>
#include <QShortcut>

#ifdef Q_OS_MACOS
// Qt does not support fontconfig on macOS, so we need to use a "real" font
// name.
#define DEFAULT_FONT_FAMILY "Menlo"
#else
#define DEFAULT_FONT_FAMILY "Monospace"
#endif

#define STEP_ZOOM 1

static const int nativeEvtInterval = 1;

using namespace TConsole;

TerminalEmulator::TerminalEmulator(QWidget* parent)
    : QWidget(parent), _primaryImage(nullptr) {
  _transmitSingals = new TransmitSignals(this);
  _nativeEvtTimer = new QTimer(this);
  connect(_nativeEvtTimer, &QTimer::timeout, this,
          &TerminalEmulator::nativeEventCallback);
}

TerminalEmulator::~TerminalEmulator() {}

Session* TerminalEmulator::createSession(QWidget* parent) {
  Session* session = new Session(parent);
  session->setTitle(Session::NameRole, QLatin1String("Ssh Session"));
  session->setAutoClose(true);
  session->setCodec(QTextCodec::codecForName("UTF-8"));
  session->setHistoryType(HistoryTypeBuffer(10000));
  session->setKeyBindings(QString());
  return session;
}

void TerminalEmulator::initialize() {
  _mainLayout = new QVBoxLayout();
  _mainLayout->setContentsMargins(QMargins(0, 0, 0, 0));
  _mainLayout->setSpacing(0);
  setLayout(_mainLayout);

  Session::setTransmitSignals(_transmitSingals);
  SessionGroup::setTransmitSignals(_transmitSingals);

  SessionGroup::changeState(SessionGroup::ONE);

  SessionGroup* group = SessionGroup::getSessionGroup(SessionGroup::ONE_CENTER);
  _terminalView = group->view();

  UrlFilter* urlFilter = new UrlFilter();
  connect(urlFilter, &UrlFilter::activated, this,
          &TerminalEmulator::urlActivated);
  _terminalView->filterChain()->addFilter(urlFilter);

  TabsBar* tabsBar = group->tabsBar();

  _mainLayout->addWidget(tabsBar);
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
          [this](QKeyEvent* e, bool) { Q_EMIT termKeyPressed(e); });
  connect(this, SIGNAL(updateBackground(const QColor&)), group->tabsBar(),
          SLOT(onBackgroundChange(const QColor&)));

  // Setup the signal transmit.
  connect(_transmitSingals, SIGNAL(sigTabRightClick()), this,
          SLOT(onTabRightClick()));
  connect(_transmitSingals, SIGNAL(sigTabButtonMousePressed(QString, int)),
          this, SLOT(onTabButtonMousePress(QString, int)));
  connect(_transmitSingals, SIGNAL(sigTabButtonMouseRelease(QString, int)),
          this, SLOT(onTabButtonMouseRelease(QString, int)));

  QFont font = QApplication::font();
  font.setFamily(QLatin1String(DEFAULT_FONT_FAMILY));
  font.setPointSize(10);
  font.setStyleHint(QFont::TypeWriter);
  setTerminalFont(font);

  _terminalView->setScrollBarPosition(ScrollBarPosition::SCROLL_BAR_RIGHT);
  _terminalView->setKeyboardCursorShape(KeyboardCursorShape::BLOCK_CURSOR);
}

void TerminalEmulator::setCursorShape(KeyboardCursorShape shape) {
  _terminalView->setKeyboardCursorShape(shape);
}

void TerminalEmulator::setBlinkingCursor(bool blink) {
  _terminalView->setBlinkingCursor(blink);
}

void TerminalEmulator::setTerminalFont(const QFont& font) {
  _terminalView->setVTFont(font);
}

void TerminalEmulator::sendText(QString text) {
  //  _emulation->sendText(text);
}

void TerminalEmulator::clear() {
  //  _emulation->reset();
  //  _emulation->clearHistory();
}

void TerminalEmulator::requestRedrawImage(QImage* primaryImage,
                                          QImage* secondaryImage) {
  this->_primaryImage = primaryImage;
  this->_secondaryImage = secondaryImage;
}

bool TerminalEmulator::eventFilter(QObject* obj, QEvent* ev) {
  if (ev->type() == QEvent::Paint) {
    QPaintEvent* pe = static_cast<QPaintEvent*>(ev);
    QWidget::paintEvent(pe);
  }
  if (ev->type() == QEvent::UpdateRequest) {
    _terminalView->nativeCanvas()->lock();
    bool renderered = false;
    int bufferStatus = _terminalView->nativeCanvas()->bufferStatus();
    if (bufferStatus == PRIMARY_BUFFER && _primaryImage != nullptr) {
      QPainter painter(_primaryImage);
      painter.setRenderHints(QPainter::SmoothPixmapTransform |
                             QPainter::Antialiasing |
                             QPainter::TextAntialiasing);
      this->render(&painter);
      painter.end();
      renderered = true;
    } else if (bufferStatus == SECONDARY_BUFFER && _secondaryImage != nullptr) {
      QPainter painter(_secondaryImage);
      painter.setRenderHints(QPainter::SmoothPixmapTransform |
                             QPainter::Antialiasing |
                             QPainter::TextAntialiasing);
      this->render(&painter);
      painter.end();
      renderered = true;
    } else {
      if (bufferStatus != PRIMARY_BUFFER && bufferStatus != SECONDARY_BUFFER) {
        std::cerr << "[eventFilter] Invalid buffer status: " << bufferStatus
                  << std::endl;
        _terminalView->nativeCanvas()->unlock();
        return QWidget::eventFilter(obj, ev);
      }
    }

    if (renderered) nativeRedrawCallback();
    _terminalView->nativeCanvas()->unlock();
    if (renderered) _terminalView->nativeCanvas()->toggleBuffer();
  }
  return QWidget::eventFilter(obj, ev);
}

void TerminalEmulator::setBackgroundColor(const QColor& color) {
  _terminalView->setBackgroundColor(color);
  emit updateBackground(color);
}

void TerminalEmulator::setForegroundColor(const QColor& color) {
  _terminalView->setForegroundColor(color);
}

void TerminalEmulator::setNativeEvtCallback(
    const std::function<void()>& newNativeEvtCallback) {
  nativeEvtCallback = newNativeEvtCallback;
  _nativeEvtTimer->start(nativeEvtInterval);
}

void TerminalEmulator::createSshSession(long sessionId, QString host,
                                        QString user, QString password) {
  qDebug() << "Receive create ssh session event, host = " << host
           << ", user = " << user << ", password = " << password;
  Session* session = createSession(this);
  int groupId =
      SessionGroup::addSessionToGroup(SessionGroup::ONE_CENTER, session);
  session->setProtcolType(ProtocolType::Ssh);
  session->setProgram(ssh);
  session->setSessionId(sessionId);
  session->setHost(host);
  session->setUser(user);
  session->setPassword(password);

  SessionGroup* group = SessionGroup::getSessionGroup(groupId);
  group->unbindViewEmulation();
  SessionGroup::activeSession = session;
  group->bindViewToEmulation();

  connect(this, SIGNAL(updateBackground(const QColor&)), session->getTab(),
          SLOT(onBackgroundChange(const QColor&)));

  // connect view signals and slots
  connect(_terminalView, SIGNAL(changedContentSizeSignal(int, int)), session,
          SLOT(onViewSizeChange(int, int)));

  // slot for close
  connect(_terminalView, SIGNAL(destroyed(QObject*)), session,
          SLOT(viewDestroyed(QObject*)));

  session->run();
}

void TerminalEmulator::shellStartupSession(long sessionId, QString param) {
  qDebug() << "Receive shell startup session event, command = " << param;
  Session* session = createSession(this);
  int groupId =
      SessionGroup::addSessionToGroup(SessionGroup::ONE_CENTER, session);
  session->setProtcolType(ProtocolType::LocalShell);
  session->setProgram(param);
  session->setSessionId(sessionId);

  SessionGroup* group = SessionGroup::getSessionGroup(groupId);
  group->unbindViewEmulation();
  SessionGroup::activeSession = session;
  group->bindViewToEmulation();

  connect(this, SIGNAL(updateBackground(const QColor&)), session->getTab(),
          SLOT(onBackgroundChange(const QColor&)));

  // connect view signals and slots
  connect(_terminalView, SIGNAL(changedContentSizeSignal(int, int)), session,
          SLOT(onViewSizeChange(int, int)));

  // slot for close
  connect(_terminalView, SIGNAL(destroyed(QObject*)), session,
          SLOT(viewDestroyed(QObject*)));

  session->run();
}

void TerminalEmulator::sendSimulatedEvent(QEvent* event) {
  if (!SessionGroup::activeSession) {
    return;
  }

  SessionGroup* activeGroup = SessionGroup::getSessionGroup(
      SessionGroup::activeSession->sessionGroupId());
  QApplication::sendEvent(activeGroup->view(), event);
}

void TerminalEmulator::setNativeRedrawCallback(
    const std::function<void()>& newNativeRedrawCallback) {
  nativeRedrawCallback = newNativeRedrawCallback;
}

void TerminalEmulator::setNativeCanvas(nativers::SharedCanvas* nativeCanvas) {
  _terminalView->setNativeCanvas(nativeCanvas);
}

void TerminalEmulator::selectionChanged(bool textSelected) {
  emit copyAvailable(textSelected);
}

void TerminalEmulator::onCursorChanged(KeyboardCursorShape cursorShape,
                                       bool blinkingCursorEnabled) {
  // TODO: A switch to enable/disable DECSCUSR?
  setCursorShape(cursorShape);
  setBlinkingCursor(blinkingCursorEnabled);
}

void TerminalEmulator::nativeEventCallback() { nativeEvtCallback(); }

void TerminalEmulator::onTabRightClick() {
  _terminalView->nativeCanvas()->pushNativeEvent(
      "win.right-click-terminal-tab");
}

void TerminalEmulator::onTabButtonMousePress(QString name, int button) {
  QString evtMsg = "win.tab-button-mouse-press";
  evtMsg += ";" + name + ";" + QString::number(button);
  _terminalView->nativeCanvas()->pushNativeEvent(evtMsg.toStdString());
}

void TerminalEmulator::onTabButtonMouseRelease(QString name, int button) {
  QString evtMsg = "win.tab-button-mouse-release";
  evtMsg += ";" + name + ";" + QString::number(button);
  _terminalView->nativeCanvas()->pushNativeEvent(evtMsg.toStdString());
}
