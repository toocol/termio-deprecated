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
class Session : public QWidget {
  Q_OBJECT
 public:
  explicit Session(QWidget* parent = nullptr);

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
   * Sets the type of history store used by this session.
   * Lines of output produced by the terminal are added
   * to the history store.  The type of history store
   * used affects the number of lines which can be
   * remembered before they are lost and the storage
   * (in memory, on-disk etc.) used.
   */
  void setHistoryType(const HistoryType& type);
  /**
   * Returns the type of history store used by this session.
   */
  const HistoryType& historyType() const;
  /**
   * Clears the history store used by this session.
   */
  void clearHistory();

  /**
   * Sets the key bindings used by this session.  The bindings
   * specify how input key sequences are translated into
   * the character stream which is sent to the terminal.
   *
   * @param id The name of the key bindings to use.  The
   * names of available key bindings can be determined using the
   * KeyboardTranslatorManager class.
   */
  void setKeyBindings(const QString& id);
  /** Returns the name of the key bindings used by this session. */
  QString keyBindings() const;

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

  /** Returns the terminal session's window size in lines and columns. */
  QSize size();
  /**
   * Emits a request to resize the session to accommodate
   * the specified window size.
   *
   * @param size The size in lines and columns to request.
   */
  void setSize(const QSize& size);
  /** Sets the text codec used by this session's terminal emulation. */
  void setCodec(QTextCodec* codec) const;

  /**
   * Specifies whether to close the session automatically when the terminal
   * process terminates.
   */
  void setAutoClose(bool b) { _autoClose = b; }

  int sessionGroupId() const;
  void setSessionGroupId(int newSessionGroupId);

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
  void updateTerminalSize();
  static int lastSessionId;
  static QRegularExpression _rexp;

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
  int _sessionGroupId;

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

/**
 * A session group contains a batch of sessions, every session group has one
 * combind TerminalView.
 * Means Session group is used for split screen.
 */
class SessionGroup : public QWidget {
  Q_OBJECT
 public:
  enum SplitScreenState { ZERO = 0, ONE = 1, TWO = 2, THREE = 4, FOUR = 8 };
  enum SessionGroupLocation {
    // Split screen for one.
    ONE_CENTER,
    // Split screen for two.
    TWO_LEFT,
    TWO_RIGHT,
    // Split screen for three.
    THREE_LEFT,
    THREE_RIGHT_TOP,
    THREE_RIGHT_BOTTOM,
    // Split screen for four.
    FOR_LEFT_TOP,
    FOR_LEFT_BOTTOM,
    FOR_RIGHT_TOP,
    FOR_RIGHT_BOTTOM
  };
  explicit SessionGroup(QWidget* parent = nullptr);

  static void initialize(QWidget*);
  /**
   * Change state and execute the state mechine.
   */
  static void changeState(SplitScreenState);

  /**
   * Add the session to the specific session group via SessionGroupLocation, and
   * return the session group id.
   */
  static int addSessionToGroup(SessionGroupLocation, Session*);

  static SessionGroup* getGroup(int);

  TerminalView* view() const;
  void setView(TerminalView* newView);

 private:
  static SplitScreenState _state;
  static int _lastSessionGroupId;
  static bool _isInit;
  /**
   * key:   Session group id
   * value: Sessions in the group
   */
  static QHash<int, SessionGroup*> _sessionGroupMaps;
  /**
   * key: currentState|newState(currentState < newState)
   *      -currentState|newState(currentState > newState)
   * value: function to execute
   */
  static QHash<int, std::function<void()>> _splitStateMachine;
  static SessionGroup* createNewSessionGroup(QWidget*);

  void createTerminalView(QWidget*);

  int _groupId;
  QList<Session*> _sessions;
  TerminalView* _view;
  SessionGroupLocation _location = ONE_CENTER;
};

}  // namespace TConsole
#endif  // SESSION_H
