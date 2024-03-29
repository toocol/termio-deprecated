#include <QApplication>
#include <QList>

#include "terminal_emulator.h"

namespace nrs = nativers;

static const QRegularExpression lfRegularExp("\n");

Qt::KeyboardModifiers transferModifiers(int);

int main(int argc, char* argv[]) {
  if (!argv[1] || QString(argv[1]) != "--initialize-flag") {
    return -1;
  }

  QApplication app(argc, argv, false);
  QImage* primary_image = NULL;
  QImage* secondary_image = NULL;

  TConsole::TerminalEmulator emulator;
  emulator.resize(1280, 800);
  emulator.initialize();
  //  emulator.setBackgroundColor(QColor(0x15, 0x15, 0x15));
  //  emulator.setForegroundColor(QColor(0xE1, 0xE1, 0xE1));
  emulator.setBackgroundColor(QColor(0xFA, 0xFA, 0xFA));
  emulator.setForegroundColor(QColor(0x38, 0x38, 0x38));
  emulator.setBlinkingCursor(true);

  auto qtRedraw = [&primary_image, &secondary_image, &emulator](
                      const std::string& name, uchar* primaryBufferData,
                      uchar* secondaryBufferData, int w, int h) {
    if (primary_image == NULL || secondary_image == NULL) {
      primary_image = new QImage(primaryBufferData, w, h, w * 4,
                                 QImage::Format_ARGB32_Premultiplied);
      secondary_image = new QImage(secondaryBufferData, w, h, w * 4,
                                   QImage::Format_ARGB32_Premultiplied);
      emulator.resize(w, h);
      emulator.requestRedrawImage(primary_image, secondary_image);
    }
  };

  QList<QScreen*> screens = QApplication::screens();
  QList<QScreen*>::iterator i;
  int maxWidth = 0, maxHeight = 0;
  for (i = screens.begin(); i != screens.end(); i++) {
    QRect rect = (*i)->geometry();
    maxWidth = max(maxWidth, rect.width());
    maxHeight = max(maxHeight, rect.height());
  }
  nrs::SharedCanvas* canvas =
      nrs::SharedCanvas::create("_emulator_mem", maxWidth, maxHeight);

  auto qtResized = [&primary_image, &secondary_image, &emulator](
                       const std::string& name, uchar* primaryBufferData,
                       uchar* secondaryBufferData, int w, int h) {
    if (primary_image == NULL || secondary_image == NULL ||
        emulator.width() != w || emulator.height() != h) {
      delete primary_image;
      delete secondary_image;
      primary_image = new QImage(primaryBufferData, w, h, w * 4,
                                 QImage::Format_ARGB32_Premultiplied);
      secondary_image = new QImage(secondaryBufferData, w, h, w * 4,
                                   QImage::Format_ARGB32_Premultiplied);
      emulator.resize(w, h);
      emulator.requestRedrawImage(primary_image, secondary_image);
    }
  };

  QWidget* prevEvtTarget = NULL;
  QPoint prevP;

  auto evt = [&emulator, &prevEvtTarget, &prevP](const std::string& name,
                                                 nrs::event* evt) {
    if (evt->type & nrs::NRS_FOCUS_EVENT) {
      nrs::focus_event* focusEvt = static_cast<nrs::focus_event*>((void*)evt);
      QEvent::Type type = focusEvt->focus ? QEvent::FocusIn : QEvent::FocusOut;
      QFocusEvent* evt = new QFocusEvent(type);
      emulator.sendSimulatedEvent(evt);
      qDebug() << "Request focus " << focusEvt->focus;
    } else if (evt->type & nrs::NRS_CREATE_SSH_SESSION_EVENT) {
      nrs::create_ssh_session_event* sshEvt =
          static_cast<nrs::create_ssh_session_event*>((void*)evt);
      std::string host = nrs::get_shared_string(sshEvt->host);
      std::string user = nrs::get_shared_string(sshEvt->user);
      std::string password = nrs::get_shared_string(sshEvt->password);

      emulator.createSshSession(sshEvt->sessionId, QString::fromStdString(host),
                                QString::fromStdString(user),
                                QString::fromStdString(password));
    } else if (evt->type & nrs::NRS_SHELL_STARTUP) {
      nrs::shell_startup_event* shellStartEvt =
          static_cast<nrs::shell_startup_event*>((void*)evt);
      std::string param = nrs::get_shared_string(shellStartEvt->param);

      emulator.shellStartupSession(shellStartEvt->sessionId,
                                   QString::fromStdString(param));
    } else if (evt->type & nrs::NRS_KEY_EVENT) {
      nrs::key_event* key_evt = static_cast<nrs::key_event*>((void*)evt);

      //[static] QWidget *QApplication::focusWidget()
      // Returns the application widget that has the keyboard input focus, or 0
      // if no widget in this application has the focus. See also
      // QWidget::setFocus(), QWidget::hasFocus(), activeWindow(), and
      // focusChanged().

      std::string kChars = key_evt->chars;
      QKeyEvent* qkevt = NULL;
      //      std::cout << "key_evt: " << key_evt->key_code << ", chars: " <<
      //      kChars
      //                << std::endl;
      Qt::KeyboardModifiers modifiers = transferModifiers(key_evt->modifiers);
      if (key_evt->type & nrs::NRS_KEY_PRESSED) {
        qkevt = new QKeyEvent(QEvent::KeyPress, key_evt->key_code, modifiers, 0,
                              0, 0, kChars.c_str());
      } else if (key_evt->type & nrs::NRS_KEY_RELEASED) {
        qkevt = new QKeyEvent(QEvent::KeyRelease, key_evt->key_code, modifiers,
                              0, 0, 0, kChars.c_str());
      } else if (key_evt->type & nrs::NRS_KEY_TYPED) {
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
    } else if (evt->type & nrs::NRS_MOUSE_EVENT) {
      nrs::mouse_event* mouse_evt = static_cast<nrs::mouse_event*>((void*)evt);

      QPoint p(mouse_evt->x, mouse_evt->y);

      Qt::MouseButton btn = Qt::NoButton;

      QWidget* receiver = NULL;
      receiver = emulator.childAt(p);
      bool flag = false;

      if (receiver == NULL) {
        flag = true;
      }

      // detected mouse enter/exit events
      if (mouse_evt->type & nrs::NRS_MOUSE_ENTERED) {
        qDebug() << "ENTER";
        QMouseEvent* mEvt = new QMouseEvent((QEvent::Enter), p, Qt::NoButton,
                                            Qt::NoButton, Qt::NoModifier);
        if (flag) {
          emulator.sendSimulatedEvent(mEvt);
        } else {
          QApplication::sendEvent(receiver, mEvt);
        }
        return;
      }
      if (mouse_evt->type & nrs::NRS_MOUSE_EXITED) {
        qDebug() << "LEAVE";
        QMouseEvent* mEvt = new QMouseEvent(
            (QEvent::Leave), prevP, Qt::NoButton, Qt::NoButton, Qt::NoModifier);
        if (flag) {
          emulator.sendSimulatedEvent(mEvt);
        } else {
          QApplication::sendEvent(receiver, mEvt);
        }
        return;
      }

      prevEvtTarget = receiver;
      prevP = p;

      if (mouse_evt->buttons & nrs::NRS_PRIMARY_BTN) {
        btn = Qt::LeftButton;
        // std::cout << "-> btn: PRIMARY\n";
      }

      if (mouse_evt->buttons & nrs::NRS_SECONDARY_BTN) {
        btn = Qt::RightButton;
        // std::cout << "-> btn: SECONDARY\n";
      }

      if (mouse_evt->buttons & nrs::NRS_MIDDLE_BTN) {
        btn = Qt::MiddleButton;
        // std::cout << "-> btn: MIDDLE\n";
      }

      if (mouse_evt->type & nrs::NRS_MOUSE_MOVED) {
        QMouseEvent* mEvt = new QMouseEvent(
            (QEvent::MouseMove), p, Qt::NoButton, Qt::NoButton, Qt::NoModifier);
        //        std::cout << "-> evt-type: MOVE\n";
        if (flag) {
          emulator.sendSimulatedEvent(mEvt);
        } else {
          QApplication::sendEvent(receiver, mEvt);
        }
      }

      if (mouse_evt->type & nrs::NRS_MOUSE_PRESSED) {
        QEvent::Type type;
        switch (mouse_evt->click_count) {
          case 2:
            type = QEvent::MouseButtonDblClick;
            break;
          default:
            type = QEvent::MouseButtonPress;
            break;
        }
        QMouseEvent* mEvt =
            new QMouseEvent(type, p, btn, Qt::NoButton, Qt::NoModifier);
        qDebug() << "-> evt-type: PRESS " << mouse_evt->click_count;
        if (flag) {
          emulator.sendSimulatedEvent(mEvt);
        } else {
          QApplication::sendEvent(receiver, mEvt);
        }
      }

      if (mouse_evt->type & nrs::NRS_MOUSE_RELEASED) {
        QMouseEvent* mEvt = new QMouseEvent((QEvent::MouseButtonRelease), p,
                                            btn, Qt::NoButton, Qt::NoModifier);
        qDebug() << "-> evt-type: RELEASE";
        if (flag) {
          emulator.sendSimulatedEvent(mEvt);
        } else {
          QApplication::sendEvent(receiver, mEvt);
        }
      }

      if (mouse_evt->type & nrs::NRS_MOUSE_WHEEL) {
        QWheelEvent* mEvt =
            new QWheelEvent(QPointF(0, 0), QPointF(0, 0), QPoint(0, 0),
                            QPoint(0, mouse_evt->amount), Qt::NoButton,
                            Qt::NoModifier, Qt::ScrollBegin, false);
        qDebug() << "-> evt-type: WHELL";
        emulator.sendSimulatedEvent(mEvt);
      }
    }
  };

  auto nativeRedrawCallback = [&canvas, &qtRedraw]() {
    canvas->draw(qtRedraw);
  };
  emulator.setNativeRedrawCallback(nativeRedrawCallback);

  auto nativeEvtCallback = [&canvas, &emulator, &evt, &qtResized]() {
    canvas->resize(qtResized);
    canvas->processEvents(evt);

    std::string resp = "";
    QString text = QString::fromUtf8(canvas->getSharedString().c_str());
    int sharedType = canvas->sharedStringType();
    if (sharedType == nativers::NRS_SEND_TEXT && text != "") {
      emulator.sendText(text.replace(lfRegularExp, "\r\n"));
    } else if (sharedType == nativers::NRS_REQUEST_SIZE) {
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
  emulator.setAttribute(Qt::WA_DontShowOnScreen, true);

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
