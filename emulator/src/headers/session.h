#ifndef _EMU_SESSION_H
#define _EMU_SESSION_H

#include <QObject>

#include "emulation.h"
#include "kprocess.h"

#ifdef Q_OS_WIN
#include "conpty.h"
#else
#include "pty.h"
#endif

// class ZModemDialog;

namespace TConsole {

/**
 * Represents a ssh session consisting of a pseudo-teletype(PTY for linux,
 * ConPTY for Windows). The PTY(or ConPTY) handles I/O between the terminal
 * process and TConsole.
 */
class Session : public QObject {
  Q_OBJECT
 public:
  explicit Session(QObject* parent = nullptr);

  /**
   * Adds a new view for this session.
   *
   * The viewing widget will display the output from the terminal and
   * input from the viewing widget (key presses, mouse activity etc.)
   * will be sent to the terminal.
   *
   * Views can be removed using removeView().  The session is automatically
   * closed when the last view is removed.
   */
  void addView(TerminalView* widget);
  /**
   * Removes a view from this session.  When the last view is removed,
   * the session will be closed automatically.
   *
   * @p widget will no longer display output from or send input
   * to the terminal
   */
  void removeView(TerminalView* widget);
  /**
   * Returns the views connected to this session
   */
  QList<TerminalView*> views() const;
  /**
   * Returns the terminal emulation instance being used to encode / decode
   * characters to / from the process.
   */
  Emulation* emulation() const;

  /** Returns the unique ID for this session. */
  int sessionId() const;

  /**
   * Return the session title set by the user (ie. the program running
   * in the terminal), or an empty string if the user has not set a custom title
   */
  QString userTitle() const;

  /**
   * This enum describes the contexts for which separate
   * tab title formats may be specified.
   */
  enum TabTitleContext {
    /** Default tab title format */
    LocalTabTitle,
    /**
     * Tab title format used session currently contains
     * a connection to a remote computer (via SSH)
     */
    RemoteTabTitle
  };
  /**
   * Sets the format used by this session for tab titles.
   *
   * @param context The context whose format should be set.
   * @param format The tab title format.  This may be a mixture
   * of plain text and dynamic elements denoted by a '%' character
   * followed by a letter.  (eg. %d for directory).  The dynamic
   * elements available depend on the @p context
   */
  void setTabTitleFormat(TabTitleContext context, const QString& format);
  /** Returns the format used by this session for tab titles. */
  QString tabTitleFormat(TabTitleContext context) const;

  //  /** Returns the arguments passed to the shell process when run() is
  //  called. */ QStringList arguments() const;
  //  /** Returns the program name of the shell process started when run() is
  //   * called. */
  //  QString program() const;

  /**
   * Sets the command line arguments which the session's program will be passed
   * when run() is called.
   */
  void setArguments(const QStringList& arguments);
  /** Sets the program to be executed when run() is called. */
  void setProgram(const QString& program);

  /** Returns the session's current working directory. */
  QString initialWorkingDirectory() { return _initialWorkingDir; }
  /**
   * Sets the initial working directory for the session when it is run
   * This has no effect once the session has been started.
   */
  void setInitialWorkingDirectory(const QString& dir);

  /**
   * This enum describes the available title roles.
   */
  enum TitleRole {
    /** The name of the session. */
    NameRole,
    /** The title of the session which is displayed in tabs etc. */
    DisplayedTitleRole
  };
  /** Sets the session's title for the specified @p role to @p title. */
  void setTitle(TitleRole role, const QString& title);
  /** Returns the session's title for the specified @p role. */
  QString title(TitleRole role) const;
  /** Convenience method used to read the name property.  Returns
   * title(Session::NameRole). */
  QString nameTitle() const { return title(Session::NameRole); }

  /** Sets the name of the icon associated with this session. */
  void setIconName(const QString& iconName);
  /** Returns the name of the icon associated with this session. */
  QString iconName() const;

  /** Sets the text of the icon associated with this session. */
  void setIconText(const QString& iconText);
  /** Returns the text of the icon associated with this session. */
  QString iconText() const;

  /** Flag if the title/icon was changed by user/shell. */
  bool isTitleChanged() const;

  /** Specifies whether a utmp entry should be created for the pty used by this
   * session. */
  void setAddToUtmp(bool);

  /** Sends the specified @p signal to the terminal process. */
  bool sendSignal(int signal);

  /**
   * Sets whether flow control is enabled for this terminal
   * session.
   */
  void setFlowControlEnabled(bool enabled);
  /** Returns whether flow control is enabled for this terminal session. */
  bool flowControlEnabled() const;

