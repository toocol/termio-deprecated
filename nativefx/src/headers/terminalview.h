#ifndef TERMINALVIEW_H
#define TERMINALVIEW_H

#include <QClipboard>
#include <QColor>
#include <QDrag>
#include <QGridLayout>
#include <QLabel>
#include <QMouseEvent>
#include <QPixmap>
#include <QPointer>
#include <QScrollBar>
#include <QTimer>
#include <QTimerEvent>
#include <QVector>
#include <QWidget>

#include "character.h"

class QDrag;
class QDragEnterEvent;
class QDropEvent;
class QLabel;
class QTimer;
class QEvent;
class QGridLayout;
class QKeyEvent;
class QScrollBar;
class QShowEvent;
class QHideEvent;
class QTimerEvent;
class QWidget;

namespace TConsole {

enum ScrollBarPosition {
  NO_SCROLL_BAR = 0,
  SCROLL_BAR_LEFT = 1,
  SCROLL_BAR_RIGHT = 2
};

enum TripleClickMode { SELECT_WHOLE_LINE = 1, SELECT_FORWARDS_FROM_CURSOR = 2 };

enum MotionAfterPasting {
  NO_MOVE_SCREEN_WINDOW = 0,
  MOVE_START_SCREEN_WINDOW = 1,
  MOVE_END_SCREEN_WINDOW = 2
};

enum CursorShape {
  // A rectangualr block
  BLOCK_CURSOR = 0,
  // A single flat line at the bottom of the cursor character
  UNDERLINE_CURSOR = 1,
  // A cursor shaped like capital letter "I"
  IBEAM_CURSOR = 2
};

enum BackgroundMode { NONE, STRETCH, ZOOM, FIT, CENTER };

enum BellMode {
  SYSTEM_BEEP_BELL = 0,
  NOTIFY_BELL = 1,
  VISUAL_BELL = 2,
  NO_BELL = 3
};

class TerminalView : public QWidget {
  Q_OBJECT
 public:
  explicit TerminalView(QWidget* parent = nullptr);
  ~TerminalView();

  /** Returns the terminal color palette used by the display. */
  const ColorEntry* getColorTable() const;
  /** Sets the terminal color palette used by the display. */
  void setColorTable(const ColorEntry table[]);
  /**
   * Sets the seed used to generate random colors for the display
   * (in color schemes that support them).
   */
  void setRandomSeed(uint seed);
  /**
   * Returns the seed used to generate random colors for the display
   * (in color schemes that support them).
   */
  uint getRandomSeed() const;
  /** Sets the opacity of the terminal display. */
  void setOpacity(qreal opacity);
  /** Sets the background image of the terminal display. */
  void setBackgroundImage(const QString& backgroundImage);
  /** Sets the background image mode of the terminal display. */
  void setBackgroundMode(BackgroundMode mode);
  /**
   * Specifies whether the terminal display has a vertical scroll bar, and if so
   * whether it is shown on the left or right side of the display.
   */
  void setScrollBarPosition(ScrollBarPosition position);
  /**
   * Setting the current position and range of the display scroll bar.
   *
   * @brief setScroll
   * @param cursor
   * @param lines
   */
  void setScroll(int cursor, int lines);
  /**
   * Scroll to the bottom of the terminal (reset scrolling).
   */
  void scrollToEnd();
  /** Returns true if the cursor is set to blink or false otherwise. */
  bool blinkingCursor() { return hasBlinkingCursor; }
  /** Specifies whether or not the cursor blinks. */
  void setBlinkingCursor(bool blink);
  /** Specifies whether or not text can blink. */
  void setBlinkingTextEnabled(bool blink);
  void setCtrlDrag(bool enable) { ctrlDrag = enable; }
  bool isCtrlDrag() { return ctrlDrag; }

