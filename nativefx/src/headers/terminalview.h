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
#include "filter.h"
#include "screenwindow.h"

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

enum KeyboardCursorShape {
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
  uint randomSeed() const;
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

  /**
   * Returns the display's filter chain.  When the image for the display is
   * updated, the text is passed through each filter in the chain.  Each filter
   * can define hotspots which correspond to certain strings (such as URLs or
   * particular words). Depending on the type of the hotspots created by the
   * filter ( returned by Filter::Hotspot::type() ) the view will draw visual
   * cues such as underlines on mouse-over for links or translucent rectangles
   * for markers.
   *
   * To add a new filter to the view, call:
   *      viewWidget->filterChain()->addFilter( filterObject );
   */
  FilterChain* filterChain() const;
  /**
   * Updates the filters in the display's filter chain.  This will cause
   * the hotspots to be updated to match the current image.
   *
   * WARNING:  This function can be expensive depending on the
   * image size and number of filters in the filterChain()
   *
   * TODO - This API does not really allow efficient usage.  Revise it so
   * that the processing can be done in a better way.
   *
   * eg:
   *      - Area of interest may be known ( eg. mouse cursor hovering
   *      over an area )
   */
  void processFilters();
  /**
   * Returns a list of menu actions created by the filters for the content
   * at the given @p position.
   */
  QList<QAction*> filterActions(const QPoint& position);

  /** Returns true if the cursor is set to blink or false otherwise. */
  bool blinkingCursor() { return _hasBlinkingCursor; }
  /** Specifies whether or not the cursor blinks. */
  void setBlinkingCursor(bool blink);
  /** Specifies whether or not text can blink. */
  void setBlinkingTextEnabled(bool blink);
  void setCtrlDrag(bool enable) { _ctrlDrag = enable; }
  bool ctrlDrag() { return _ctrlDrag; }

  /** Sets how the text is selected when the user triple clicks within the
   * display. */
  void setTripleClickMode(TripleClickMode mode) { _tripleClickMode = mode; }
  /** See setTripleClickSelectionMode() */
  TripleClickMode getTripleClickMode() { return _tripleClickMode; }

  void setLineSpacing(uint);
  void setMargin(int);

  int margin() const;
  uint lineSpacing() const;

  void emitSelection(bool useXselection, bool appendReturn);

  /** change and wrap text corresponding to paste mode **/
  void bracketText(QString& text) const;

  /**
   * Sets the shape of the keyboard cursor.  This is the cursor drawn
   * at the position in the terminal where keyboard input will appear.
   *
   * In addition the terminal display widget also has a cursor for
   * the mouse pointer, which can be set using the QWidget::setCursor()
   * method.
   *
   * Defaults to BlockCursor
   */
  void setKeyboardCursorShape(KeyboardCursorShape shape);
  /**
   * Returns the shape of the keyboard cursor.  See setKeyboardCursorShape()
   */
  KeyboardCursorShape keyboardCursorShape() const;
  /**
   * Sets the color used to draw the keyboard cursor.
   *
   * The keyboard cursor defaults to using the foreground color of the character
   * underneath it.
   *
   * @param useForegroundColor If true, the cursor color will change to match
   * the foreground color of the character underneath it as it is moved, in this
   * case, the @p color parameter is ignored and the color of the character
   * under the cursor is inverted to ensure that it is still readable.
   * @param color The color to use to draw the cursor.  This is only taken into
   * account if @p useForegroundColor is false.
   */
  void setKeyboardCursorColor(bool useForegroundColor, const QColor& color);
  /**
   * Returns the color of the keyboard cursor, or an invalid color if the
   * keyboard cursor color is set to change according to the foreground color of
   * the character underneath it.
   */
  QColor keyboardCursorColor() const;

  /**
   * Returns the number of lines of text which can be displayed in the widget.
   *
   * This will depend upon the height of the widget and the current font.
   * See fontHeight()
   */
  int lines() { return _lines; }
  /**
   * Returns the number of characters of text which can be displayed on
   * each line in the widget.
   *
   * This will depend upon the width of the widget and the current font.
   * See fontWidth()
   */
  int columns() { return _columns; }

  /**
   * Returns the height of the characters in the font used to draw the text in
   * the display.
   */
  int fontHeight() { return _fontHeight; }
  /**
   * Returns the width of the characters in the display.
   * This assumes the use of a fixed-width font.
   */
  int fontWidth() { return _fontWidth; }

  void setSize(int cols, int lins);
  void setFixedSize(int cols, int lins);

  // reimplemented
  QSize sizeHint() const override;

