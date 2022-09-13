#include <QApplication>

#include "terminalemulator.h"

int main(int argc, char *argv[]) {
  QApplication a(argc, argv);
  TConsole::TerminalEmulator emulator;
  emulator.initialize();
  emulator.setBackgroundColor(QColor(0x15, 0x15, 0x15));
  emulator.setForegroundColor(QColor(0xE1, 0xE1, 0xE1));
  emulator.show();

  if (argv[1]) {
    QString param = QString(argv[1]);
    QStringList li = param.split("=");
    if (li.size() == 2 && li[0] == "--with-text") {
      QString content = "";
      for (int i = 0; i < li[1].toInt(); i++) {
        content.append(
            QString::number(i) +
            "]0;root@joezane:~[root@joezane ~]#\r\n"
            "[0m[01;36mbin[0m   [01;34mdata[0m  [01;34metc[0m   "
            "[01;36mlib[0m      "
            "[01;34mlost+found[0m  [01;34mmnt[0m    [01;34mproc[0m     "
            "\r\n"
            "[01;34mroot[0m  [01;36msbin[0m  [01;34msys[0m   [01;34musr"
            "[0m[01;34mboot[0m  [01;34mdev[0m   [01;34mhome[0m  "
            "[01;36mlib64[0m  [01;34mmedia[0m\r\n");
      }
      emulator.sendText(content);
    }
  }
  return a.exec();
}
