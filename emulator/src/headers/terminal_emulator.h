#ifndef TERMINALEMULATOR_H
#define TERMINALEMULATOR_H

#include <QPainter>
#include <QTextBlock>
#include <QTextDocument>
#include <QTextEdit>
#include <QTextLayout>
#include <QVBoxLayout>
#include <QWidget>

#include "emulation.h"
#include "session.h"

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

  void requestRedrawImage(QImage *);

  void setNativeRedrawCallback(
      const std::function<void()> &newNativeRedrawCallback);

  void setNativeCanvas(nativefx::SharedCanvas *nativeData);

  void setNativeEvtCallback(const std::function<void()> &newNativeEvtCallback);

  void createSshSession(long sessionId, QString host, QString user,
                        QString password);

  void sendSimulatedEvent(QEvent *);

 protected:
  bool eventFilter(QObject *obj, QEvent *ev) override;

 private:
  void bindViewToEmulation(Emulation *, TerminalView *);

  std::function<void()> nativeRedrawCallback;
  std::function<void()> nativeEvtCallback;
  QImage *_nativeImage;
  QTimer *_nativeEvtTimer;

  TerminalView *_terminalView;
  Emulation *_emulation;

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

 protected slots:
  void selectionChanged(bool textSelected);

 private slots:
  void onCursorChanged(TConsole::KeyboardCursorShape cursorShape,
                       bool blinkingCursorEnabled);
  void nativeEventCallback();
};

}  // namespace TConsole
#endif  // TERMINALEMULATOR_H
