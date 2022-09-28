#ifndef CONPTY_H
#define CONPTY_H

#include <QObject>
#include <QProcess>

#include "virtualpty.h"

// It is written here temporarily and Signal needs to be handled separately
// later
#define SIGHUP 1

namespace TConsole {

class ConPty : public QObject, public VirtualPty {
  Q_OBJECT
 public:
  explicit ConPty(QObject* parent = nullptr);

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
  int start(const QString& program, const QStringList& arguments,
            const QStringList& environment, ulong winid,
            bool addToUtmp) override;

  /**
   * set properties for "EmptyPTY"
   */
  void setEmptyPTYProperties() override;

  /** TODO: Document me */
  void setWriteable(bool writeable) override;

  /**
   * Enables or disables Xon/Xoff flow control.  The flow control setting
   * may be changed later by a terminal application, so flowControlEnabled()
   * may not equal the value of @p on in the previous call to
   * setFlowControlEnabled()
   */
  void setFlowControlEnabled(bool on) override;

  /** Queries the terminal state and returns true if Xon/Xoff flow control is
   * enabled. */
  bool flowControlEnabled() const override;

  /**
   * Sets the size of the window (in lines and columns of characters)
   * used by this teletype.
   */
  void setWindowSize(int lines, int cols) override;

  /** Returns the size of the window used by this teletype.  See setWindowSize()
   */
  QSize windowSize() const override;

  /** TODO Document me */
  void setErase(char erase) override;

  /** */
  char erase() const override;

  /**
   * Returns the process id of the teletype's current foreground
   * process.  This is the process which is currently reading
   * input sent to the terminal via. sendData()
   *
   * If there is a problem reading the foreground process group,
   * 0 will be returned.
   */
  int foregroundProcessGroup() const override;

  void setWorkingDirectory(const QString);

  bool isRunning();

  QProcess::ExitStatus exitStatus() const { return _exitStatus; }

 public slots:

  /**
   * Put the pty into UTF-8 mode on systems which support it.
   */
  void setUtf8Mode(bool on);

  /**
   * Suspend or resume processing of data from the standard
   * output of the terminal process.
   *
   * See K3Process::suspend() and K3Process::resume()
   *
   * @param lock If true, processing of output is suspended,
   * otherwise processing is resumed.
   */
  void lockPty(bool lock);

  /**
   * Sends data to the process currently controlling the
   * teletype ( whose id is returned by foregroundProcessGroup() )
   *
   * @param buffer Pointer to the data to send.
   * @param length Length of @p buffer.
   */
  void sendData(const char* buffer, int length);

 signals:

  /**
   * Emitted when a new block of data is received from
   * the teletype.
   *
   * @param buffer Pointer to the data received.
   * @param length Length of @p buffer
   */
  void receivedData(const char* buffer, int length);

 protected:
  void init() override;

 private slots:
  // called when data is received from the terminal process
  void dataReceived();

 private:
  int fd;
  bool _running;
  QProcess::ExitStatus _exitStatus;
  QString _workingDirectory;
};

}  // namespace TConsole

#endif  // CONPTY_H
