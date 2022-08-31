#ifndef TERMINALEMULATOR_H
#define TERMINALEMULATOR_H

#include <QWidget>

class TerminalEmulator : public QWidget
{
    Q_OBJECT

public:
    TerminalEmulator(QWidget *parent = nullptr);
    ~TerminalEmulator();
};
#endif // TERMINALEMULATOR_H
