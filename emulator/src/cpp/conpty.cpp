#include "conpty.h"

#include <QSize>

using namespace TConsole;

ConPty::ConPty(QObject *parent) : QObject{parent} { ConPty::init(); }

void ConPty::init() {
  _windowColumns = 0;
  _windowLines = 0;
  _eraseChar = 0;
  _xonXoff = true;
  _utf8 = true;
}

int ConPty::start(const QString &program, const QStringList &arguments,
                  const QStringList &environment, ulong winid, bool addToUtmp) {
#ifdef Q_OS_WIN
#endif
  return 0;
}

void ConPty::setEmptyPTYProperties() {
#ifdef Q_OS_WIN
#endif
}

void ConPty::setWriteable(bool writeable) {
#ifdef Q_OS_WIN
#endif
}

void ConPty::setFlowControlEnabled(bool on) { _xonXoff = on; }

bool ConPty::flowControlEnabled() const { return _xonXoff; }

void ConPty::setWindowSize(int lines, int cols) {
  _windowColumns = cols;
  _windowLines = lines;
}

QSize ConPty::windowSize() const { return {_windowColumns, _windowLines}; }

void ConPty::setErase(char erase) {
#ifdef Q_OS_WIN
#endif
}

char ConPty::erase() const { return _eraseChar; }

int ConPty::foregroundProcessGroup() const {
#ifdef Q_OS_WIN
#endif
  return 0;
}

void ConPty::setUtf8Mode(bool on) {
#ifdef Q_OS_WIN
#endif
}

void ConPty::lockPty(bool lock) {
#ifdef Q_OS_WIN
#endif
}

void ConPty::sendData(const char *buffer, int length) {
#ifdef Q_OS_WIN
#endif
}

void ConPty::dataReceived() {
#ifdef Q_OS_WIN
#endif
}
