#ifndef EMULATION_H
#define EMULATION_H

#include <QObject>

#include "screen.h"
#include "screenwindow.h"
#include "terminalview.h"

namespace TConsole {
class Emulation : public QObject {
  Q_OBJECT
 public:
  explicit Emulation(QWidget *parent = nullptr);

  ScreenWindow *createWindow();

  bool programUseMouse();
  void setUseMouse(bool on);

  bool programBracketedPasteMode();
  void setBracketedPasteMode(bool on);

  /**
   * Returns true if the active terminal program wants
   * mouse input events.
   *
   * The programUsesMouseChanged() signal is emitted when this
   * changes.
   */
  bool programUsesMouse() const;

  bool programBracketedPasteMode() const;

 private:
  QList<ScreenWindow *> _windows;

  // current active screen
  Screen *_currentScreen;
  // 0 = primary screen
  // 1 = alternate (used by vi,emocs etc. scrollBar is not enable in this mode)
  Screen *_screen[2];

  bool _useMouse;
  bool _bracketedPasteMode;
  QTimer _bulkTimer1;
  QTimer _bulkTimer2;
 signals:
  /**
   * Emitted when the contents of the screen image change.
   * The emulation buffers the updates from successive image changes,
   * and only emits outputChanged() at sensible intervals when
   * there is a lot of terminal activity.
   *
   * Normally there is no need for objects other than the screen windows
   * created with createWindow() to listen for this signal.
   *
   * ScreenWindow objects created using createWindow() will emit their
   * own outputChanged() signal in response to this signal.
   */
  void outputChanged();
  /**
   * Emitted when the cursor shape or its blinking state is changed via
   * DECSCUSR sequences.
   *
   * @param cursorShape One of 3 possible values in KeyboardCursorShape enum
   * @param blinkingCursorEnabled Whether to enable blinking or not
   */
  void cursorChanged(CursorShape cursorShape, bool blinkingCursorEnabled);
  /**
   * Emitted when the program running in the terminal wishes to update the
   * session's title.  This also allows terminal programs to customize other
   * aspects of the terminal emulation display.
   *
   * This signal is emitted when the escape sequence "\033]ARG;VALUE\007"
   * is received in the input string, where ARG is a number specifying what
   * should change and VALUE is a string specifying the new value.
   *
   * TODO:  The name of this method is not very accurate since this method
   * is used to perform a whole range of tasks besides just setting
   * the user-title of the session.
   *
   * @param title Specifies what to change.
   * <ul>
   * <li>0 - Set window icon text and session title to @p newTitle</li>
   * <li>1 - Set window icon text to @p newTitle</li>
   * <li>2 - Set session title to @p newTitle</li>
   * <li>11 - Set the session's default background color to @p newTitle,
   *         where @p newTitle can be an HTML-style string ("#RRGGBB") or a
   * named color (eg 'red', 'blue'). See
   * http://doc.trolltech.com/4.2/qcolor.html#setNamedColor for more details.
   * </li>
   * <li>31 - Supposedly treats @p newTitle as a URL and opens it (NOT
   * IMPLEMENTED)</li> <li>32 - Sets the icon associated with the session.  @p
   * newTitle is the name of the icon to use, which can be the name of any icon
   * in the current KDE icon theme (eg: 'konsole', 'kate', 'folder_home')</li>
   * </ul>
   * @param newTitle Specifies the new title
   */
  void titleChanged(int title, const QString &newTitle);

  /**
   * This is emitted when the program running in the shell indicates whether or
   * not it is interested in mouse events.
   *
   * @param usesMouse This will be true if the program wants to be informed
   * about mouse events or false otherwise.
   */
  void programUsesMouseChanged(bool usesMouse);

  void programBracketedPasteModeChanged(bool bracketedPasteMode);

 private slots:
  /**
   * Schedules an update of attached views.
   * Repeated calls to bufferedUpdate() in close succession will result in only
   * a single update, much like the Qt buffered update of widgets.
   */
  void bufferedUpdate();
  // triggered by timer, causes the emulation to send an updated screen image to
  // each view
  void showBulk();

  void usesMouseChanged(bool usesMouse);

  void bracketedPasteModeChanged(bool bracketedPasteMode);
};
}  // namespace TConsole

#endif  // EMULATION_H
