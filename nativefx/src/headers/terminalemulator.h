#ifndef TERMINALEMULATOR_H
#define TERMINALEMULATOR_H

#include <QPainter>
#include <QTextBlock>
#include <QTextDocument>
#include <QTextEdit>
#include <QTextLayout>
#include <QWidget>

#include "terminalview.h"

namespace TConsole {

class TerminalEmulator : public QWidget {
  Q_OBJECT
 private:
  TerminalView *terminalView;

 public:
  TerminalEmulator(QWidget *parent = nullptr);
  ~TerminalEmulator();

  void draw();

 protected:
  void paintEvent(QPaintEvent *e) override;
};

}  // namespace TConsole
#endif  // TERMINALEMULATOR_H
