#ifndef TERMINALEMULATOR_H
#define TERMINALEMULATOR_H

#include <QTextBlock>
#include <QTextEdit>
#include <QWidget>

class TerminalEmulator : public QWidget {
  Q_OBJECT
 private:
  QTextEdit *textDisplay;

 public:
  TerminalEmulator(QWidget *parent = nullptr);
  ~TerminalEmulator();
};
#endif  // TERMINALEMULATOR_H
