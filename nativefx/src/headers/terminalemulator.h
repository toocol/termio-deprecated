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

namespace TConsole {

class TerminalEmulator : public QWidget {
  Q_OBJECT
 public:
  TerminalEmulator(QWidget *parent = nullptr);
  ~TerminalEmulator();

  void initialize();
  /** Returns the terminal session's window size in lines and columns. */
  QSize size();
  /**
   * Emits a request to resize the session to accommodate
   * the specified window size.
   *
   * @param size The size in lines and columns to request.
   */
  void setSize(const QSize &size);
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

  void setNativeRedrawCallback(const std::function<void ()> &newNativeRedrawCallback);

  protected:
  bool eventFilter(QObject *obj, QEvent *ev) override;

 private:
  void createEmulation();
  void createTerminalView();
  void bindViewToEmulation(TerminalView *terminalView);
  void updateTerminalSize();

  QImage *image;
  std::function<void()> nativeRedrawCallback;

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
  void onViewSizeChange(int height, int width);
  void onEmulationSizeChange(QSize);
  void onCursorChanged(KeyboardCursorShape cursorShape,
                       bool blinkingCursorEnabled);
  void viewDestroyed(QObject *view);
};

}  // namespace TConsole
#endif  // TERMINALEMULATOR_H