  /**
   * Sets which characters, in addition to letters and numbers,
   * are regarded as being part of a word for the purposes
   * of selecting words in the display by double clicking on them.
   *
   * The word boundaries occur at the first and last characters which
   * are either a letter, number, or a character in @p wc
   *
   * @param wc An array of characters which are to be considered parts
   * of a word ( in addition to letters and numbers ).
   */
  void setWordCharacters(const QString& wc);
  /**
   * Returns the characters which are considered part of a word for the
   * purpose of selecting words in the display with the mouse.
   *
   * @see setWordCharacters()
   */
  QString wordCharacters() { return _wordCharacters; }

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
  int bellMode() { return _bellMode; }

  void setSelection(const QString& t);

  /**
   * Reimplemented.  Has no effect.  Use setVTFont() to change the font
   * used to draw characters in the display.
   */
  virtual void setFont(const QFont&);

  /** Returns the font used to draw characters in the display */
  QFont getVTFont() { return font(); }

  /**
   * Sets the font used to draw the display.  Has no effect if @p font
   * is larger than the size of the display itself.
   */
  void setVTFont(const QFont& font);

  /**
   * Specified whether anti-aliasing of text in the terminal display
   * is enabled or not.  Defaults to enabled.
   */
  static void setAntialias(bool antialias) { _antialiasText = antialias; }
  /**
   * Returns true if anti-aliasing of text in the terminal is enabled.
   */
  static bool antialias() { return _antialiasText; }

  /**
   * Specify whether line chars should be drawn by ourselves or left to
   * underlying font rendering libraries.
   */
  void setDrawLineChars(bool drawLineChars) { _drawLineChars = drawLineChars; }

  /**
   * Specifies whether characters with intense colors should be rendered
   * as bold. Defaults to true.
   */
  void setBoldIntense(bool value) { _boldIntense = value; }
  /**
   * Returns true if characters with intense colors are rendered in bold.
   */
  bool getBoldIntense() { return _boldIntense; }

  /**
   * Sets whether or not the current height and width of the
   * terminal in lines and columns is displayed whilst the widget
   * is being resized.
   */
  void setTerminalSizeHint(bool on) { _terminalSizeHint = on; }
  /**
   * Returns whether or not the current height and width of
   * the terminal in lines and columns is displayed whilst the widget
   * is being resized.
   */
  bool terminalSizeHint() { return _terminalSizeHint; }
  /**
   * Sets whether the terminal size display is shown briefly
   * after the widget is first shown.
   *
   * See setTerminalSizeHint() , isTerminalSizeHint()
   */
  void setTerminalSizeStartup(bool on) { _terminalSizeStartup = on; }
  /**
   * Sets the status of the BiDi rendering inside the terminal display.
   * Defaults to disabled.
   */
  void setBidiEnabled(bool set) { _bidiEnabled = set; }
  /**
   * Returns the status of the BiDi rendering in this widget.
   */
  bool isBidiEnabled() { return _bidiEnabled; }

  /**
   * Sets the terminal screen section which is displayed in this widget.
   * When updateImage() is called, the display fetches the latest character
   * image from the the associated terminal screen window.
   *
   * In terms of the model-view paradigm, the ScreenWindow is the model which is
   * rendered by the TerminalView.
   */
  void setScreenWindow(ScreenWindow* window);
  /** Returns the terminal screen section which is displayed in this widget.
   * See setScreenWindow() */
  ScreenWindow* getScreenWindow() const;

  static bool HAVE_TRANSPARENCY;

  void setMotionAfterPasting(MotionAfterPasting action);
  int motionAfterPasting();
  void setConfirmMultilinePaste(bool confirmMultilinePaste);
  void setTrimPastedTrailingNewlines(bool trimPastedTrailingNewlines);

  // maps a point on the widget to the position ( ie. line and column )
  // of the character at that point.
  void getCharacterPosition(const QPointF& widgetPoint, int& line,
                            int& column) const;

  void disableBracketedPasteMode(bool disable) {
    _disabledBracketedPasteMode = disable;
  }
  bool bracketedPasteModeIsDisabled() const {
    return _disabledBracketedPasteMode;
  }

 protected:
  bool event(QEvent*) override;

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

  // classifies the 'ch' into one of three categories
  // and returns a character to indicate which category it is in
  //
  //     - A space (returns ' ')
  //     - Part of a word (returns 'a')
  //     - Other characters (returns the input character)
  QChar charClass(QChar ch) const;

  void clearImage();

  void mouseTripleClickEvent(QMouseEvent* ev);

 signals:
  /**
   * Emitted when the user presses a key whilst the terminal widget has focus.
   */
  void keyPressedSignal(QKeyEvent* e, bool fromPaste);