  /**
   * Sends @p text to the current foreground terminal program.
   */
  void sendText(const QString& text) const;
  void sendKeyEvent(QKeyEvent* e) const;

 signals:
  /** Emitted when the terminal process starts. */
  void started();

  /**
   * Emitted when the terminal process exits.
   */
  void finished();

  /**
   * Emitted when output is received from the terminal process.
   */
  void receivedData(const QString& text);

  /** Emitted when the session's title has changed. */
  void titleChanged();

  /** Emitted when the session's profile has changed. */
  void profileChanged(const QString& profile);

  /**
   * Emitted when the activity state of this session changes.
   *
   * @param state The new state of the session.  This may be one
   * of NOTIFYNORMAL, NOTIFYSILENCE or NOTIFYACTIVITY
   */
  void stateChanged(int state);

  /** Emitted when a bell event occurs in the session. */
  void bellRequest(const QString& message);

  /**
   * Requests that the color the text for any tabs associated with
   * this session should be changed;
   *
   * TODO: Document what the parameter does
   */
  void changeTabTextColorRequest(int);

  /**
   * Requests that the background color of views on this session
   * should be changed.
   */
  void changeBackgroundColorRequest(const QColor&);

  /** TODO: Document me. */
  void openUrlRequest(const QString& url);

  /** TODO: Document me. */
  void zmodemDetected();

  /**
   * Emitted when the terminal process requests a change
   * in the size of the terminal window.
   *
   * @param size The requested window size in terms of lines and columns.
   */
  void resizeRequest(const QSize& size);

  /**
   * Emitted when a profile change command is received from the terminal.
   *
   * @param text The text of the command.  This is a string of the form
   * "PropertyName=Value;PropertyName=Value ..."
   */
  void profileChangeCommandReceived(const QString& text);

  /**
   * Emitted when the flow control state changes.
   *
   * @param enabled True if flow control is enabled or false otherwise.
   */
  void flowControlEnabledChanged(bool enabled);

  /**
   * Broker for Emulation::cursorChanged() signal
   */
  void cursorChanged(TConsole::KeyboardCursorShape cursorShape,
                     bool blinkingCursorEnabled);

  void silence();
  void activity();

 public slots:

  /**
   * Starts the terminal session.
   *
   * This creates the terminal process and connects the teletype to it.
   */
  void run();

  /**
   * Starts the terminal session for "as is" PTY
   * (without the direction a data to internal terminal process).
   * It can be used for control or display a remote/external terminal.
   */
  void runEmptyPTY();

  /**
   * Closes the terminal session.  This sends a hangup signal
   * (SIGHUP) to the terminal process and causes the done(Session*)
   * signal to be emitted.
   */
  void close();

  /**
   * Changes the session title or other customizable aspects of the terminal
   * emulation display. For a list of what may be changed see the
   * Emulation::titleChanged() signal.
   */
  void setUserTitle(int, const QString& caption);

 private slots:
  void done(int);

  //  void fireZModemDetected();

  void onReceiveBlock(const char* buffer, int len);
  void monitorTimerDone();

  void onViewSizeChange(int height, int width);
  void onEmulationSizeChange(QSize);

  void activityStateSet(int);

  // automatically detach views from sessions when view is destroyed
  void viewDestroyed(QObject* view);

  //  void zmodemReadStatus();
  //  void zmodemReadAndSendBlock();
  //  void zmodemRcvBlock(const char* data, int len);
  //  void zmodemFinished();

 private:
  static int lastSessionId;

  WId windowId() const;

  Emulation* _emulation;
  QList<TerminalView*> _views;
#ifdef Q_OS_WIN
  ConPty* _shellProcess;
#else
  Pty* _shellProcess;
#endif
  QStringList _environment;
  int _sessionId;

  bool _autoClose;
  bool _wantedClose;

  QString _nameTitle;
  QString _displayTitle;
  QString _userTitle;

  QString _localTabTitleFormat;
  QString _remoteTabTitleFormat;

  QString _initialWorkingDir;

  QString _iconName;
  QString _iconText;     // as set by: echo -en '\033]1;IconText\007
  bool _isTitleChanged;  ///< flag if the title/icon was changed by user
  bool _addToUtmp;
  bool _flowControl;
  bool _fullScripting;

  QString _program;
  QStringList _arguments;

  bool _hasDarkBackground;
  QColor _modifiedBackground;  // as set by: echo -en '\033]11;Color\007

  // ZModem
  bool _zmodemBusy;
  KProcess* _zmodemProc;
  //  ZModemDialog* _zmodemProgress;
};

}  // namespace TConsole
#endif  // SESSION_H
