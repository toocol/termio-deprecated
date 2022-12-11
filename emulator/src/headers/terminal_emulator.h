#ifndef TERMINALEMULATOR_H
#define TERMINALEMULATOR_H

#include <QPainter>
#include <QTextBlock>
#include <QTextDocument>
#include <QTextEdit>
#include <QTextLayout>
#include <QVBoxLayout>
#include <QWidget>

#include "session.h"
#include "transmit_signals.h"

namespace TConsole {

class TerminalEmulator : public QWidget {
  Q_OBJECT
 public:
  TerminalEmulator(QWidget *parent = nullptr);
  ~TerminalEmulator();

  Session *createSession(QWidget *parent);

  void initialize();
  /**
   * Sets the shape of the keyboard cursor.  This is the cursor drawn
   * at the position in the terminal where keyboard input will appear.
   */
  void setCursorShape(KeyboardCursorShape shape);

  void setBlinkingCursor(bool blink);

  void setTerminalFont(const QFont &font);

  void setBackgroundColor(const QColor &color);

  void setForegroundColor(const QColor &color);

  void sendText(QString text);

  void clear();

  void requestRedrawImage(QImage *, QImage *);

  void setNativeRedrawCallback(
      const std::function<void()> &newNativeRedrawCallback);

  void setNativeCanvas(nativers::SharedCanvas *nativeData);

  void setNativeEvtCallback(const std::function<void()> &newNativeEvtCallback);

  void createSshSession(long sessionId, QString host, QString user,
                        QString password);

  void sendSimulatedEvent(QEvent *);

 protected:
  bool eventFilter(QObject *obj, QEvent *ev) override;

 private:
  std::function<void()> nativeRedrawCallback;
  std::function<void()> nativeEvtCallback;
  QImage *_primaryImage;
  QImage *_secondaryImage;
  QTimer *_nativeEvtTimer;

  TransmitSignals *_transmitSingals;
  TerminalView *_terminalView;

  QVBoxLayout *_mainLayout;

 signals:
  /**
   * Emitted when the terminal process exits.
   */
  void finished();

  void copyAvailable(bool);

  void termGetFocus();
  void termLostFocus();

  void termKeyPressed(QKeyEvent *);

  void urlActivated(const QUrl &, bool fromContextMenu);

  void updateBackground(const QColor &);

 protected slots:
  void selectionChanged(bool textSelected);

 private slots:
  void onCursorChanged(TConsole::KeyboardCursorShape cursorShape,
                       bool blinkingCursorEnabled);
  void nativeEventCallback();
  void onTabRightClick();
  void onTabButtonMousePress(QString, int);
  void onTabButtonMouseRelease(QString, int);
};

}  // namespace TConsole
#endif  // TERMINALEMULATOR_H
