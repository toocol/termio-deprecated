#include <QApplication>

#include "terminalemulator.h"

namespace nfx = nativefx;

static const QRegularExpression lfRegularExp("\n");

int main(int argc, char* argv[]) {
  if (!argv[1] || QString(argv[1]) != "--initialize-flag") {
    return -1;
  }

  QApplication app(argc, argv, false);
  QImage* image = NULL;

  TConsole::TerminalEmulator emulator;
  emulator.resize(1280, 800);
  emulator.initialize();
  emulator.setBackgroundColor(QColor(0x15, 0x15, 0x15));
  emulator.setForegroundColor(QColor(0xE1, 0xE1, 0xE1));
  emulator.setBlinkingCursor(true);

  auto qtRedraw = [&image, &emulator](std::string const& name,
                                      uchar* bufferData, int w, int h) {
    if (image == NULL) {
      qDebug() << "Redraw width: " << w << ", height: " << h;
      image = new QImage(bufferData, w, h, w * 4,
                         QImage::Format_ARGB32_Premultiplied);
      emulator.resize(w, h);
      emulator.requestRedrawImage(image);
    }
  };

  nfx::SharedCanvas* canvas = nfx::SharedCanvas::create("_emulator_mem");

  auto qtResized = [&image, &emulator](std::string const& name,
                                       uchar* bufferData, int w, int h) {
    if (image == NULL || emulator.width() != w || emulator.height() != h) {
      delete image;
      image = new QImage(bufferData, w, h, w * 4,
                         QImage::Format_ARGB32_Premultiplied);
      emulator.resize(w, h);
      emulator.requestRedrawImage(image);
    }
  };

  auto evt = [&emulator](std::string const& name, nfx::event* evt) {
    if (evt->type == nfx::NFX_FOCUS_EVENT) {
      nfx::focus_event* focusEvt = static_cast<nfx::focus_event*>((void*)evt);
      emulator.requestFocus(focusEvt->focus);
    } else if (evt->type == nfx::NFX_CREATE_SSH_SESSION_EVENT) {
      nfx::create_ssh_session_event* sshEvt =
          static_cast<nfx::create_ssh_session_event*>((void*)evt);
      std::string host = nfx::get_shared_string(sshEvt->host);
      std::string user = nfx::get_shared_string(sshEvt->user);
      std::string password = nfx::get_shared_string(sshEvt->password);

      emulator.createSshSession(sshEvt->sessionId, QString::fromStdString(host),
                                QString::fromStdString(user),
                                QString::fromStdString(password));
    }
  };

  auto nativeRedrawCallback = [&canvas, &qtRedraw, &qtResized]() {
    canvas->draw(qtRedraw, qtResized);
  };
  emulator.setNativeRedrawCallback(nativeRedrawCallback);

  auto nativeEvtCallback = [&canvas, &emulator, &evt]() {
    canvas->processEvents(evt);

    std::string resp = "";
    QString text = QString::fromUtf8(canvas->getSharedString().c_str());
    int sharedType = canvas->sharedStringType();
    if (sharedType == nativefx::NFX_SEND_TEXT && text != "") {
      emulator.sendText(text.replace(lfRegularExp, "\r\n"));
    } else if (sharedType == nativefx::NFX_REQUEST_SIZE) {
      resp = "125x80";
    }
    canvas->responseSharedString(resp);
  };
  emulator.setNativeEvtCallback(nativeEvtCallback);

  emulator.setNativeCanvas(canvas);
  emulator.setAttribute(Qt::WA_OpaquePaintEvent, true);
  emulator.setAttribute(Qt::WA_DontCreateNativeAncestors, true);
  emulator.setAttribute(Qt::WA_NativeWindow, true);
  emulator.setAttribute(Qt::WA_NoSystemBackground, true);
  emulator.setAutoFillBackground(false);

  // don't show the native window
  // we could reuse this to offer optional fullscreen mode
  emulator.setAttribute(Qt::WA_DontShowOnScreen, false);

  emulator.installEventFilter(&emulator);
  emulator.show();

  if (argv[2]) {
    QString param = QString(argv[2]);
    QStringList li = param.split("=");
    if (li.size() == 2 && li[0] == "--with-text") {
      QString content = "";
      for (int i = 0; i < li[1].toInt(); i++) {
        content.append(
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

  return app.exec();
}
