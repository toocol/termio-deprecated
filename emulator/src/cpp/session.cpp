#include "session.h"

#include <QDir>

#include "shellcommand.h"
#include "vt102emulation.h"

using namespace TConsole;

int Session::lastSessionId = 0;

static QRegularExpression rexp(QLatin1String("^~"));

Session::Session(QObject *parent)
    : QObject{parent},
      _emulation(nullptr),
      _shellProcess(nullptr),
      _sessionId(0),
      _isTitleChanged(false),
      _addToUtmp(false),
      _flowControl(true),
      _fullScripting(false) {
  _sessionId = ++lastSessionId;
#ifdef Q_OS_WIN
  _shellProcess = new ConPty();
#else
  _shellProcess = new Pty();
#endif

  // create emulation backend
  _emulation = new Vt102Emulation();

  connect(_emulation, SIGNAL(titleChanged(int, QString)), this,
          SLOT(setUserTitle(int, QString)));
  connect(_emulation, SIGNAL(stateSet(int)), this, SLOT(activityStateSet(int)));
  //    connect( _emulation, SIGNAL( zmodemDetected() ), this ,
  //            SLOT( fireZModemDetected() ) );
  connect(_emulation, SIGNAL(changeTabTextColorRequest(int)), this,
          SIGNAL(changeTabTextColorRequest(int)));
  connect(_emulation, SIGNAL(profileChangeCommandReceived(QString)), this,
          SIGNAL(profileChangeCommandReceived(QString)));

  connect(_emulation, SIGNAL(imageResizeRequest(QSize)), this,
          SLOT(onEmulationSizeChange(QSize)));
  connect(_emulation, SIGNAL(imageSizeChanged(int, int)), this,
          SLOT(onViewSizeChange(int, int)));
  connect(_emulation, &Vt102Emulation::cursorChanged, this,
          &Session::cursorChanged);

  // connect teletype to emulation backend
  _shellProcess->setUtf8Mode(_emulation->utf8());

  connect(_shellProcess, SIGNAL(receivedData(const char *, int)), this,
          SLOT(onReceiveBlock(const char *, int)));
  connect(_emulation, SIGNAL(sendData(const char *, int)), _shellProcess,
          SLOT(sendData(const char *, int)));
  connect(_emulation, SIGNAL(lockPtyRequest(bool)), _shellProcess,
          SLOT(lockPty(bool)));
  connect(_emulation, SIGNAL(useUtf8Request(bool)), _shellProcess,
          SLOT(setUtf8Mode(bool)));

  connect(_shellProcess, SIGNAL(finished(int, QProcess::ExitStatus)), this,
          SLOT(done(int)));
}

void Session::addView(TerminalView *widget) {
  Q_ASSERT(!_views.contains(widget));
  _views.append(widget);
}

void Session::removeView(TerminalView *widget) {
  _views.removeAll(widget);
  disconnect(widget, nullptr, this, nullptr);
  // close the session automatically when the last view is removed
  if (_views.count() == 0) {
    close();
  }
}

QList<TerminalView *> Session::views() const { return _views; }

Emulation *Session::emulation() const { return _emulation; }

int Session::sessionId() const { return _sessionId; }

QString Session::userTitle() const { return _userTitle; }

void Session::setTabTitleFormat(TabTitleContext context,
                                const QString &format) {
  if (context == LocalTabTitle) {
    _localTabTitleFormat = format;
  } else if (context == RemoteTabTitle) {
    _remoteTabTitleFormat = format;
  }
}

QString Session::tabTitleFormat(TabTitleContext context) const {
  if (context == LocalTabTitle) {
    return _localTabTitleFormat;
  } else if (context == RemoteTabTitle) {
    return _remoteTabTitleFormat;
  }
  return QString();
}

void Session::setArguments(const QStringList &arguments) {
  _arguments = ShellCommand::expand(arguments);
}

void Session::setProgram(const QString &program) {
  _program = ShellCommand::expand(program);
}

void Session::setInitialWorkingDirectory(const QString &dir) {
  _initialWorkingDir = ShellCommand::expand(dir);
}

void Session::setTitle(TitleRole role, const QString &newTitle) {
  if (title(role) != newTitle) {
    if (role == NameRole) {
      _nameTitle = newTitle;
    } else if (role == DisplayedTitleRole) {
      _displayTitle = newTitle;
    }

    emit titleChanged();
  }
}

