#ifndef VIRTUALPTY_H
#define VIRTUALPTY_H
#include <QObject>
class VirtualPty {
 public:
  VirtualPty(){};
  ~VirtualPty(){};

  /**
   * Starts the terminal process.
   *
   * Returns 0 if the process was started successfully or non-zero
   * otherwise.
   *
   * @param program Path to the program to start
   * @param arguments Arguments to pass to the program being started
   * @param environment A list of key=value pairs which will be added
   * to the environment for the new process.  At the very least this
   * should include an assignment for the TERM environment variable.
   * @param winid Specifies the value of the WINDOWID environment variable
   * in the process's environment.
   * @param addToUtmp Specifies whether a utmp entry should be created for
   * the pty used.  See K3Process::setUsePty()
   * @param dbusService Specifies the value of the KONSOLE_DBUS_SERVICE
   * environment variable in the process's environment.
   * @param dbusSession Specifies the value of the KONSOLE_DBUS_SESSION
   * environment variable in the process's environment.
   */
  virtual int start(const QString& program, const QStringList& arguments,
                    const QStringList& environment, ulong winid,
                    bool addToUtmp) = 0;
  /**
   * set properties for "EmptyPTY"
   */
  virtual void setEmptyPTYProperties() = 0;

  /** TODO: Document me */
  virtual void setWriteable(bool writeable) = 0;

  /**
   * Enables or disables Xon/Xoff flow control.  The flow control setting
   * may be changed later by a terminal application, so flowControlEnabled()
   * may not equal the value of @p on in the previous call to
   * setFlowControlEnabled()
   */
  virtual void setFlowControlEnabled(bool on) = 0;

  /** Queries the terminal state and returns true if Xon/Xoff flow control is
   * enabled. */
  virtual bool flowControlEnabled() const = 0;

  /**
   * Sets the size of the window (in lines and columns of characters)
   * used by this teletype.
   */
  virtual void setWindowSize(int lines, int cols) = 0;

  /**
   * Returns the size of the window used by this teletype.  See setWindowSize()
   */
  virtual QSize windowSize() const = 0;

  /** TODO Document me */
  virtual void setErase(char erase) = 0;

  /** */
  virtual char erase() const = 0;

  /**
   * Returns the process id of the teletype's current foreground
   * process.  This is the process which is currently reading
   * input sent to the terminal via. sendData()
   *
   * If there is a problem reading the foreground process group,
   * 0 will be returned.
   */
  virtual int foregroundProcessGroup() const = 0;

 protected:
  virtual void init() = 0;

  int _windowColumns;
  int _windowLines;
  char _eraseChar;
  bool _xonXoff;
  bool _utf8;
};
#endif  // VITRUALPTY_H