  void setSize(int cols, int lins);
  /**
   * Sets the type of effect used to alert the user when a 'bell' occurs in the
   * terminal session.
   *
   * The terminal session can trigger the bell effect by calling bell() with
   * the alert message.
   */
  void setBellMode(int mode);
  /**
   * Returns the type of effect used to alert the user when a 'bell' occurs in
   * the terminal session.
   *
   * See setBellMode()
   */
  int getBellMode() { return bellMode; }
  /**
   * Sets whether or not the current height and width of the
   * terminal in lines and columns is displayed whilst the widget
   * is being resized.
   */
  void setTerminalSizeHint(bool on) { terminalSizeHint = on; }
  /**
   * Returns whether or not the current height and width of
   * the terminal in lines and columns is displayed whilst the widget
   * is being resized.
   */
  bool isTerminalSizeHint() { return terminalSizeHint; }
  /**
   * Sets whether the terminal size display is shown briefly
   * after the widget is first shown.
   *
   * See setTerminalSizeHint() , isTerminalSizeHint()
   */
  void setTerminalSizeStartup(bool on) { terminalSizeStartup = on; }
  /**
   * Sets the status of the BiDi rendering inside the terminal display.
   * Defaults to disabled.
   */
  void setBidiEnabled(bool set) { bidiEnabled = set; }
  /**
   * Returns the status of the BiDi rendering in this widget.
   */
  bool isBidiEnabled() { return bidiEnabled; }
  /** Sets how the text is selected when the user triple clicks within the
   * display. */
  void setTripleClickMode(TripleClickMode mode) { tripleClickMode = mode; }
  /** See setTripleClickSelectionMode() */
  TripleClickMode getTripleClickMode() { return tripleClickMode; }

 protected:
  void paintEvent(QPaintEvent*) override;

  void showEvent(QShowEvent*) override;
  void hideEvent(QHideEvent*) override;
  void resizeEvent(QResizeEvent*) override;

  virtual void fontChange(const QFont& font);
  void focusInEvent(QFocusEvent* event) override;
  void focusOutEvent(QFocusEvent* event) override;
  void keyPressEvent(QKeyEvent* event) override;
  void mouseDoubleClickEvent(QMouseEvent* ev) override;
  void mousePressEvent(QMouseEvent*) override;
  void mouseReleaseEvent(QMouseEvent*) override;
  void mouseMoveEvent(QMouseEvent*) override;
  virtual void extendSelection(const QPoint& pos);
  void wheelEvent(QWheelEvent*) override;

  bool focusNextPrevChild(bool next) override;

  // drag and drop
  void dragEnterEvent(QDragEnterEvent* event) override;
  void dropEvent(QDropEvent* event) override;
  void doDrag();

  enum DragState { DI_NONE, DI_PENDING, DI_DRAGGING };
  struct _dragInfo {
    DragState state;
    QPoint start;
    QDrag* dragObject;
  } dragInfo;

 signals:
  void useMouseChanged();
  void changedContentSizeSignal(int height, int width);

 public slots:
  /**
   * Sets whether the program whoose output is being displayed in the view
   * is interested in mouse events.
   *
   * @brief setUsesMouse
   * @param on whether the use mouse is on
   */
  void setUsesMouse(bool on);
  /**
   *
   * @brief setBracketedPasteMode
   * @param on
   */
  void setBracketedPasteMode(bool on);
  /**
   * Sets the background of the display to the specified color.
   * @see setColorTable(), setForegroundColor()
   */
  void setBackgroundColor(const QColor& color);

  /**
   * Sets the text of the display to the specified color.
   * @see setColorTable(), setBackgroundColor()
   */
  void setForegroundColor(const QColor& color);

 protected slots:
  void scrollBarPositionChanged(int value);
  void blinkEvent();
  void blinkCursorEvent();

  // Renables bell noises and visuals.  Used to disable further bells for a
  // short period of time after emitting the first in a sequence of bell events.
  void enableBell();

  void clearImage();

 private:
  // determine the width of this text.
  int textWidth(int startColumn, int length, int line) const;
  // determine the area that encloses this series of characters.
  QRect calculateTextArea(int topLeftX, int topLeftY, int startColumn, int line,
                          int length);
  // split display contents by rect into fragment according to their colors and
  // styles and call drawTextFragment() to draw.
  void drawContents(QPainter& painter, const QRect& rect);
  // draw content's fragments.
  void drawTextFragment(QPainter& painter, const QRect& rect,
                        const std::wstring& text, const Character* style);
  // draws the background for a text fragment
  // if useOpacitySetting is true then the color's alpha value will be set to
  // the display's transparency (set with setOpacity()), otherwise the
  // background will be drawn fully opaque
  void drawBackground(QPainter& painter, const QRect& rect, const QColor& color,
                      bool useOpacitySetting);
  // draws the cursor character
  void drawCursor(QPainter& painter, const QRect& rect,
                  const QColor& foregroundColor, const QColor& backgroundColor,
                  bool& invertColors);
  // draws the characters or line graphics in a text fragment
  void drawCharacters(QPainter& painter, const QRect& rect,
                      const std::wstring& text, const Character* style,
                      bool invertCharacterColor);
  // draws a string of line graphics
  void drawLineCharString(QPainter& painter, int x, int y,
                          const std::wstring& str,
                          const Character* attributes) const;
  // draws the preedit string for input methods
  void drawInputMethodPreeditString(QPainter& painter, const QRect& rect);