QString Session::title(TitleRole role) const {
  if (role == NameRole) {
    return _nameTitle;
  } else if (role == DisplayedTitleRole) {
    return _displayTitle;
  } else {
    return QString();
  }
}

void Session::setIconName(const QString &iconName) {
  if (iconName != _iconName) {
    _iconName = iconName;
    emit titleChanged();
  }
}

QString Session::iconName() const { return _iconName; }

void Session::setIconText(const QString &iconText) { _iconText = iconText; }

QString Session::iconText() const { return _iconText; }

bool Session::isTitleChanged() const { return false; }

void Session::setAddToUtmp(bool on) { _addToUtmp = on; }

bool Session::sendSignal(int signal) { return false; }

void Session::setFlowControlEnabled(bool enabled) { _flowControl = enabled; }

bool Session::flowControlEnabled() const { return _flowControl; }

void Session::sendText(const QString &text) const {
  _emulation->sendText(text);
}

void Session::sendKeyEvent(QKeyEvent *e) const {
  _emulation->sendKeyEvent(e, false);
}

void Session::run() {
  // Upon a KPty error, there is no description on what that error was...
  // Check to see if the given program is executable.

  /* ok I'm not exactly sure where _program comes from - however it was set to
   * /bin/bash on my system That's bad for BSD as its /usr/local/bin/bash there
   * - its also bad for arch as its /usr/bin/bash there too! So i added a check
   * to see if /bin/bash exists - if no then we use $SHELL - if that does not
   * exist either, we fall back to /bin/sh As far as i know /bin/sh exists on
   * every unix system.. You could also just put some ifdef __FREEBSD__ here but
   * i think these 2 filechecks are worth their computing time on any system -
   * especially with the problem on arch linux being there too.
   */
  QString exec = QString::fromLocal8Bit(QFile::encodeName(_program));
  // if 'exec' is not specified, fall back to default shell.  if that
  // is not set then fall back to /bin/sh

  // here we expect full path. If there is no fullpath let's expect it's
  // a custom shell (eg. python, etc.) available in the PATH.
  if (exec.startsWith(QLatin1Char('/')) || exec.isEmpty()) {
    const QString defaultShell{QLatin1String("/bin/sh")};

    QFile excheck(exec);
    if (exec.isEmpty() || !excheck.exists()) {
      exec = QString::fromLocal8Bit(qgetenv("SHELL"));
    }
    excheck.setFileName(exec);

    if (exec.isEmpty() || !excheck.exists()) {
      qWarning() << "Neither default shell nor $SHELL is set to a correct "
                    "path. Fallback to"
                 << defaultShell;
      exec = defaultShell;
    }
  }

  // _arguments sometimes contain ("") so isEmpty()
  // or count() does not work as expected...
  QString argsTmp(_arguments.join(QLatin1Char(' ')).trimmed());
  QStringList arguments;
  arguments << exec;
  if (argsTmp.length()) arguments << _arguments;

  QString cwd = QDir::currentPath();
  if (!_initialWorkingDir.isEmpty()) {
    _shellProcess->setWorkingDirectory(_initialWorkingDir);
  } else {
    _shellProcess->setWorkingDirectory(cwd);
  }

  _shellProcess->setFlowControlEnabled(_flowControl);
  _shellProcess->setErase(_emulation->eraseChar());

  // this is not strictly accurate use of the COLORFGBG variable.  This does not
  // tell the terminal exactly which colors are being used, but instead
  // approximates the color scheme as "black on white" or "white on black"
  // depending on whether the background color is deemed dark or not
  QString backgroundColorHint = _hasDarkBackground
                                    ? QLatin1String("COLORFGBG=15;0")
                                    : QLatin1String("COLORFGBG=0;15");

  /* if we do all the checking if this shell exists then we use it ;)
   * Dont know about the arguments though.. maybe youll need some more checking
   * im not sure However this works on Arch and FreeBSD now.
   */
  int result =
      _shellProcess->start(exec, arguments, _environment << backgroundColorHint,
                           windowId(), _addToUtmp);

  if (result < 0) {
    qDebug() << "CRASHED! result: " << result;
    return;
  }

  _shellProcess->setWriteable(false);  // We are reachable via kwrited.
  emit started();
}

