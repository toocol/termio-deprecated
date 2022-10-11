#include <QApplication>
#include "terminal_emulator.h"

namespace nfx = nativefx;

static const QRegularExpression lfRegularExp("\n");

Qt::KeyboardModifiers transferModifiers(int);

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

  auto qtRedraw = [&image, &emulator](const std::string& name,
                                      uchar* bufferData, int w, int h) {
    if (image == NULL) {
      image = new QImage(bufferData, w, h, w * 4,
                         QImage::Format_ARGB32_Premultiplied);
      emulator.resize(w, h);
      emulator.requestRedrawImage(image);
    }
  };

  nfx::SharedCanvas* canvas = nfx::SharedCanvas::create("_emulator_mem");

  auto qtResized = [&image, &emulator](const std::string& name,
                                       uchar* bufferData, int w, int h) {
    if (image == NULL || emulator.width() != w || emulator.height() != h) {
      delete image;
      image = new QImage(bufferData, w, h, w * 4,
                         QImage::Format_ARGB32_Premultiplied);
      emulator.resize(w, h);
      emulator.requestRedrawImage(image);
    }
  };

  QWidget* prevEvtTarget = NULL;
  QPoint prevP;

  auto evt = [&emulator, &prevEvtTarget, &prevP](const std::string& name,
                                                 nfx::event* evt) {
    if (evt->type & nfx::NFX_FOCUS_EVENT) {
      nfx::focus_event* focusEvt = static_cast<nfx::focus_event*>((void*)evt);
      QEvent::Type type = focusEvt->focus ? QEvent::FocusIn : QEvent::FocusOut;
      QFocusEvent* evt = new QFocusEvent(type);
      emulator.sendSimulatedEvent(evt);
    } else if (evt->type & nfx::NFX_CREATE_SSH_SESSION_EVENT) {
      nfx::create_ssh_session_event* sshEvt =
          static_cast<nfx::create_ssh_session_event*>((void*)evt);
      std::string host = nfx::get_shared_string(sshEvt->host);
      std::string user = nfx::get_shared_string(sshEvt->user);
      std::string password = nfx::get_shared_string(sshEvt->password);

      emulator.createSshSession(sshEvt->sessionId, QString::fromStdString(host),
                                QString::fromStdString(user),
                                QString::fromStdString(password));
    } else if (evt->type & nfx::NFX_KEY_EVENT) {
      nfx::key_event* key_evt = static_cast<nfx::key_event*>((void*)evt);

      //[static] QWidget *QApplication::focusWidget()
      // Returns the application widget that has the keyboard input focus, or 0
      // if no widget in this application has the focus. See also
      // QWidget::setFocus(), QWidget::hasFocus(), activeWindow(), and
      // focusChanged().

      std::string kChars = key_evt->chars;
      QKeyEvent* qkevt = NULL;
      std::cout << "key_evt: " << key_evt->key_code << ", chars: " << kChars
                << std::endl;
      Qt::KeyboardModifiers modifiers = transferModifiers(key_evt->modifiers);
      if (key_evt->type & nfx::NFX_KEY_PRESSED) {
        qkevt = new QKeyEvent(QEvent::KeyPress, key_evt->key_code, modifiers, 0,
                              0, 0, kChars.c_str());
      } else if (key_evt->type & nfx::NFX_KEY_RELEASED) {
        qkevt = new QKeyEvent(QEvent::KeyRelease, key_evt->key_code, modifiers,
                              0, 0, 0, kChars.c_str());
      } else if (key_evt->type & nfx::NFX_KEY_TYPED) {
        qkevt = new QKeyEvent(QEvent::KeyPress, key_evt->key_code, modifiers, 0,
                              0, 0, kChars.c_str());
      } else {
        return;
      }

      QWidget* event_receiver = QApplication::focusWidget();

      if (event_receiver != NULL) {
        QApplication::sendEvent(event_receiver, qkevt);
      } else {
        emulator.sendSimulatedEvent(qkevt);
      }
    } else if (evt->type & nfx::NFX_MOUSE_EVENT) {
      nfx::mouse_event* mouse_evt = static_cast<nfx::mouse_event*>((void*)evt);

      QPoint p(mouse_evt->x, mouse_evt->y);

      Qt::MouseButton btn = Qt::NoButton;

      QWidget* widget = QApplication::widgetAt(p);
      QWidget* receiver = NULL;
      if (widget == NULL) {
        receiver = emulator.childAt(p);
      } else {
        receiver = widget->childAt(p);
      }

      if (receiver == NULL) {
        qDebug() << "Get receiver failed";
        return;
      }

      // detected mouse enter/exit events
      if (prevEvtTarget != receiver) {
        if (prevEvtTarget != NULL) {
          std::cout << "LEAVE\n";
          QMouseEvent* mEvt =
              new QMouseEvent((QEvent::Leave), prevP, Qt::NoButton,
                              Qt::NoButton, Qt::NoModifier);
          QApplication::sendEvent(prevEvtTarget, mEvt);
        }

        std::cout << "ENTER\n";
        QMouseEvent* mEvt = new QMouseEvent((QEvent::Enter), p, Qt::NoButton,
                                            Qt::NoButton, Qt::NoModifier);
        QApplication::sendEvent(receiver, mEvt);
      }

      prevEvtTarget = receiver;
      prevP = p;

      if (mouse_evt->buttons & nfx::NFX_PRIMARY_BTN) {
        btn = Qt::LeftButton;
        // std::cout << "-> btn: PRIMARY\n";
      }

      if (mouse_evt->buttons & nfx::NFX_SECONDARY_BTN) {
        btn = Qt::RightButton;
        // std::cout << "-> btn: SECONDARY\n";
      }

      if (mouse_evt->buttons & nfx::NFX_MIDDLE_BTN) {
        btn = Qt::MiddleButton;
        // std::cout << "-> btn: MIDDLE\n";
      }

      if (mouse_evt->type & nfx::NFX_MOUSE_MOVED) {
        QMouseEvent* mEvt = new QMouseEvent(
            (QEvent::MouseMove), p, Qt::NoButton, Qt::NoButton, Qt::NoModifier);
        // std::cout << "-> evt-type: MOVE\n";
        QApplication::sendEvent(receiver, mEvt);
      }

      if (mouse_evt->type & nfx::NFX_MOUSE_PRESSED) {
        QMouseEvent* mEvt = new QMouseEvent((QEvent::MouseButtonPress), p, btn,
                                            Qt::NoButton, Qt::NoModifier);
        // std::cout << "-> evt-type: PRESS\n";
        QApplication::sendEvent(receiver, mEvt);
      }

      if (mouse_evt->type & nfx::NFX_MOUSE_RELEASED) {
        QMouseEvent* mEvt = new QMouseEvent((QEvent::MouseButtonRelease), p,
                                            btn, Qt::NoButton, Qt::NoModifier);
        // std::cout << "-> evt-type: RELEASE\n";
        QApplication::sendEvent(receiver, mEvt);
      }

      if (mouse_evt->type & nfx::NFX_MOUSE_WHEEL) {
        QWheelEvent* mEvt =
            new QWheelEvent(QPointF(0, 0), QPointF(0, 0), QPoint(0, 0),
                            QPoint(0, mouse_evt->amount), Qt::NoButton,
                            Qt::NoModifier, Qt::ScrollBegin, false);
        // std::cout << "-> evt-type: RELEASE\n";
        QApplication::sendEvent(receiver, mEvt);
      }
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

  return app.exec();
}

Qt::KeyboardModifiers transferModifiers(int modifiers) {
  Qt::KeyboardModifiers trans = Qt::NoModifier;
  if (modifiers & Qt::ShiftModifier) {
    trans |= Qt::ShiftModifier;
  }
  if (modifiers & Qt::ControlModifier) {
    trans |= Qt::ControlModifier;
  }
  if (modifiers & Qt::AltModifier) {
    trans |= Qt::AltModifier;
  }
  if (modifiers & Qt::MetaModifier) {
    trans |= Qt::MetaModifier;
  }
  return trans;
}