  /**
   * A mouse event occurred.
   * @param button The mouse button (0 for left button, 1 for middle button, 2
   * for right button, 3 for release)
   * @param column The character column where the event occurred
   * @param line The character row where the event occurred
   * @param eventType The type of event.  0 for a mouse press / release or 1 for
   * mouse motion
   */
  void mouseSignal(int button, int column, int line, int eventType);
  void changedFontMetricSignal(int height, int width);
  void changedContentSizeSignal(int height, int width);

  /**
   * Emitted when the user right clicks on the display, or right-clicks with the
   * Shift key held down if usesMouse() is true.
   *
   * This can be used to display a context menu.
   */
  void configureRequest(const QPoint& position);

  /**
   * When a shortcut which is also a valid terminal key sequence is pressed
   * while the terminal widget  has focus, this signal is emitted to allow the
   * host to decide whether the shortcut should be overridden. When the shortcut
   * is overridden, the key sequence will be sent to the terminal emulation
   * instead and the action associated with the shortcut will not be triggered.
   *
   * @p override is set to false by default and the shortcut will be triggered
   * as normal.
   */
  void overrideShortcutCheck(QKeyEvent* keyEvent, bool& override);

  void isBusySelecting(bool);
  void sendStringToEmu(const char*);

  // terminalemulator signals
  void copyAvailable(bool);
  void termGetFocus();
  void termLostFocus();

  void notifyBell(const QString&);
  void usesMouseChanged();

 public slots:
  /**
   * Causes the terminal display to fetch the latest character image from the
   * associated terminal screen ( see setScreenWindow() ) and redraw the
   * display.
   */
  void updateImage();

  /** Essentially calls processFilters().
   */
  void updateFilters();

  /**
   * Causes the terminal display to fetch the latest line status flags from the
   * associated terminal screen ( see setScreenWindow() ).
   */
  void updateLineProperties();

  /** Copies the selected text to the clipboard. */
  void copyClipboard();
  /**
   * Pastes the content of the clipboard into the
   * display.
   */
  void pasteClipboard();
  /**
   * Pastes the content of the selection into the
   * display.
   */
  void pasteSelection();

  /**
   * Causes the widget to display or hide a message informing the user that
   * terminal output has been suspended (by using the flow control key
   * combination Ctrl+S)
   *
   * @param suspended True if terminal output has been suspended and the warning
   * message should be shown or false to indicate that terminal output has been
   * resumed and that the warning message should disappear.
   */
  void outputSuspended(bool suspended);

  /**
   * Sets whether the program whose output is being displayed in the view
   * is interested in mouse events.
   *
   * If this is set to true, mouse signals will be emitted by the view when the
   * user clicks, drags or otherwise moves the mouse inside the view. The user
   * interaction needed to create selections will also change, and the user will
   * be required to hold down the shift key to create a selection or perform
   * other mouse activities inside the view area - since the program running in
   * the terminal is being allowed to handle normal mouse events itself.
   *
   * @param usesMouse Set to true if the program running in the terminal is
   * interested in mouse events or false otherwise.
   */
  void setUsesMouse(bool usesMouse);

  /** See setUsesMouse() */
  bool usesMouse() const;

  void setBracketedPasteMode(bool bracketedPasteMode);
  bool bracketedPasteMode() const;

  /**
   * Shows a notification that a bell event has occurred in the terminal.
   * TODO: More documentation here
   */
  void bell(const QString& message);

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

  void selectionChanged();

 protected slots:
  void scrollBarPositionChanged(int value);
  void blinkEvent();
  void blinkCursorEvent();

  // Renables bell noises and visuals.  Used to disable further bells for a
  // short period of time after emitting the first in a sequence of bell events.
  void enableBell();

 private slots:
  void swapColorTable();
  void tripleClickTimeout();  // resets possibleTripleClick

 private:
  // -- Drawing helpers --

  // determine the width of this text
  int textWidth(int startColumn, int length, int line) const;
  // determine the area that encloses this series of characters
  QRect calculateTextArea(int topLeftX, int topLeftY, int startColumn, int line,
                          int length);

  // divides the part of the display specified by 'rect' into
  // fragments according to their colors and styles and calls
  // drawTextFragment() to draw the fragments
  void drawContents(QPainter& paint, const QRect& rect);
  // draws a section of text, all the text in this section
  // has a common color and style
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

  // --

  // maps an area in the character image to an area on the widget
  QRect imageToWidget(const QRect& imageArea) const;

  // the area where the preedit string for input methods will be draw
  QRect preeditRect() const;

  // shows a notification window in the middle of the widget indicating the
  // terminal's current size in columns and lines
  void showResizeNotification();

