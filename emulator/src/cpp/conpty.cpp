#include "conpty.h"

#include <QDebug>
#include <QSize>
#include <QString>
#include <QStringList>
#include <thread>

#ifdef Q_OS_WIN
#include <pipeio.h>
#include <winconpty.h>

#include <QString>
#endif

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
  Q_ASSERT(arguments.count() >= 1);
  QString execute;
  execute.append(program).append(" ").append(arguments[1]);
  QString passwordTip = QString(arguments[1]).append("'s password:");
  QString password = QString(arguments[2]).append("\n");

  fd = openConPty(_windowLines, _windowColumns);
  setUTF8Mode(_utf8);
  if (fd > 0) {
    startReadListener(fd, [=](const char *data, const int length) {
      if (length < 0) return;
      if (QString(data).contains(passwordTip)) {
        sendData(password.toStdString().c_str(), -1);
      } else {
        char *dup = new char[length];
        memcpy(dup, data, length);
        emit receivedData(dup, strlen(dup));
      }
    });

    // ssh root@47.108.157.178
    auto subProcess = [&](int fd, QString execute) {
      startSubProcess(
          fd, (LPWSTR)execute.append(" -e \033").toStdWString().c_str());
      emit finished(-1);
    };
    std::thread(subProcess, fd, execute).detach();
  }
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
  resizeConPty(fd, lines, cols);
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

void ConPty::setWorkingDirectory(const QString dir) { _workingDirectory = dir; }

bool ConPty::isRunning() { return _running; }

void ConPty::setUtf8Mode(bool on) {
#ifdef Q_OS_WIN
  _utf8 = on;
  setUTF8Mode(on);
#endif
}

void ConPty::lockPty(bool lock) {
#ifdef Q_OS_WIN
#endif
}

void ConPty::sendData(const char *buffer, int length) {
#ifdef Q_OS_WIN
  writeData(fd, buffer);
#endif
}

void ConPty::dataReceived() {
#ifdef Q_OS_WIN
#endif
}