void Session::runEmptyPTY() {
  _shellProcess->setFlowControlEnabled(_flowControl);
  _shellProcess->setErase(_emulation->eraseChar());
  _shellProcess->setWriteable(false);

  // disconnect send data from emulator to internal terminal process
  disconnect(_emulation, SIGNAL(sendData(const char *, int)), _shellProcess,
             SLOT(sendData(const char *, int)));

  _shellProcess->setEmptyPTYProperties();
  emit started();
}

void Session::close() {
  _autoClose = true;
  _wantedClose = true;
  if (!_shellProcess->isRunning() || !sendSignal(SIGHUP)) {
    // Forced close.
    QTimer::singleShot(1, this, SIGNAL(finished()));
  }
}

void Session::setUserTitle(int what, const QString &caption) {
  // set to true if anything is actually changed (eg. old _nameTitle != new
  // _nameTitle )
  bool modified = false;

  // (btw: what=0 changes _userTitle and icon, what=1 only icon, what=2 only
  // _nameTitle
  if ((what == 0) || (what == 2)) {
    _isTitleChanged = true;
    if (_userTitle != caption) {
      _userTitle = caption;
      modified = true;
    }
  }

  if ((what == 0) || (what == 1)) {
    _isTitleChanged = true;
    if (_iconText != caption) {
      _iconText = caption;
      modified = true;
    }
  }

  if (what == 11) {
    QString colorString = caption.section(QLatin1Char(';'), 0, 0);
    // qDebug() << __FILE__ << __LINE__ << ": setting background colour to " <<
    // colorString;
    QColor backColor = QColor(colorString);
    if (backColor.isValid()) {  // change color via \033]11;Color\007
      if (backColor != _modifiedBackground) {
        _modifiedBackground = backColor;

        // bail out here until the code to connect the terminal display
        // to the changeBackgroundColor() signal has been written
        // and tested - just so we don't forget to do this.
        Q_ASSERT(0);

        emit changeBackgroundColorRequest(backColor);
      }
    }
  }

  if (what == 30) {
    _isTitleChanged = true;
    if (_nameTitle != caption) {
      setTitle(Session::NameRole, caption);
      return;
    }
  }

  if (what == 31) {
    QString cwd = caption;
    cwd = cwd.replace(rexp, QDir::homePath());
    emit openUrlRequest(cwd);
  }

  // change icon via \033]32;Icon\007
  if (what == 32) {
    _isTitleChanged = true;
    if (_iconName != caption) {
      _iconName = caption;

      modified = true;
    }
  }

  if (what == 50) {
    emit profileChangeCommandReceived(caption);
    return;
  }

  if (modified) {
    emit titleChanged();
  }
}

void Session::done(int) {
  if (!_autoClose) {
    _userTitle = QString::fromLatin1("This session is done. Finished");
    emit titleChanged();
    return;
  }

  // message is not being used. But in the original kpty.cpp file
  // (https://cgit.kde.org/kpty.git/) it's part of a notification.
  // So, we make it translatable, hoping that in the future it will
  // be used in some kind of notification.
  //  QString message;
  //  if (!_wantedClose || exitStatus != 0) {

  //    if (_shellProcess->exitStatus() == QProcess::NormalExit) {
  //      message = tr("Session '%1' exited with status
  //      %2.").arg(_nameTitle).arg(exitStatus);
  //    } else {
  //      message = tr("Session '%1' crashed.").arg(_nameTitle);
  //    }
  //  }

  //  if ( !_wantedClose && _shellProcess->exitStatus() != QProcess::NormalExit
  //  )
  //    message = tr("Session '%1' exited unexpectedly.").arg(_nameTitle);
  //  else
  //    emit finished();
}

void Session::onReceiveBlock(const char *buffer, int len) {
  _emulation->receiveData(buffer, len);
  emit receivedData(QString::fromLatin1(buffer, len));
}

void Session::monitorTimerDone() {}

void Session::onViewSizeChange(int height, int width) {}

void Session::onEmulationSizeChange(QSize) {}

void Session::activityStateSet(int) {}

void Session::viewDestroyed(QObject *view) {}

WId Session::windowId() const { return 0; }
