#ifndef TERMINALEMULATOR_H
#define TERMINALEMULATOR_H

#include <QPainter>
#include <QTextBlock>
#include <QTextDocument>
#include <QTextEdit>
#include <QTextLayout>
#include <QWidget>

#include "screen.h"
#include "terminalview.h"

namespace TConsole {

class TerminalEmulator : public QWidget {
  Q_OBJECT
 public:
  TerminalEmulator(QWidget *parent = nullptr);
  ~TerminalEmulator();

  void initialize();
  ScreenWindow *createWindow();

  bool programUseMouse();
  void setUseMouse(bool on);

  bool programBracketedPasteMode();
  void setBracketedPasteMode(bool on);

 protected:
  void paintEvent(QPaintEvent *e) override;

 private:
  TerminalView *_terminalView;
  QVBoxLayout *_mainLayout;

  QList<ScreenWindow *> _windows;

  // current active screen
  Screen *_currentScreen;
  // 0 = primary screen
  // 1 = alternate (used by vi,emocs etc. scrollBar is not enable in this mode)
  Screen *_screen[2];

  bool _useMouse;
  bool _bracketedPasteMode;
};

}  // namespace TConsole
#endif  // TERMINALEMULATOR_H