  void paintFilters(QPainter& painter);

  // the area where the preedit string for input methods will be draw
  QRect preeditRect() const;
  bool isLineChar(wchar_t c) const;
  bool isLineCharString(const std::wstring& string) const;
  // shows a notification window in the middle of the widget indicating the
  // terminal's current size in columns and lines
  void showResizeNotification();

  // returns the position of the cursor in columns and lines
  QPoint cursorPosition() const;

  void calcGeometry();
  void propagateSize();
  void updateImageSize();
  void makeImage();

  QGridLayout* gridLayout;
  bool allowBell;
  // Whether intense colors should be bold.
  bool boldIntense;
  // Whether is test mode.
  bool testFlag;

  // whether has fixed pitch.
  bool fixedFont;
  int fontHeight;
  int fontWidth;
  int fontAscend;
  int drawTextAdditionHeight;
  int leftMargin;
  int topMargin;

  int leftBaseMargin;
  int topBaseMargin;

  // The total number of lines that can be displayed in the view;
  int lines;
  // The total number of columns that can be displayed in the view;
  int columns;

  int usedLines;
  int usedColumns;

  int contentHeight;
  int contentWidth;

  Character* image;
  int imageSize;

  QVector<LineProperty> lineProperties;

  ColorEntry colorTable[TABLE_COLORS];
  uint randomSeed;

  bool resizing;
  bool terminalSizeHint;
  bool terminalSizeStartup;
  bool bidiEnabled;
  bool mouseMarks;
  bool bracketedPasteMode;
  bool disabledBracketedPasteMode;

  // initial selection point
  QPoint iPntSel;
  // current selection point
  QPoint pntSel;
  // help avoid flicker
  QPoint tripleSelBegin;
  // selection state
  int actSel;
  bool wordSelectionMode;
  bool lineSelectionMode;
  bool preserveLineBreaks;
  bool columnSelectionMode;

  QClipboard* clipboard;
  QScrollBar* scrollBar;
  ScrollBarPosition scrollbarLocation;
  QString wordCharacters;
  int bellMode;

  // hide text in paint event.
  bool blinking;
  // has character to blink.
  bool hasBlinker;
  // hide cursor in paint event.
  bool cursorBlinking;
  // has bliking cursor enable.
  bool hasBlinkingCursor;
  // allow text to blink.
  bool allowBlinkingText;
  // require Ctrl key for drag
  bool ctrlDrag;
  // columns/lines are locked.
  bool isFixedSize;
  // set in mouseDoubleClickEvent and delete after
  // QApplication::doubleClickInterval() delay.
  bool possibleTripleClick;
  TripleClickMode tripleClickMode;
  QTimer* blinkTimer;
  QTimer* blinkCursorTimer;

  // true during visual bell
  bool colorsInverted;

  QLabel* resizeWidget;
  QTimer* resizeTimer;

  QLabel* outputSuspendedLabel;

  uint lineSpacing;
  qreal opacity;
  QSize size;

  QPixmap backgroundImage;
  BackgroundMode backgroundMode;

  CursorShape cursorShape;
  QColor cursorColor;

  MotionAfterPasting motionAfterPasting;
  bool confirmMultilinePaster;
  bool trimPastedTrailingNewLines;

  struct InputMethodData {
    std::wstring preeditString;
    QRect previousPreeditRect;
  };
  InputMethodData inputMethodData;

  bool drawLineChars;
};

class AutoScrollHandler : public QObject {
  Q_OBJECT

 public:
  AutoScrollHandler(QWidget* parent);

 protected:
  void timerEvent(QTimerEvent* event) override;
  bool eventFilter(QObject* watched, QEvent* event) override;

 private:
  QWidget* widget() const { return static_cast<QWidget*>(parent()); }
  int _timerId;
};

}  // namespace TConsole

#endif  // TERMINALVIEW_H
