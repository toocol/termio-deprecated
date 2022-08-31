#include "terminalemulator.h"

#include <QApplication>

int main(int argc, char *argv[])
{
    QApplication a(argc, argv);
    TerminalEmulator w;
    w.show();
    return a.exec();
}
