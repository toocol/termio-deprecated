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

 private:
  void createTerminalView();

  TerminalView *_terminalView;

  Emulation *_emulation;
  QVBoxLayout *_mainLayout;
};

}  // namespace TConsole
#endif  // TERMINALEMULATOR_H