  // scrolls the image by a number of lines.
  // 'lines' may be positive ( to scroll the image down )
  // or negative ( to scroll the image up )
  // 'region' is the part of the image to scroll - currently only
  // the top, bottom and height of 'region' are taken into account,
  // the left and right are ignored.
  void scrollImage(int lines, const QRect& region);

  // shows the multiline prompt
  bool multilineConfirmation(const QString& text);

  void calcGeometry();
  void propagateSize();
  void updateImageSize();
  void makeImage();

  void paintFilters(QPainter& painter);

  void calDrawTextAdditionHeight(QPainter& painter);

  // returns a region covering all of the areas of the widget which contain
  // a hotspot
  QRegion hotSpotRegion() const;

  // returns the position of the cursor in columns and lines
  QPoint cursorPosition() const;

  // redraws the cursor
  void updateCursor();

  bool handleShortcutOverrideEvent(QKeyEvent* event);

  bool isLineChar(wchar_t c) const;
  bool isLineCharString(const std::wstring& string) const;

  QPointer<ScreenWindow> _screenWindow;

  QGridLayout* _gridLayout;
  bool _allowBell;
  // Whether intense colors should be bold.
  bool _boldIntense;
  // Whether is test mode.
  bool _drawTextTestFlag;

  // whether has fixed pitch.
  bool _fixedFont;
  int _fontHeight;
  int _fontWidth;
  int _fontAscend;
  int _drawTextAdditionHeight;
  int _leftMargin;
  int _topMargin;

  int _leftBaseMargin;
  int _topBaseMargin;

  // The total number of lines that can be displayed in the view;
  int _lines;
  // The total number of columns that can be displayed in the view;
  int _columns;

  int _usedLines;
  int _usedColumns;

  int _contentHeight;
  int _contentWidth;

  Character* _image;
  int _imageSize;

  QVector<LineProperty> _lineProperties;

  ColorEntry _colorTable[TABLE_COLORS];
  uint _randomSeed;

  bool _resizing;
  bool _terminalSizeHint;
  bool _terminalSizeStartup;
  bool _bidiEnabled;
  bool _mouseMarks;
  bool _bracketedPasteMode;
  bool _disabledBracketedPasteMode;

  // initial selection point
  QPoint _iPntSel;
  // current selection point
  QPoint _pntSel;
  // help avoid flicker
  QPoint _tripleSelBegin;
  // selection state
  int _actSel;
  bool _wordSelectionMode;
  bool _lineSelectionMode;
  bool _preserveLineBreaks;
  bool _columnSelectionMode;

  QClipboard* _clipboard;
  QScrollBar* _scrollBar;
  ScrollBarPosition _scrollbarLocation;
  QString _wordCharacters;
  int _bellMode;

  // hide text in paint event.
  bool _blinking;
  // has character to blink.
  bool _hasBlinker;
  // hide cursor in paint event.
  bool _cursorBlinking;
  // has bliking cursor enable.
  bool _hasBlinkingCursor;
  // allow text to blink.
  bool _allowBlinkingText;
  // require Ctrl key for drag
  bool _ctrlDrag;
  // columns/lines are locked.
  bool _isFixedSize;
  // set in mouseDoubleClickEvent and delete after
  // QApplication::doubleClickInterval() delay.
  bool _possibleTripleClick;
  TripleClickMode _tripleClickMode;
  QTimer* _blinkTimer;
  QTimer* _blinkCursorTimer;

  // true during visual bell
  bool _colorsInverted;

  QLabel* _resizeWidget;
  QTimer* _resizeTimer;

  QLabel* _outputSuspendedLabel;

  uint _lineSpacing;
  qreal _opacity;
  QSize _size;

  QPixmap _backgroundImage;
  BackgroundMode _backgroundMode;

  // list of filters currently applied to the display.  used for links and
  // search highlight
  TerminalImageFilterChain* _filterChain;
  QRegion _mouseOverHotspotArea;

  KeyboardCursorShape _cursorShape;
  QColor _cursorColor;

  MotionAfterPasting mMotionAfterPasting;
  bool _confirmMultilinePaste;
  bool _trimPastedTrailingNewlines;

  struct InputMethodData {
    std::wstring preeditString;
    QRect previousPreeditRect;
  };
  InputMethodData _inputMethodData;

  bool _drawLineChars;

  static bool _antialiasText;  // do we antialias or not

  // the delay in milliseconds between redrawing blinking text
  static const int TEXT_BLINK_DELAY = 500;

 public:
  static void setTransparencyEnabled(bool enable) {
    HAVE_TRANSPARENCY = enable;
  }
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
