#include "terminal_view.h"

#include <QAbstractButton>
#include <QApplication>
#include <QBoxLayout>
#include <QClipboard>
#include <QDateTime>
#include <QDrag>
#include <QEvent>
#include <QFile>
#include <QGridLayout>
#include <QKeyEvent>
#include <QLabel>
#include <QLayout>
#include <QMessageBox>
#include <QMimeData>
#include <QPainter>
#include <QPixmap>
#include <QRegularExpression>
#include <QScrollBar>
#include <QStyle>
#include <QTime>
#include <QTimer>
#include <QUrl>
#include <QtDebug>
#include <thread>

#include "screen.h"
#include "wcwidth.h"

using namespace TConsole;

#ifndef loc
#define loc(X, Y) ((Y)*_columns + (X))
#endif

#define REPCHAR                \
  "ABCDEFGHIJKLMNOPQRSTUVWXYZ" \
  "abcdefgjijklmnopqrstuvwxyz" \
  "0123456789./+@"

// static
bool TerminalView::_antialiasText = true;
bool TerminalView::HAVE_TRANSPARENCY = true;

QRegularExpression rRegularExpression(QStringLiteral("\\r+$"));
QString qssFilePath = QString(QSS_DIR).append("/view-scrollbar.qss");

const ColorEntry TConsole::base_color_table[TABLE_COLORS] =
    // The following are almost IBM standard color codes, with some slight
    // gamma correction for the dim colors to compensate for bright X screens.
    // It contains the 8 ansiterm/xterm colors in 2 intensities.
    {
        // Fixme: could add faint colors here, also.
        // normal
        ColorEntry(QColor(0x00, 0x00, 0x00), false),
        ColorEntry(QColor(0xB2, 0xB2, 0xB2), true),  // Dfore, Dback
        ColorEntry(QColor(0x00, 0x00, 0x00), false),
        ColorEntry(QColor(0xB2, 0x18, 0x18), false),  // Black, Red
        ColorEntry(QColor(0x18, 0xB2, 0x18), false),
        ColorEntry(QColor(0xB2, 0x68, 0x18), false),  // Green, Yellow
        ColorEntry(QColor(0x18, 0x18, 0xB2), false),
        ColorEntry(QColor(0xB2, 0x18, 0xB2), false),  // Blue, Magenta
        ColorEntry(QColor(0x18, 0xB2, 0xB2), false),
        ColorEntry(QColor(0xB2, 0xB2, 0xB2), false),  // Cyan, White

        // intensiv
        ColorEntry(QColor(0x00, 0x00, 0x00), false),
        ColorEntry(QColor(0xFF, 0xFF, 0xFF), true),
        ColorEntry(QColor(0x68, 0x68, 0x68), false),
        ColorEntry(QColor(0xFF, 0x54, 0x54), false),
        ColorEntry(QColor(0x54, 0xFF, 0x54), false),
        ColorEntry(QColor(0xFF, 0xFF, 0x54), false),
        ColorEntry(QColor(0x54, 0x54, 0xFF), false),
        ColorEntry(QColor(0xFF, 0x54, 0xFF), false),
        ColorEntry(QColor(0x54, 0xFF, 0xFF), false),
        ColorEntry(QColor(0xFF, 0xFF, 0xFF), false)};

unsigned short TConsole::vt100_graphics[32] =
    {  // 0/8     1/9    2/10    3/11    4/12    5/13    6/14    7/15
        0x0020, 0x25C6, 0x2592, 0x2409, 0x240c, 0x240d, 0x240a, 0x00b0,
        0x00b1, 0x2424, 0x240b, 0x2518, 0x2510, 0x250c, 0x2514, 0x253c,
        0xF800, 0xF801, 0x2500, 0xF803, 0xF804, 0x251c, 0x2524, 0x2534,
        0x252c, 0x2502, 0x2264, 0x2265, 0x03C0, 0x2260, 0x00A3, 0x00b7};

const QChar LTR_OVERRIDE_CHAR(0x202D);

/* ------------------------------------------------------------------------- */
/*                                                                           */
/*                             Display Operations                            */
/*                                                                           */
/* ------------------------------------------------------------------------- */
enum LineEncode {
  TopL = (1 << 1),
  TopC = (1 << 2),
  TopR = (1 << 3),

  LeftT = (1 << 5),
  Int11 = (1 << 6),
  Int12 = (1 << 7),
  Int13 = (1 << 8),
  RightT = (1 << 9),

  LeftC = (1 << 10),
  Int21 = (1 << 11),
  Int22 = (1 << 12),
  Int23 = (1 << 13),
  RightC = (1 << 14),

  LeftB = (1 << 15),
  Int31 = (1 << 16),
  Int32 = (1 << 17),
  Int33 = (1 << 18),
  RightB = (1 << 19),

  BotL = (1 << 21),
  BotC = (1 << 22),
  BotR = (1 << 23)
};
static const quint32 LineChars[] = {
    0x00007c00, 0x000fffe0, 0x00421084, 0x00e739ce, 0x00000000, 0x00000000,
    0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
    0x00427000, 0x004e7380, 0x00e77800, 0x00ef7bc0, 0x00421c00, 0x00439ce0,
    0x00e73c00, 0x00e7bde0, 0x00007084, 0x000e7384, 0x000079ce, 0x000f7bce,
    0x00001c84, 0x00039ce4, 0x00003dce, 0x0007bdee, 0x00427084, 0x004e7384,
    0x004279ce, 0x00e77884, 0x00e779ce, 0x004f7bce, 0x00ef7bc4, 0x00ef7bce,
    0x00421c84, 0x00439ce4, 0x00423dce, 0x00e73c84, 0x00e73dce, 0x0047bdee,
    0x00e7bde4, 0x00e7bdee, 0x00427c00, 0x0043fce0, 0x004e7f80, 0x004fffe0,
    0x004fffe0, 0x00e7fde0, 0x006f7fc0, 0x00efffe0, 0x00007c84, 0x0003fce4,
    0x000e7f84, 0x000fffe4, 0x00007dce, 0x0007fdee, 0x000f7fce, 0x000fffee,
    0x00427c84, 0x0043fce4, 0x004e7f84, 0x004fffe4, 0x00427dce, 0x00e77c84,
    0x00e77dce, 0x0047fdee, 0x004e7fce, 0x00e7fde4, 0x00ef7f84, 0x004fffee,
    0x00efffe4, 0x00e7fdee, 0x00ef7fce, 0x00efffee, 0x00000000, 0x00000000,
    0x00000000, 0x00000000, 0x000f83e0, 0x00a5294a, 0x004e1380, 0x00a57800,
    0x00ad0bc0, 0x004390e0, 0x00a53c00, 0x00a5a1e0, 0x000e1384, 0x0000794a,
    0x000f0b4a, 0x000390e4, 0x00003d4a, 0x0007a16a, 0x004e1384, 0x00a5694a,
    0x00ad2b4a, 0x004390e4, 0x00a52d4a, 0x00a5a16a, 0x004f83e0, 0x00a57c00,
    0x00ad83e0, 0x000f83e4, 0x00007d4a, 0x000f836a, 0x004f93e4, 0x00a57d4a,
    0x00ad836a, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
    0x00000000, 0x00000000, 0x00001c00, 0x00001084, 0x00007000, 0x00421000,
    0x00039ce0, 0x000039ce, 0x000e7380, 0x00e73800, 0x000e7f80, 0x00e73884,
    0x0003fce0, 0x004239ce};
static void drawLineChar(QPainter& paint, int x, int y, int w, int h,
                         uint8_t code) {
  // Calculate cell midpoints, end points.
  int cx = x + w / 2;
  int cy = y + h / 2;
  int ex = x + w - 1;
  int ey = y + h - 1;

  quint32 toDraw = LineChars[code];

  // Top _lines:
  if (toDraw & TopL) paint.drawLine(cx - 1, y, cx - 1, cy - 2);
  if (toDraw & TopC) paint.drawLine(cx, y, cx, cy - 2);
  if (toDraw & TopR) paint.drawLine(cx + 1, y, cx + 1, cy - 2);

  // Bot _lines:
  if (toDraw & BotL) paint.drawLine(cx - 1, cy + 2, cx - 1, ey);
  if (toDraw & BotC) paint.drawLine(cx, cy + 2, cx, ey);
  if (toDraw & BotR) paint.drawLine(cx + 1, cy + 2, cx + 1, ey);

  // Left _lines:
  if (toDraw & LeftT) paint.drawLine(x, cy - 1, cx - 2, cy - 1);
  if (toDraw & LeftC) paint.drawLine(x, cy, cx - 2, cy);
  if (toDraw & LeftB) paint.drawLine(x, cy + 1, cx - 2, cy + 1);

  // Right _lines:
  if (toDraw & RightT) paint.drawLine(cx + 2, cy - 1, ex, cy - 1);
  if (toDraw & RightC) paint.drawLine(cx + 2, cy, ex, cy);
  if (toDraw & RightB) paint.drawLine(cx + 2, cy + 1, ex, cy + 1);

  // Intersection points.
  if (toDraw & Int11) paint.drawPoint(cx - 1, cy - 1);
  if (toDraw & Int12) paint.drawPoint(cx, cy - 1);
  if (toDraw & Int13) paint.drawPoint(cx + 1, cy - 1);

  if (toDraw & Int21) paint.drawPoint(cx - 1, cy);
  if (toDraw & Int22) paint.drawPoint(cx, cy);
  if (toDraw & Int23) paint.drawPoint(cx + 1, cy);

  if (toDraw & Int31) paint.drawPoint(cx - 1, cy + 1);
  if (toDraw & Int32) paint.drawPoint(cx, cy + 1);
  if (toDraw & Int33) paint.drawPoint(cx + 1, cy + 1);
}

static void drawOtherChar(QPainter& paint, int x, int y, int w, int h,
                          uchar code) {
  // Calculate cell midpoints, end points.
  const int cx = x + w / 2;
  const int cy = y + h / 2;
  const int ex = x + w - 1;
  const int ey = y + h - 1;

  // Double dashes
  if (0x4C <= code && code <= 0x4F) {
    const int xHalfGap = qMax(w / 15, 1);
    const int yHalfGap = qMax(h / 15, 1);
    switch (code) {
      case 0x4D:  // BOX DRAWINGS HEAVY DOUBLE DASH HORIZONTAL
        paint.drawLine(x, cy - 1, cx - xHalfGap - 1, cy - 1);
        paint.drawLine(x, cy + 1, cx - xHalfGap - 1, cy + 1);
        paint.drawLine(cx + xHalfGap, cy - 1, ex, cy - 1);
        paint.drawLine(cx + xHalfGap, cy + 1, ex, cy + 1);
        /* Falls through. */
      case 0x4C:  // BOX DRAWINGS LIGHT DOUBLE DASH HORIZONTAL
        paint.drawLine(x, cy, cx - xHalfGap - 1, cy);
        paint.drawLine(cx + xHalfGap, cy, ex, cy);
        break;
      case 0x4F:  // BOX DRAWINGS HEAVY DOUBLE DASH VERTICAL
        paint.drawLine(cx - 1, y, cx - 1, cy - yHalfGap - 1);
        paint.drawLine(cx + 1, y, cx + 1, cy - yHalfGap - 1);
        paint.drawLine(cx - 1, cy + yHalfGap, cx - 1, ey);
        paint.drawLine(cx + 1, cy + yHalfGap, cx + 1, ey);
        /* Falls through. */
      case 0x4E:  // BOX DRAWINGS LIGHT DOUBLE DASH VERTICAL
        paint.drawLine(cx, y, cx, cy - yHalfGap - 1);
        paint.drawLine(cx, cy + yHalfGap, cx, ey);
        break;
    }
  }

  // Rounded corner characters
  else if (0x6D <= code && code <= 0x70) {
    const int r = w * 3 / 8;
    const int d = 2 * r;
    switch (code) {
      case 0x6D:  // BOX DRAWINGS LIGHT ARC DOWN AND RIGHT
        paint.drawLine(cx, cy + r, cx, ey);
        paint.drawLine(cx + r, cy, ex, cy);
        paint.drawArc(cx, cy, d, d, 90 * 16, 90 * 16);
        break;
      case 0x6E:  // BOX DRAWINGS LIGHT ARC DOWN AND LEFT
        paint.drawLine(cx, cy + r, cx, ey);
        paint.drawLine(x, cy, cx - r, cy);
        paint.drawArc(cx - d, cy, d, d, 0 * 16, 90 * 16);
        break;
      case 0x6F:  // BOX DRAWINGS LIGHT ARC UP AND LEFT
        paint.drawLine(cx, y, cx, cy - r);
        paint.drawLine(x, cy, cx - r, cy);
        paint.drawArc(cx - d, cy - d, d, d, 270 * 16, 90 * 16);
        break;
      case 0x70:  // BOX DRAWINGS LIGHT ARC UP AND RIGHT
        paint.drawLine(cx, y, cx, cy - r);
        paint.drawLine(cx + r, cy, ex, cy);
        paint.drawArc(cx, cy - d, d, d, 180 * 16, 90 * 16);
        break;
    }
  }

  // Diagonals
  else if (0x71 <= code && code <= 0x73) {
    switch (code) {
      case 0x71:  // BOX DRAWINGS LIGHT DIAGONAL UPPER RIGHT TO LOWER LEFT
        paint.drawLine(ex, y, x, ey);
        break;
      case 0x72:  // BOX DRAWINGS LIGHT DIAGONAL UPPER LEFT TO LOWER RIGHT
        paint.drawLine(x, y, ex, ey);
        break;
      case 0x73:  // BOX DRAWINGS LIGHT DIAGONAL CROSS
        paint.drawLine(ex, y, x, ey);
        paint.drawLine(x, y, ex, ey);
        break;
    }
  }
}

/* ------------------------------------------------------------------------- */
/*                                                                           */
/*                                TerminalView                               */
/*                                                                           */
/* ------------------------------------------------------------------------- */
void TerminalView::scrollBarPositionChanged(int value) {
  if (!_screenWindow) return;

  _screenWindow->scrollTo(_scrollBar->value());

  // if the thumb has been moved to the bottom of the _scrollBar then set
  // the display to automatically track new output,
  // that is, scroll down automatically
  // to how new _lines as they are added
  const bool atEndOfOutput = (_scrollBar->value() == _scrollBar->maximum());
  _screenWindow->setTrackOutput(atEndOfOutput);

  updateImage();
}

void TerminalView::blinkEvent() {
  if (!_allowBlinkingText) return;

  _blinking = !_blinking;

  // TODO:  Optimize to only repaint the areas of the widget
  // where there is blinking text
  // rather than repainting the whole widget.
  update();
}

void TerminalView::blinkCursorEvent() {
  _cursorBlinking = !_cursorBlinking;
  updateCursor();
}

void TerminalView::enableBell() { _allowBell = true; }

void TerminalView::swapColorTable() {
  ColorEntry color = _colorTable[1];
  _colorTable[1] = _colorTable[0];
  _colorTable[0] = color;
  _colorsInverted = !_colorsInverted;
  update();
}

void TerminalView::tripleClickTimeout() { _possibleTripleClick = false; }

void TerminalView::clearImage() {
  // We initialize _image[_imageSize] too. See makeImage()
  for (int i = 0; i <= _imageSize; i++) {
    _image[i].character = ' ';
    _image[i].foregroundColor =
        CharacterColor(COLOR_SPACE_DEFAULT, DEFAULT_FORE_COLOR);
    _image[i].backgroundColor =
        CharacterColor(COLOR_SPACE_DEFAULT, DEFAULT_BACK_COLOR);
    _image[i].rendition = DEFAULT_RENDITION;
  }
}

int TerminalView::textWidth(const int startColumn, const int length,
                            const int line) const {
  QFontMetrics fontMetrics(font());
  int result = 0;
  for (int column = 0; column < length; column++) {
    result += fontMetrics.horizontalAdvance(
        _image[loc(startColumn + column, line)].character);
  }
  return result;
}

QRect TerminalView::calculateTextArea(int topLeftX, int topLeftY,
                                      int startColumn, int line, int length) {
  int left =
      _fixedFont ? _fontWidth * startColumn : textWidth(0, startColumn, line);
  int top = _fontHeight * line;
  int width =
      _fixedFont ? _fontWidth * length : textWidth(startColumn, length, line);
  return {_leftMargin + topLeftX + left, _topMargin + topLeftY + top, width,
          _fontHeight};
}

void TerminalView::drawContents(QPainter& painter, const QRect& rect) {
  QPoint tL = contentsRect().topLeft();
  int tLx = tL.x();
  int tLy = tL.y();

  int lux = qMin(_usedColumns - 1,
                 qMax(0, (rect.left() - tLx - _leftMargin) / _fontWidth));
  int luy = qMin(_usedLines - 1,
                 qMax(0, (rect.top() - tLy - _topMargin) / _fontHeight));
  int rlx = qMin(_usedColumns - 1,
                 qMax(0, (rect.right() - tLx - _leftMargin) / _fontWidth));
  int rly = qMin(_usedLines - 1,
                 qMax(0, (rect.bottom() - tLy - _topMargin) / _fontHeight));

  const int bufferSize = _usedColumns;
  std::wstring unistr;
  unistr.reserve(bufferSize);

  int y = luy;
  for (; y <= rly; y++) {
    quint32 c = _image[loc(lux, y)].character;
    int x = lux;
    if (!c && x) x--;  // Search for start of multi-column character
    //    drawSegment(c, rlx, lux, x, y1, tLx, tLy, bufferSize, unistr,
    //    painter);
    for (; x <= rlx; x++) {
      int len = 1;
      int p = 0;

      // reset our buffer to the maximal size
      unistr.resize(bufferSize);

      // is this a single character or a sequence of characters ?
      if (_image[loc(x, y)].rendition & RE_EXTENDED_CHAR) {
        // sequence of characters
        ushort extendedCharLength = 0;
        ushort* chars = ExtendedCharTable::instance.lookupExtendedChar(
            _image[loc(x, y)].charSequence, extendedCharLength);
        for (int index = 0; index < extendedCharLength; index++) {
          Q_ASSERT(p < bufferSize);
          unistr[p++] = chars[index];
        }
      } else {
        // single character
        c = _image[loc(x, y)].character;
        if (c) {
          Q_ASSERT(p < bufferSize);
          unistr[p++] = c;  // fontMap(c);
        }
      }

      bool lineDraw = isLineChar(c);
      bool doubleWidth =
          (_image[qMin(loc(x, y) + 1, _imageSize)].character == 0);
      CharacterColor currentForeground = _image[loc(x, y)].foregroundColor;
      CharacterColor currentBackground = _image[loc(x, y)].backgroundColor;
      quint8 currentRendition = _image[loc(x, y)].rendition;

      while (x + len <= rlx &&
             _image[loc(x + len, y)].foregroundColor == currentForeground &&
             _image[loc(x + len, y)].backgroundColor == currentBackground &&
             _image[loc(x + len, y)].rendition == currentRendition &&
             (_image[qMin(loc(x + len, y) + 1, _imageSize)].character == 0) ==
                 doubleWidth &&
             isLineChar(c = _image[loc(x + len, y)].character) ==
                 lineDraw)  // Assignment!
      {
        if (c) unistr[p++] = c;  // fontMap(c);
        if (doubleWidth)  // assert((_image[loc(x+len,y)+1].character == 0)),
                          // see above if condition
          len++;          // Skip trailing part of multi-column character
        len++;
      }
      if ((x + len < _usedColumns) && (!_image[loc(x + len, y)].character))
        len++;  // Adjust for trailing part of multi-column character

      bool save__fixedFont = _fixedFont;
      if (lineDraw) _fixedFont = false;
      unistr.resize(p);

      // Create a text scaling matrix for double width and double height lines.
      QTransform textScale;

      if (y < _lineProperties.size()) {
        if (_lineProperties[y] & LINE_DOUBLEWIDTH) textScale.scale(2, 1);

        if (_lineProperties[y] & LINE_DOUBLEHEIGHT) textScale.scale(1, 2);
      }

      // Apply text scaling matrix.
      painter.setWorldTransform(textScale, true);

      // calculate the area in which the text will be drawn
      QRect textArea = calculateTextArea(tLx, tLy, x, y, len);

      // move the calculated area to take account of scaling applied to the
      // painter. the position of the area from the origin (0,0) is scaled by
      // the opposite of whatever transformation has been applied to the
      // painter. this ensures that painting does actually start from
      // textArea.topLeft()
      //(instead of textArea.topLeft() * painter-scale)
      textArea.moveTopLeft(textScale.inverted().map(textArea.topLeft()));

      // paint text fragment
      drawTextFragment(painter, textArea, unistr, &_image[loc(x, y)]);  //,
                                                                        // 0,
      //!_isPrinting );

      _fixedFont = save__fixedFont;

      // reset back to single-width, single-height _lines
      painter.setWorldTransform(textScale.inverted(), true);

      if (y < _lineProperties.size() - 1) {
        // double-height _lines are represented by two adjacent _lines
        // containing the same characters
        // both _lines will have the LINE_DOUBLEHEIGHT attribute.
        // If the current line has the LINE_DOUBLEHEIGHT attribute,
        // we can therefore skip the next line
        if (_lineProperties[y] & LINE_DOUBLEHEIGHT) y++;
      }

      x += len - 1;
    }
  }
}

void TerminalView::drawTextFragment(QPainter& painter, const QRect& rect,
                                    const std::wstring& text,
                                    const Character* style) {
  painter.save();
  // setup painter
  const QColor foregroundColor = style->foregroundColor.color(_colorTable);
  const QColor backgroundColor = style->backgroundColor.color(_colorTable);

  if (backgroundColor != palette().window().color())
    drawBackground(painter, rect, backgroundColor, false);

  bool invertCharacterColor = false;

  // draw text
  drawCharacters(painter, rect, text, style, invertCharacterColor);

  if (style->rendition & RE_CURSOR)
    drawCursor(painter, rect, foregroundColor, backgroundColor,
               invertCharacterColor);

  painter.restore();
}

void TerminalView::drawBackground(QPainter& painter, const QRect& rect,
                                  const QColor& backgroundColor,
                                  bool useOpacitySetting) {
  if (useOpacitySetting) {
    if (_backgroundImage.isNull()) {
      QColor color(backgroundColor);
      color.setAlphaF(_opacity);

      painter.save();
      painter.setCompositionMode(QPainter::CompositionMode_Source);
      painter.fillRect(rect, color);
      painter.restore();
    }
  }
}

void TerminalView::drawCursor(QPainter& painter, const QRect& rect,
                              const QColor& foregroundColor,
                              const QColor& backgroundColor,
                              bool& invertCharacterColor) {
  QRectF cursorRect = rect;
  cursorRect.setHeight(_fontHeight - _lineSpacing - 1);

  if (!_cursorBlinking) {
    if (_cursorColor.isValid())
      painter.setPen(_cursorColor);
    else
      painter.setPen(foregroundColor);

    if (_cursorShape == KeyboardCursorShape::BLOCK_CURSOR) {
      // draw the cursor outline, adjusting the area so that
      // it is draw entirely inside 'rect'
      float penWidth = qMax(1, painter.pen().width());

      painter.drawRect(cursorRect.adjusted(penWidth / 2, penWidth / 2,
                                           -penWidth / 2, -penWidth / 2));
      if (hasFocus() ||
          (_nativeCanvas != nullptr && _nativeCanvas->hasFocus())) {
        painter.fillRect(cursorRect, _cursorColor.isValid() ? _cursorColor
                                                            : foregroundColor);

        if (!_cursorColor.isValid()) {
          // invert the colour used to draw the text to ensure that the
          // character at the cursor position is readable
          invertCharacterColor = true;
        }
      }
    } else if (_cursorShape == KeyboardCursorShape::UNDERLINE_CURSOR)
      painter.drawLine(cursorRect.left(), cursorRect.bottom(),
                       cursorRect.right(), cursorRect.bottom());
    else if (_cursorShape == KeyboardCursorShape::IBEAM_CURSOR)
      painter.drawLine(cursorRect.left(), cursorRect.top(), cursorRect.left(),
                       cursorRect.bottom());
  }
}

void TerminalView::drawCharacters(QPainter& painter, const QRect& rect,
                                  const std::wstring& text,
                                  const Character* style,
                                  bool invertCharacterColor) {
  // don't draw text which is currently blinking
  if (_blinking && (style->rendition & RE_BLINK)) return;

  // don't draw concealed characters
  if (style->rendition & RE_CONCEAL) return;

  // setup bold and underline
  bool useBold =
      ((style->rendition & RE_BOLD) && _boldIntense) || font().bold();
  const bool useUnderline =
      style->rendition & RE_UNDERLINE || font().underline();
  const bool useItalic = style->rendition & RE_ITALIC || font().italic();
  const bool useStrikeOut =
      style->rendition & RE_STRIKEOUT || font().strikeOut();
  const bool useOverline = style->rendition & RE_OVERLINE || font().overline();

  QFont font = painter.font();
  if (font.bold() != useBold || font.underline() != useUnderline ||
      font.italic() != useItalic || font.strikeOut() != useStrikeOut ||
      font.overline() != useOverline) {
    font.setBold(useBold);
    font.setItalic(useItalic);
    // static text
    // FIXME: QStaticText have some problem with underline, strikeline and
    // overline... see below
    //    font.setUnderline(useUnderline);
    //    font.setStrikeOut(useStrikeOut);
    //    font.setOverline(useOverline);
    painter.setFont(font);
  }

  // setup pen
  const CharacterColor& textColor =
      (invertCharacterColor ? style->backgroundColor : style->foregroundColor);
  const QColor color = textColor.color(_colorTable);
  QPen pen = painter.pen();
  if (pen.color() != color) {
    pen.setColor(color);
    painter.setPen(color);
  }

  // draw text
  if (isLineCharString(text))
    drawLineCharString(painter, rect.x(), rect.y(), text, style);
  else {
    // Force using LTR as the document layout for the terminal area, because
    // there is no use cases for RTL emulator and RTL terminal application.
    //
    // This still allows RTL characters to be rendered in the RTL way.
    painter.setLayoutDirection(Qt::LeftToRight);

    if (_bidiEnabled) {
      painter.fillRect(rect, style->backgroundColor.color(_colorTable));
      painter.drawText(rect.x(), rect.y() + _fontAscend + _lineSpacing,
                       QString::fromStdWString(text));
    } else {
// static text
#if 1
      QString draw_text_str = QString::fromStdWString(text);
      uint32_t draw_text_flags = 0;
      if (useBold) draw_text_flags |= (1 << 0);
      //       if (useUnderline) draw_text_flags |= (1 << 1);
      if (useItalic) draw_text_flags |= (1 << 2);
      //       if (useStrikeOut) draw_text_flags |= (1 << 3);
      //       if (useOverline) draw_text_flags |= (1 << 4);

      QPair<uint32_t, QString> static_text_key(draw_text_flags, draw_text_str);

      QStaticText* staticText = _staticTextCache.object(static_text_key);
      if (!staticText) {
        staticText = new QStaticText(draw_text_str);
        staticText->setTextFormat(Qt::PlainText);
        staticText->prepare(QTransform(), font);
        _staticTextCache.insert(static_text_key, staticText);
      }

      painter.fillRect(rect, style->backgroundColor.color(_colorTable));
      painter.drawStaticText(rect.topLeft(), *staticText);
      //      painter.drawStaticText(
      //          QPointF(
      //              rect.left(),
      //              rect.top() -
      //                  (staticText->size().height() - rect.height()) * 4 /
      //                      5),  // align baseline for fallback font (80% of
      //                      height)
      //          *staticText);
      // FIXME: see previous comments
      if (useUnderline)
        painter.drawLine(QLineF(rect.left(), rect.bottom() - 0.5, rect.right(),
                                rect.bottom() - 0.5));
      if (useStrikeOut)
        painter.drawLine(QLineF(rect.left(), (rect.top() + rect.bottom()) / 2.0,
                                rect.right(),
                                (rect.top() + rect.bottom()) / 2.0));
      if (useOverline)
        painter.drawLine(QLineF(rect.left(), rect.top() + 0.5, rect.right(),
                                rect.top() + 0.5));
#else
      QRect drawRect(rect.topLeft(), rect.size());
      drawRect.setHeight(rect.height() + _drawTextAdditionHeight);
      painter.fillRect(drawRect, style->backgroundColor.color(_colorTable));
      painter.drawText(drawRect, Qt::AlignBottom,
                       LTR_OVERRIDE_CHAR + QString::fromStdWString(text));
#endif
    }
  }
}

void TerminalView::drawLineCharString(QPainter& painter, int x, int y,
                                      const std::wstring& str,
                                      const Character* attributes) const {
  const QPen& currentPen = painter.pen();

  if ((attributes->rendition & RE_BOLD) && _boldIntense) {
    QPen boldPen(currentPen);
    boldPen.setWidth(3);
    painter.setPen(boldPen);
  }

  for (size_t i = 0; i < str.length(); i++) {
    uint8_t code = static_cast<uint8_t>(str[i] & 0xffU);
    if (LineChars[code])
      drawLineChar(painter, x + (_fontWidth * i), y, _fontWidth, _fontHeight,
                   code);
    else
      drawOtherChar(painter, x + (_fontWidth * i), y, _fontWidth, _fontHeight,
                    code);
  }

  painter.setPen(currentPen);
}

void TerminalView::drawInputMethodPreeditString(QPainter& painter,
                                                const QRect& rect) {
  if (_inputMethodData.preeditString.empty()) return;

  const QPoint cursorPos = cursorPosition();

  bool invertColors = false;
  const QColor background = _colorTable[DEFAULT_BACK_COLOR].color;
  const QColor foreground = _colorTable[DEFAULT_FORE_COLOR].color;
  const Character* style = &_image[loc(cursorPos.x(), cursorPos.y())];

  drawBackground(painter, rect, background, true);
  drawCursor(painter, rect, foreground, background, invertColors);
  drawCharacters(painter, rect, _inputMethodData.preeditString, style,
                 invertColors);

  _inputMethodData.previousPreeditRect = rect;
}

bool TerminalView::multilineConfirmation(const QString& text) {
  QMessageBox confirmation(this);
  confirmation.setWindowTitle(tr("Paste multiline text"));
  confirmation.setText(tr("Are you sure you want to paste this text?"));
  confirmation.setDetailedText(text);
  confirmation.setStandardButtons(QMessageBox::Yes | QMessageBox::No);
  // Click "Show details..." to show those by default
  const auto buttons = confirmation.buttons();
  for (QAbstractButton* btn : buttons) {
    if (confirmation.buttonRole(btn) == QMessageBox::ActionRole &&
        btn->text() == QMessageBox::tr("Show Details...")) {
      Q_EMIT btn->clicked();
    }
  }
  confirmation.setDefaultButton(QMessageBox::Yes);
  confirmation.exec();
  if (confirmation.standardButton(confirmation.clickedButton()) !=
      QMessageBox::Yes) {
    return false;
  }
  return true;
}

void TerminalView::paintFilters(QPainter& painter) {
  // get color of character under mouse and use it to draw
  // lines for filters
  QPoint cursorPos = mapFromGlobal(QCursor::pos());
  int cursorLine;
  int cursorColumn;
  int leftMargin = _leftBaseMargin +
                   ((_scrollbarLocation == SCROLL_BAR_LEFT &&
                     !_scrollBar->style()->styleHint(
                         QStyle::SH_ScrollBar_Transient, nullptr, _scrollBar))
                        ? _scrollBar->width()
                        : 0);

  getCharacterPosition(cursorPos, cursorLine, cursorColumn);
  Character cursorCharacter = _image[loc(cursorColumn, cursorLine)];

  painter.setPen(QPen(cursorCharacter.foregroundColor.color(getColorTable())));

  // iterate over hotspots identified by the display's currently active filters
  // and draw appropriate visuals to indicate the presence of the hotspot

  QList<Filter::HotSpot*> spots = _filterChain->hotSpots();
  QListIterator<Filter::HotSpot*> iter(spots);
  while (iter.hasNext()) {
    Filter::HotSpot* spot = iter.next();

    QRegion region;
    if (spot->type() == Filter::HotSpot::Link) {
      QRect r;
      if (spot->startLine() == spot->endLine()) {
        r.setCoords(spot->startColumn() * _fontWidth + 1 + leftMargin,
                    spot->startLine() * _fontHeight + 1 + _topBaseMargin,
                    spot->endColumn() * _fontWidth - 1 + leftMargin,
                    (spot->endLine() + 1) * _fontHeight - 1 + _topBaseMargin);
        region |= r;
      } else {
        r.setCoords(spot->startColumn() * _fontWidth + 1 + leftMargin,
                    spot->startLine() * _fontHeight + 1 + _topBaseMargin,
                    _columns * _fontWidth - 1 + leftMargin,
                    (spot->startLine() + 1) * _fontHeight - 1 + _topBaseMargin);
        region |= r;
        for (int line = spot->startLine() + 1; line < spot->endLine(); line++) {
          r.setCoords(0 * _fontWidth + 1 + leftMargin,
                      line * _fontHeight + 1 + _topBaseMargin,
                      _columns * _fontWidth - 1 + leftMargin,
                      (line + 1) * _fontHeight - 1 + _topBaseMargin);
          region |= r;
        }
        r.setCoords(0 * _fontWidth + 1 + leftMargin,
                    spot->endLine() * _fontHeight + 1 + _topBaseMargin,
                    spot->endColumn() * _fontWidth - 1 + leftMargin,
                    (spot->endLine() + 1) * _fontHeight - 1 + _topBaseMargin);
        region |= r;
      }
    }

    for (int line = spot->startLine(); line <= spot->endLine(); line++) {
      int startColumn = 0;
      int endColumn =
          _columns - 1;  // TODO use number of _columns which are actually
                         // occupied on this line rather than the width of the
                         // display in _columns

      // ignore whitespace at the end of the lines
      while (QChar(_image[loc(endColumn, line)].character).isSpace() &&
             endColumn > 0)
        endColumn--;

      // increment here because the column which we want to set 'endColumn' to
      // is the first whitespace character at the end of the line
      endColumn++;

      if (line == spot->startLine()) startColumn = spot->startColumn();
      if (line == spot->endLine()) endColumn = spot->endColumn();

      // subtract one pixel from
      // the right and bottom so that
      // we do not overdraw adjacent
      // hotspots
      //
      // subtracting one pixel from all sides also prevents an edge case where
      // moving the mouse outside a link could still leave it underlined
      // because the check below for the position of the cursor
      // finds it on the border of the target area
      QRect r;
      r.setCoords(startColumn * _fontWidth + 1 + leftMargin,
                  line * _fontHeight + 1 + _topBaseMargin,
                  endColumn * _fontWidth - 1 + leftMargin,
                  (line + 1) * _fontHeight - 1 + _topBaseMargin);
      // Underline link hotspots
      if (spot->type() == Filter::HotSpot::Link) {
        QFontMetrics metrics(font());

        // find the baseline (which is the invisible line that the characters in
        // the font sit on, with some having tails dangling below)
        int baseline = r.bottom() - metrics.descent();
        // find the position of the underline below that
        int underlinePos = baseline + metrics.underlinePos();
        if (region.contains(mapFromGlobal(QCursor::pos()))) {
          painter.drawLine(r.left(), underlinePos, r.right(), underlinePos);
        }
      }
      // Marker hotspots simply have a transparent rectanglular shape
      // drawn on top of them
      else if (spot->type() == Filter::HotSpot::Marker) {
        // TODO - Do not use a hardcoded colour for this
        painter.fillRect(r, QBrush(QColor(255, 0, 0, 120)));
      }
    }
  }
}

QRect TerminalView::imageToWidget(const QRect& imageArea) const {
  QRect result;
  result.setLeft(_leftMargin + _fontWidth * imageArea.left());
  result.setTop(_topMargin + _fontHeight * imageArea.top());
  result.setWidth(_fontWidth * imageArea.width());
  result.setHeight(_fontHeight * imageArea.height());

  return result;
}

QRect TerminalView::preeditRect() const {
  const int preeditLength = string_width(_inputMethodData.preeditString);

  if (preeditLength == 0) return {};

  return QRect(_leftMargin + _fontWidth * cursorPosition().x(),
               _topMargin + _fontHeight * cursorPosition().y(),
               _fontWidth * preeditLength, _fontHeight);
}

bool TerminalView::isLineChar(wchar_t c) const {
  return _drawLineChars && ((c & 0xFF80) == 0x2500);
}

bool TerminalView::isLineCharString(const std::wstring& string) const {
  return (string.length() > 0) && (isLineChar(string[0]));
}

nativers::SharedCanvas* TerminalView::nativeCanvas() const {
  return _nativeCanvas;
}

void TerminalView::showResizeNotification() {
  if (_terminalSizeHint && isVisible()) {
    if (_terminalSizeStartup) {
      _terminalSizeStartup = false;
      return;
    }
    if (!_resizeWidget) {
      const QString label = tr("Size: XXX x XXX");
      _resizeWidget = new QLabel(label, this);
      _resizeWidget->setMinimumWidth(
          _resizeWidget->fontMetrics().horizontalAdvance(label));
      _resizeWidget->setMinimumHeight(_resizeWidget->sizeHint().height());
      _resizeWidget->setAlignment(Qt::AlignCenter);

      _resizeWidget->setStyleSheet(
          QLatin1String("background-color:palette(window);border-style:solid;"
                        "border-width:1px;border-color:palette(dark)"));

      _resizeTimer = new QTimer(this);
      _resizeTimer->setSingleShot(true);
      connect(_resizeTimer, SIGNAL(timeout()), _resizeWidget, SLOT(hide()));
    }
    _resizeWidget->setText(tr("Size: %1 x %2").arg(_columns).arg(_lines));
    _resizeWidget->move((width() - _resizeWidget->width()) / 2,
                        (height() - _resizeWidget->height()) / 2 + 20);
    _resizeWidget->show();
    _resizeTimer->start(1000);
  }
}

void TerminalView::scrollImage(int lines, const QRect& screenWindowRegion) {
  // if the flow control warning is enabled this will interfere with the
  // scrolling optimizations and cause artifacts.  the simple solution here
  // is to just disable the optimization whilst it is visible
  if (_outputSuspendedLabel && _outputSuspendedLabel->isVisible()) return;

  // constrain the region to the display
  // the bottom of the region is capped to the number of lines in the display's
  // internal image - 2, so that the height of 'region' is strictly less
  // than the height of the internal image.
  QRect region = screenWindowRegion;
  region.setBottom(qMin(region.bottom(), this->_lines - 2));

  // return if there is nothing to do
  if (lines == 0 || _image == nullptr || !region.isValid() ||
      (region.top() + abs(lines)) >= region.bottom() ||
      this->_lines <= region.height())
    return;

  // hide terminal size label to prevent it being scrolled
  if (_resizeWidget && _resizeWidget->isVisible()) _resizeWidget->hide();

  // Note:  With Qt 4.4 the left edge of the scrolled area must be at 0
  // to get the correct (newly exposed) part of the widget repainted.
  //
  // The right edge must be before the left edge of the scroll bar to
  // avoid triggering a repaint of the entire widget, the distance is
  // given by SCROLLBAR_CONTENT_GAP
  //
  // Set the QT_FLUSH_PAINT environment variable to '1' before starting the
  // application to monitor repainting.
  //
  int scrollBarWidth =
      _scrollBar->isHidden() ? 0
      : _scrollBar->style()->styleHint(QStyle::SH_ScrollBar_Transient, nullptr,
                                       _scrollBar)
          ? 0
          : _scrollBar->width();
  const int SCROLLBAR_CONTENT_GAP = scrollBarWidth == 0 ? 0 : 1;
  QRect scrollRect;
  if (_scrollbarLocation == SCROLL_BAR_LEFT) {
    scrollRect.setLeft(scrollBarWidth + SCROLLBAR_CONTENT_GAP);
    scrollRect.setRight(width());
  } else {
    scrollRect.setLeft(0);
    scrollRect.setRight(width() - scrollBarWidth - SCROLLBAR_CONTENT_GAP);
  }
  void* firstCharPos = &_image[region.top() * this->_columns];
  void* lastCharPos = &_image[(region.top() + abs(lines)) * this->_columns];

  int top = _topMargin + (region.top() * _fontHeight);
  int linesToMove = region.height() - abs(lines);
  int bytesToMove = linesToMove * this->_columns * sizeof(Character);

  Q_ASSERT(linesToMove > 0);
  Q_ASSERT(bytesToMove > 0);

  // scroll internal image
  if (lines > 0) {
    // check that the memory areas that we are going to move are valid
    Q_ASSERT((char*)lastCharPos + bytesToMove <
             (char*)(_image + (this->_lines * this->_columns)));

    Q_ASSERT((lines * this->_columns) < _imageSize);

    // scroll internal image down
    memmove(firstCharPos, lastCharPos, bytesToMove);

    // set region of display to scroll
    scrollRect.setTop(top);
  } else {
    // check that the memory areas that we are going to move are valid
    Q_ASSERT((char*)firstCharPos + bytesToMove <
             (char*)(_image + (this->_lines * this->_columns)));

    // scroll internal image up
    memmove(lastCharPos, firstCharPos, bytesToMove);

    // set region of the display to scroll
    scrollRect.setTop(top + abs(lines) * _fontHeight);
  }
  scrollRect.setHeight(linesToMove * _fontHeight);

  Q_ASSERT(scrollRect.isValid() && !scrollRect.isEmpty());

  // scroll the display vertically to match internal _image
  scroll(0, _fontHeight * (-lines), scrollRect);
}

QRegion TerminalView::hotSpotRegion() const {
  QRegion region;
  const auto hotSpots = _filterChain->hotSpots();
  for (Filter::HotSpot* const hotSpot : hotSpots) {
    QRect r;
    if (hotSpot->startLine() == hotSpot->endLine()) {
      r.setLeft(hotSpot->startColumn());
      r.setTop(hotSpot->startLine());
      r.setRight(hotSpot->endColumn());
      r.setBottom(hotSpot->endLine());
      region |= imageToWidget(r);
      ;
    } else {
      r.setLeft(hotSpot->startColumn());
      r.setTop(hotSpot->startLine());
      r.setRight(_columns);
      r.setBottom(hotSpot->startLine());
      region |= imageToWidget(r);
      ;
      for (int line = hotSpot->startLine() + 1; line < hotSpot->endLine();
           line++) {
        r.setLeft(0);
        r.setTop(line);
        r.setRight(_columns);
        r.setBottom(line);
        region |= imageToWidget(r);
        ;
      }
      r.setLeft(0);
      r.setTop(hotSpot->endLine());
      r.setRight(hotSpot->endColumn());
      r.setBottom(hotSpot->endLine());
      region |= imageToWidget(r);
      ;
    }
  }
  return region;
}

QPoint TerminalView::cursorPosition() const {
  if (_screenWindow)
    return _screenWindow->cursorPosition();
  else
    return {0, 0};
}

void TerminalView::updateCursor() {
  QRect cursorRect = imageToWidget(QRect(cursorPosition(), QSize(1, 1)));
  repaint(cursorRect);
}

bool TerminalView::handleShortcutOverrideEvent(QKeyEvent* keyEvent) {
  int modifiers = keyEvent->modifiers();

  //  When a possible shortcut combination is pressed,
  //  emit the overrideShortcutCheck() signal to allow the host
  //  to decide whether the terminal should override it or not.
  if (modifiers != Qt::NoModifier) {
    int modifierCount = 0;
    unsigned int currentModifier = Qt::ShiftModifier;

    while (currentModifier <= Qt::KeypadModifier) {
      if (modifiers & currentModifier) modifierCount++;
      currentModifier <<= 1;
    }
    if (modifierCount < 2) {
      bool override = false;
      emit overrideShortcutCheck(keyEvent, override);
      if (override) {
        keyEvent->accept();
        return true;
      }
    }
  }

  // Override any of the following shortcuts because
  // they are needed by the terminal
  int keyCode = keyEvent->key() | modifiers;
  switch (keyCode) {
    // list is taken from the QLineEdit::event() code
    case Qt::Key_Tab:
    case Qt::Key_Delete:
    case Qt::Key_Home:
    case Qt::Key_End:
    case Qt::Key_Backspace:
    case Qt::Key_Left:
    case Qt::Key_Right:
    case Qt::Key_Escape:
      keyEvent->accept();
      return true;
  }
  return false;
}

void TerminalView::calDrawTextAdditionHeight(QPainter& painter) {
  QRect test_rect, feedback_rect;
  test_rect.setRect(1, 1, _fontWidth * 4, _fontHeight);
  painter.drawText(test_rect, Qt::AlignBottom,
                   LTR_OVERRIDE_CHAR + QLatin1String("Mq"), &feedback_rect);

  // qDebug() << "test_rect:" << test_rect << "feeback_rect:" << feedback_rect;

  _drawTextAdditionHeight = (feedback_rect.height() - _fontHeight) / 2;
  if (_drawTextAdditionHeight < 0) {
    _drawTextAdditionHeight = 0;
  }

  _drawTextTestFlag = false;
}

void TerminalView::calcGeometry() {
  _scrollBar->resize(_scrollBar->sizeHint().width(), contentsRect().height());
  int scrollBarWidth = _scrollBar->style()->styleHint(
                           QStyle::SH_ScrollBar_Transient, nullptr, _scrollBar)
                           ? 0
                           : _scrollBar->width();
  switch (_scrollbarLocation) {
    case NO_SCROLL_BAR:
      _leftMargin = _leftBaseMargin;
      _contentWidth = contentsRect().width() - 2 * _leftBaseMargin;
      break;
    case SCROLL_BAR_LEFT:
      _leftMargin = _leftBaseMargin + scrollBarWidth;
      _contentWidth =
          contentsRect().width() - 2 * _leftBaseMargin - scrollBarWidth;
      _scrollBar->move(contentsRect().topLeft());
      break;
    case SCROLL_BAR_RIGHT:
      _leftMargin = _leftBaseMargin;
      _contentWidth =
          contentsRect().width() - 2 * _leftBaseMargin - scrollBarWidth;
      _scrollBar->move(contentsRect().topRight() -
                       QPoint(_scrollBar->width() - 1, 0));
      break;
  }

  _topMargin = _topBaseMargin;
  _contentHeight =
      contentsRect().height() - 2 * _topBaseMargin + /* mysterious */ 1;

  if (!_isFixedSize) {
    // ensure that display is always at least one column wide
    _columns = qMax(1, _contentWidth / _fontWidth);
    _usedColumns = qMin(_usedColumns, _columns);

    // ensure that display is always at least one line high
    _lines = qMax(1, _contentHeight / _fontHeight);
    _usedLines = qMin(_usedLines, _lines);
  }
}

void TerminalView::propagateSize() {
  if (_isFixedSize) {
    setSize(_columns, _lines);
    QWidget::setFixedSize(sizeHint());
    parentWidget()->adjustSize();
    parentWidget()->setFixedSize(parentWidget()->sizeHint());
    return;
  }
  if (_image) updateImageSize();
}

void TerminalView::updateImageSize() {
  Character* oldimg = _image;
  int oldlin = _lines;
  int oldcol = _columns;

  makeImage();

  // copy the old image to reduce flicker
  int mLines = qMin(oldlin, _lines);
  int mColumns = qMin(oldcol, _columns);

  if (oldimg) {
    for (int line = 0; line < mLines; line++) {
      memcpy((void*)&_image[_columns * line], (void*)&oldimg[oldcol * line],
             mColumns * sizeof(Character));
    }
    delete[] oldimg;
  }

  if (_screenWindow) _screenWindow->setWindowLines(_lines);

  _resizing = (oldlin != _lines) || (oldcol != _columns);

  if (_resizing) {
    showResizeNotification();
    emit changedContentSizeSignal(_contentHeight,
                                  _contentWidth);  // expose resizeEvent
  }

  _resizing = false;
}

void TerminalView::makeImage() {
  calcGeometry();

  // confirm that array will be of non-zero size, since the painting code
  // assumes a non-zero array length
  Q_ASSERT(_lines > 0 && _columns > 0);
  Q_ASSERT(_usedLines <= _lines && _usedColumns <= _columns);

  _imageSize = _lines * _columns;

  // We over-commit one character so that we can be more relaxed in dealing with
  // certain boundary conditions: _image[_imageSize] is a valid but unused
  // position
  _image = new Character[_imageSize + 1];

  clearImage();
}

void TerminalView::setScroll(int cursor, int lines) {
  if (_scrollBar->minimum() == 0 &&
      _scrollBar->maximum() == (lines - this->_lines) &&
      _scrollBar->value() == cursor)
    return;

  disconnect(_scrollBar, SIGNAL(valueChanged(int)), this,
             SLOT(scrollBarPositionChanged(int)));
  _scrollBar->setRange(0, lines - this->_lines);
  _scrollBar->setSingleStep(1);
  _scrollBar->setPageStep(lines);
  _scrollBar->setValue(cursor);
  connect(_scrollBar, SIGNAL(valueChanged(int)), this,
          SLOT(scrollBarPositionChanged(int)));
}

void TerminalView::scrollToEnd() {
  disconnect(_scrollBar, SIGNAL(valueChanged(int)), this,
             SLOT(scrollBarPositionChanged(int)));
  _scrollBar->setValue(_scrollBar->maximum());
  connect(_scrollBar, SIGNAL(valueChanged(int)), this,
          SLOT(scrollBarPositionChanged(int)));

  _screenWindow->scrollTo(_scrollBar->value() + 1);
  _screenWindow->setTrackOutput(_screenWindow->atEndOfOutput());
}

void TerminalView::setBlinkingCursor(bool blink) {
  _hasBlinkingCursor = blink;

  if (blink && !_blinkCursorTimer->isActive())
    _blinkCursorTimer->start(QApplication::cursorFlashTime() / 2);

  if (!blink && _blinkCursorTimer->isActive()) {
    _blinkCursorTimer->stop();
    if (_cursorBlinking)
      blinkCursorEvent();
    else
      _cursorBlinking = false;
  }
}

void TerminalView::setBlinkingTextEnabled(bool blink) {
  _allowBlinkingText = blink;

  if (blink && !_blinkTimer->isActive()) _blinkTimer->start(TEXT_BLINK_DELAY);

  if (!blink && _blinkTimer->isActive()) {
    _blinkTimer->stop();
    _blinking = false;
  }
}

FilterChain* TerminalView::filterChain() const { return _filterChain; }

void TerminalView::processFilters() {
  if (!_screenWindow) return;

  QRegion preUpdateHotSpots = hotSpotRegion();

  // use _screenWindow->getImage() here rather than _image because
  // other classes may call processFilters() when this display's
  // ScreenWindow emits a scrolled() signal - which will happen before
  // updateImage() is called on the display and therefore _image is
  // out of date at this point
  _filterChain->setImage(
      _screenWindow->getImage(), _screenWindow->windowLines(),
      _screenWindow->windowColumns(), _screenWindow->getLineProperties());
  _filterChain->process();

  QRegion postUpdateHotSpots = hotSpotRegion();

  update(preUpdateHotSpots | postUpdateHotSpots);
}

void TerminalView::setSize(int cols, int lins) {
  int scrollBarWidth =
      (_scrollBar->isHidden() ||
       _scrollBar->style()->styleHint(QStyle::SH_ScrollBar_Transient, nullptr,
                                      _scrollBar))
          ? 0
          : _scrollBar->sizeHint().width();
  int horizontalMargin = 2 * _leftBaseMargin;
  int verticalMargin = 2 * _topBaseMargin;

  QSize newSize =
      QSize(horizontalMargin + scrollBarWidth + (_columns * _fontWidth),
            verticalMargin + (_lines * _fontHeight));

  if (newSize != QWidget::size()) {
    _size = newSize;
    updateGeometry();
  }
}

void TerminalView::setFixedSize(int cols, int lins) {
  _isFixedSize = true;

  // ensure that display is at least one line by one column in size
  _columns = qMax(1, cols);
  _lines = qMax(1, lins);
  _usedColumns = qMin(_usedColumns, _columns);
  _usedLines = qMin(_usedLines, _lines);

  if (_image) {
    delete[] _image;
    makeImage();
  }
  setSize(cols, lins);
  QWidget::setFixedSize(_size);
}

QSize TerminalView::sizeHint() const { return _size; }

void TerminalView::setWordCharacters(const QString& wc) {
  _wordCharacters = wc;
}

void TerminalView::setBellMode(int mode) { _bellMode = mode; }

void TerminalView::setSelection(const QString& t) {
  if (QApplication::clipboard()->supportsSelection()) {
    QApplication::clipboard()->setText(t, QClipboard::Selection);
  }
}

void TerminalView::setFont(const QFont&) {
  // ignore font change request if not coming from konsole itself
}

void TerminalView::setVTFont(const QFont& f) {
  QFont font = f;

  // This was originally set for OS X only:
  //     mac uses floats for font width specification.
  //     this ensures the same handling for all platforms
  // but then there was revealed that various Linux distros
  // have this problem too...
  //  font.setStyleStrategy(QFont::ForceIntegerMetrics);

  if (!QFontInfo(font).fixedPitch()) {
    qDebug() << "Using a variable-width font in the terminal.  This may cause "
                "performance degradation and display/alignment errors.";
  }

  // hint that text should be drawn without anti-aliasing.
  // depending on the user's font configuration, this may not be respected
  if (!_antialiasText) font.setStyleStrategy(QFont::NoAntialias);

  // experimental optimization.  Konsole assumes that the terminal is using a
  // mono-spaced font, in which case kerning information should have an effect.
  // Disabling kerning saves some computation when rendering text.
  font.setKerning(false);

  QWidget::setFont(font);
  fontChange(font);
}

void TerminalView::setLineSpacing(uint i) {
  _lineSpacing = i;
  setVTFont(font());  // Trigger an update.
}

void TerminalView::setMargin(int i) {
  _topBaseMargin = i;
  _leftBaseMargin = i;
}

int TerminalView::margin() const { return _topBaseMargin; }

uint TerminalView::lineSpacing() const { return _lineSpacing; }

void TerminalView::emitSelection(bool useXselection, bool appendReturn) {
  if (!_screenWindow) return;

  // Paste Clipboard by simulating keypress events
  QString text = QApplication::clipboard()->text(
      useXselection ? QClipboard::Selection : QClipboard::Clipboard);
  if (!text.isEmpty()) {
    text.replace(QLatin1String("\r\n"), QLatin1String("\n"));
    text.replace(QLatin1Char('\n'), QLatin1Char('\r'));

    if (_trimPastedTrailingNewlines) {
      text.replace(rRegularExpression, QString());
    }

    if (_confirmMultilinePaste && text.contains(QLatin1Char('\r'))) {
      if (!multilineConfirmation(text)) {
        return;
      }
    }

    bracketText(text);

    // appendReturn is intentionally handled _after_ enclosing texts with
    // brackets as that feature is used to allow execution of commands
    // immediately after paste. Ref: https://bugs.kde.org/show_bug.cgi?id=16179
    // Ref:
    // https://github.com/KDE/konsole/commit/83d365f2ebfe2e659c1e857a2f5f247c556ab571
    if (appendReturn) {
      text.append(QLatin1Char('\r'));
    }

    QKeyEvent e(QEvent::KeyPress, 0, Qt::NoModifier, text);
    emit keyPressedSignal(&e, true);  // expose as a big fat keypress event

    _screenWindow->clearSelection();

    switch (mMotionAfterPasting) {
      case MOVE_START_SCREEN_WINDOW:
        // Temporarily stop tracking output, or pasting contents triggers
        // ScreenWindow::notifyOutputChanged() and the latter scrolls the
        // terminal to the last line. It will be re-enabled when needed
        // (e.g., scrolling to the last line).
        _screenWindow->setTrackOutput(false);
        _screenWindow->scrollTo(0);
        break;
      case MOVE_END_SCREEN_WINDOW:
        scrollToEnd();
        break;
      case NO_MOVE_SCREEN_WINDOW:
        break;
    }
  }
}

void TerminalView::bracketText(QString& text) const {
  if (bracketedPasteMode() && !_disabledBracketedPasteMode) {
    text.prepend(QLatin1String("\033[200~"));
    text.append(QLatin1String("\033[201~"));
  }
}

void TerminalView::setKeyboardCursorShape(KeyboardCursorShape shape) {
  _cursorShape = shape;
}

KeyboardCursorShape TerminalView::keyboardCursorShape() const {
  return _cursorShape;
}

void TerminalView::setKeyboardCursorColor(bool useForegroundColor,
                                          const QColor& color) {
  if (useForegroundColor)
    _cursorColor = QColor();  // an invalid color means that
                              // the foreground color of the
                              // current character should
                              // be used

  else
    _cursorColor = color;
}

QColor TerminalView::keyboardCursorColor() const { return _cursorColor; }

void TerminalView::setScreenWindow(ScreenWindow* window) {
  // disconnect existing screen window if any
  if (_screenWindow) {
    disconnect(_screenWindow, nullptr, this, nullptr);
  }

  _screenWindow = window;

  if (window) {
    // TODO: Determine if this is an issue.
    // #warning "The order here is not specified - does it matter whether
    // updateImage or updateLineProperties comes first?"
    connect(_screenWindow, SIGNAL(outputChanged()), this,
            SLOT(updateLineProperties()));
    connect(_screenWindow, SIGNAL(outputChanged()), this, SLOT(updateImage()));
    connect(_screenWindow, SIGNAL(outputChanged()), this,
            SLOT(updateFilters()));
    connect(_screenWindow, SIGNAL(scrolled(int)), this, SLOT(updateFilters()));
    connect(_screenWindow, &ScreenWindow::scrollToEnd, this,
            &TerminalView::scrollToEnd);
    window->setWindowLines(_lines);
  }
}

ScreenWindow* TerminalView::getScreenWindow() const { return _screenWindow; }

void TerminalView::setMotionAfterPasting(MotionAfterPasting action) {
  mMotionAfterPasting = action;
}

int TerminalView::motionAfterPasting() { return mMotionAfterPasting; }

void TerminalView::setConfirmMultilinePaste(bool confirmMultilinePaste) {
  _confirmMultilinePaste = confirmMultilinePaste;
}

void TerminalView::setTrimPastedTrailingNewlines(
    bool trimPastedTrailingNewlines) {
  _trimPastedTrailingNewlines = trimPastedTrailingNewlines;
}

void TerminalView::getCharacterPosition(const QPointF& widgetPoint, int& line,
                                        int& column) const {
  line = (widgetPoint.y() - contentsRect().top() - _topMargin) / _fontHeight;
  if (line < 0) line = 0;
  if (line >= _usedLines) line = _usedLines - 1;

  int x =
      widgetPoint.x() + _fontWidth / 2 - contentsRect().left() - _leftMargin;
  if (_fixedFont)
    column = x / _fontWidth;
  else {
    column = 0;
    while (column + 1 < _usedColumns && x > textWidth(0, column + 1, line))
      column++;
  }

  if (column < 0) column = 0;

  // the column value returned can be equal to _usedColumns, which
  // is the position just after the last character displayed in a line.
  //
  // this is required so that the user can select characters in the right-most
  // column (or left-most for right-to-left input)
  if (column > _usedColumns) column = _usedColumns;
}

void TerminalView::focusIn() {
  emit termGetFocus();
  if (_hasBlinkingCursor) {
    _blinkCursorTimer->start();
  }
  updateCursor();

  if (_hasBlinker) _blinkTimer->start();
}

void TerminalView::focusOut() {
  emit termLostFocus();
  // trigger a repaint of the cursor so that it is both visible (in case
  // it was hidden during blinking)
  // and drawn in a focused out state
  _cursorBlinking = false;
  updateCursor();

  _blinkCursorTimer->stop();
  if (_blinking) blinkEvent();

  _blinkTimer->stop();
}

/* ------------------------------------------------------------------------- */
/*                                                                           */
/*                               Events handle                               */
/*                                                                           */
/* ------------------------------------------------------------------------- */
bool TerminalView::event(QEvent* event) {
  bool eventHandled = false;
  switch (event->type()) {
    case QEvent::ShortcutOverride:
      eventHandled = handleShortcutOverrideEvent((QKeyEvent*)event);
      break;
    case QEvent::PaletteChange:
    case QEvent::ApplicationPaletteChange:
      _scrollBar->setPalette(QApplication::palette());
      break;
    default:
      break;
  }
  return eventHandled ? true : QWidget::event(event);
}

void TerminalView::paintEvent(QPaintEvent* event) {
  QPainter paint(this);
  paint.setRenderHints(QPainter::SmoothPixmapTransform |
                       QPainter::Antialiasing | QPainter::TextAntialiasing);
  QRect cr = contentsRect();

  if (!_backgroundImage.isNull()) {
    QColor background = _colorTable[DEFAULT_BACK_COLOR].color;
    if (_opacity < static_cast<qreal>(1)) {
      background.setAlphaF(_opacity);
      paint.save();
      paint.setCompositionMode(QPainter::CompositionMode_Source);
      paint.fillRect(cr, background);
      paint.restore();
    } else {
      paint.fillRect(cr, background);
    }

    paint.save();
    paint.setRenderHints(QPainter::Antialiasing |
                         QPainter::SmoothPixmapTransform);

    if (_backgroundMode == STRETCH) {  // scale the image without keeping its
                                       // proportions to fill the screen
      paint.drawPixmap(cr, _backgroundImage, _backgroundImage.rect());
    } else if (_backgroundMode == ZOOM) {  // zoom in/out the image to fit it
      QRect r = _backgroundImage.rect();
      qreal wRatio = static_cast<qreal>(cr.width()) / r.width();
      qreal hRatio = static_cast<qreal>(cr.height()) / r.height();
      if (wRatio > hRatio) {
        r.setWidth(qRound(r.width() * hRatio));
        r.setHeight(cr.height());
      } else {
        r.setHeight(qRound(r.height() * wRatio));
        r.setWidth(cr.width());
      }
      r.moveCenter(cr.center());
      paint.drawPixmap(r, _backgroundImage, _backgroundImage.rect());
    } else if (_backgroundMode == FIT) {  // if the image is bigger than the
                                          // terminal, zoom it out to fit it
      QRect r = _backgroundImage.rect();
      qreal wRatio = static_cast<qreal>(cr.width()) / r.width();
      qreal hRatio = static_cast<qreal>(cr.height()) / r.height();
      if (r.width() > cr.width()) {
        if (wRatio <= hRatio) {
          r.setHeight(qRound(r.height() * wRatio));
          r.setWidth(cr.width());
        } else {
          r.setWidth(qRound(r.width() * hRatio));
          r.setHeight(cr.height());
        }
      } else if (r.height() > cr.height()) {
        r.setWidth(qRound(r.width() * hRatio));
        r.setHeight(cr.height());
      }
      r.moveCenter(cr.center());
      paint.drawPixmap(r, _backgroundImage, _backgroundImage.rect());
    } else if (_backgroundMode ==
               CENTER) {  // center the image without scaling/zooming
      QRect r = _backgroundImage.rect();
      r.moveCenter(cr.center());
      paint.drawPixmap(r.topLeft(), _backgroundImage);
    } else  // if (_backgroundMode == None)
    {
      paint.drawPixmap(0, 0, _backgroundImage);
    }

    paint.restore();
  }

  if (_drawTextTestFlag) {
    calDrawTextAdditionHeight(paint);
  }

  const QRegion regToDraw = event->region() & cr;
  for (auto rect = regToDraw.begin(); rect != regToDraw.end(); rect++) {
    drawBackground(paint, *rect, palette().window().color(),
                   true /* use opacity setting */);
    drawContents(paint, *rect);
  }

  drawInputMethodPreeditString(paint, preeditRect());
  paintFilters(paint);
  paint.end();
}

void TerminalView::showEvent(QShowEvent*) {
  emit changedContentSizeSignal(_contentHeight, _contentWidth);
}

void TerminalView::hideEvent(QHideEvent*) {
  emit changedContentSizeSignal(_contentHeight, _contentWidth);
}

void TerminalView::resizeEvent(QResizeEvent*) {
  updateImageSize();
  processFilters();
}

void TerminalView::fontChange(const QFont&) {
  QFontMetrics fm(font());
  _fontHeight = fm.height() + _lineSpacing;

  // waba TerminalDisplay 1.123:
  // "Base character width on widest ASCII character. This prevents too wide
  //  characters in the presence of double wide (e.g. Japanese) characters."
  // Get the width from representative normal width characters
  _fontWidth = qRound((double)fm.horizontalAdvance(QLatin1String(REPCHAR)) /
                      (double)qstrlen(REPCHAR));

  _fixedFont = true;

  int fw = fm.horizontalAdvance(QLatin1Char(REPCHAR[0]));
  for (unsigned int i = 1; i < qstrlen(REPCHAR); i++) {
    if (fw != fm.horizontalAdvance(QLatin1Char(REPCHAR[i]))) {
      _fixedFont = false;
      break;
    }
  }

  if (_fontWidth < 1) _fontWidth = 1;

  _fontAscend = fm.ascent();

  // static text
  _staticTextCache.clear();

  emit changedFontMetricSignal(_fontHeight, _fontWidth);
  propagateSize();

  // We will run paint event testing procedure.
  // Although this operation will destroy the original content,
  // the content will be drawn again after the test.
  _drawTextTestFlag = true;
  update();
}

void TerminalView::focusInEvent(QFocusEvent* event) { focusIn(); }

void TerminalView::focusOutEvent(QFocusEvent* event) { focusOut(); }

void TerminalView::keyPressEvent(QKeyEvent* event) {
  _actSel = 0;  // Key stroke implies a screen update, so TerminalDisplay won't
                // know where the current selection is.

  if (_hasBlinkingCursor) {
    _blinkCursorTimer->start(QApplication::cursorFlashTime() / 2);
    if (_cursorBlinking)
      blinkCursorEvent();
    else
      _cursorBlinking = false;
  }

  _screenWindow->clearSelection();

  emit keyPressedSignal(event, false);

  event->accept();
}

void TerminalView::mouseDoubleClickEvent(QMouseEvent* ev) {
  if (ev->button() != Qt::LeftButton) return;
  if (!_screenWindow) return;
  qDebug() << "Detected double click event, " << ev->pos().x() << ", "
           << ev->pos().y();

  int charLine = 0;
  int charColumn = 0;

  getCharacterPosition(ev->pos(), charLine, charColumn);

  QPoint pos(charColumn, charLine);

  // pass on double click as two clicks.
  if (!_mouseMarks && !(ev->modifiers() & Qt::ShiftModifier)) {
    // Send just _ONE_ click event, since the first click of the double click
    // was already sent by the click handler
    emit mouseSignal(0, pos.x() + 1,
                     pos.y() + 1 + _scrollBar->value() - _scrollBar->maximum(),
                     0);  // left button
    return;
  }

  _screenWindow->clearSelection();
  QPoint bgnSel = pos;
  QPoint endSel = pos;
  int i = loc(bgnSel.x(), bgnSel.y());
  _iPntSel = bgnSel;
  _iPntSel.ry() += _scrollBar->value();

  _wordSelectionMode = true;

  // find word boundaries...
  QChar selClass = charClass(_image[i].character);
  {
    // find the start of the word
    int x = bgnSel.x();
    while (((x > 0) || (bgnSel.y() > 0 &&
                        (_lineProperties[bgnSel.y() - 1] & LINE_WRAPPED))) &&
           charClass(_image[i - 1].character) == selClass) {
      i--;
      if (x > 0)
        x--;
      else {
        x = _usedColumns - 1;
        bgnSel.ry()--;
      }
    }

    bgnSel.setX(x);
    _screenWindow->setSelectionStart(bgnSel.x(), bgnSel.y(), false);

    // find the end of the word
    i = loc(endSel.x(), endSel.y());
    x = endSel.x();
    while (((x < _usedColumns - 1) ||
            (endSel.y() < _usedLines - 1 &&
             (_lineProperties[endSel.y()] & LINE_WRAPPED))) &&
           charClass(_image[i + 1].character) == selClass) {
      i++;
      if (x < _usedColumns - 1)
        x++;
      else {
        x = 0;
        endSel.ry()++;
      }
    }

    endSel.setX(x);

    // In word selection mode don't select @ (64) if at end of word.
    if ((QChar(_image[i].character) == QLatin1Char('@')) &&
        ((endSel.x() - bgnSel.x()) > 0))
      endSel.setX(x - 1);

    _actSel = 2;  // within selection

    _screenWindow->setSelectionEnd(endSel.x(), endSel.y());

    setSelection(_screenWindow->selectedText(_preserveLineBreaks));
  }

  _possibleTripleClick = true;

  QTimer::singleShot(QApplication::doubleClickInterval(), this,
                     SLOT(tripleClickTimeout()));
}

void TerminalView::mousePressEvent(QMouseEvent* ev) {
  qDebug() << "Detected mouse pressed. " << ev->pos().x() << ","
           << ev->pos().y();
  if (_possibleTripleClick && (ev->button() == Qt::LeftButton)) {
    mouseTripleClickEvent(ev);
    return;
  }

  if (!contentsRect().contains(ev->pos())) return;

  if (!_screenWindow) return;

  int charLine;
  int charColumn;
  getCharacterPosition(ev->pos(), charLine, charColumn);
  QPoint pos = QPoint(charColumn, charLine);

  if (ev->button() == Qt::LeftButton) {
    _lineSelectionMode = false;
    _wordSelectionMode = false;

    emit isBusySelecting(true);  // Keep it steady...
    // Drag only when the Control key is hold
    bool selected = false;

    // The receiver of the testIsSelected() signal will adjust
    // 'selected' accordingly.
    // emit testIsSelected(pos.x(), pos.y(), selected);

    selected = _screenWindow->isSelected(pos.x(), pos.y());

    if ((!_ctrlDrag || ev->modifiers() & Qt::ControlModifier) && selected) {
      // The user clicked inside selected text
      dragInfo.state = DI_PENDING;
      dragInfo.start = ev->pos();
    } else {
      // No reason to ever start a drag event
      dragInfo.state = DI_NONE;

      _preserveLineBreaks = !((ev->modifiers() & Qt::ControlModifier) &&
                              !(ev->modifiers() & Qt::AltModifier));
      _columnSelectionMode = (ev->modifiers() & Qt::AltModifier) &&
                             (ev->modifiers() & Qt::ControlModifier);

      if (_mouseMarks || (ev->modifiers() & Qt::ShiftModifier)) {
        _screenWindow->clearSelection();

        // emit clearSelectionSignal();
        pos.ry() += _scrollBar->value();
        _iPntSel = _pntSel = pos;
        _actSel = 1;  // left mouse button pressed but nothing selected yet.

      } else {
        emit mouseSignal(
            0, charColumn + 1,
            charLine + 1 + _scrollBar->value() - _scrollBar->maximum(), 0);
      }

      Filter::HotSpot* spot = _filterChain->hotSpotAt(charLine, charColumn);
      if (spot && spot->type() == Filter::HotSpot::Link)
        spot->activate(QLatin1String("click-action"));
    }
  } else if (ev->button() == Qt::MiddleButton) {
    if (_mouseMarks || (ev->modifiers() & Qt::ShiftModifier))
      emitSelection(true, ev->modifiers() & Qt::ControlModifier);
    else
      emit mouseSignal(
          1, charColumn + 1,
          charLine + 1 + _scrollBar->value() - _scrollBar->maximum(), 0);
  } else if (ev->button() == Qt::RightButton) {
    if (_mouseMarks || (ev->modifiers() & Qt::ShiftModifier))
      emit configureRequest(ev->pos());
    else
      emit mouseSignal(
          2, charColumn + 1,
          charLine + 1 + _scrollBar->value() - _scrollBar->maximum(), 0);
  }
}

void TerminalView::mouseReleaseEvent(QMouseEvent* ev) {
  qDebug() << "Detected mouse released.";
  if (!_screenWindow) return;

  int charLine;
  int charColumn;
  getCharacterPosition(ev->pos(), charLine, charColumn);

  if (ev->button() == Qt::LeftButton) {
    emit isBusySelecting(false);
    if (dragInfo.state == DI_PENDING) {
      // We had a drag event pending but never confirmed.  Kill selection
      _screenWindow->clearSelection();
      // emit clearSelectionSignal();
    } else {
      if (_actSel > 1) {
        setSelection(_screenWindow->selectedText(_preserveLineBreaks));
      }

      _actSel = 0;

      // FIXME: emits a release event even if the mouse is
      //       outside the range. The procedure used in `mouseMoveEvent'
      //       applies here, too.

      if (!_mouseMarks && !(ev->modifiers() & Qt::ShiftModifier))
        emit mouseSignal(
            0, charColumn + 1,
            charLine + 1 + _scrollBar->value() - _scrollBar->maximum(), 2);
    }
    dragInfo.state = DI_NONE;
  }

  if (!_mouseMarks && ((ev->button() == Qt::RightButton &&
                        !(ev->modifiers() & Qt::ShiftModifier)) ||
                       ev->button() == Qt::MiddleButton)) {
    emit mouseSignal(ev->button() == Qt::MiddleButton ? 1 : 2, charColumn + 1,
                     charLine + 1 + _scrollBar->value() - _scrollBar->maximum(),
                     2);
  }
}

void TerminalView::mouseMoveEvent(QMouseEvent* ev) {
  //  qDebug() << "Detected mouse move. " << ev->pos().x() << ", " <<
  //  ev->pos().y();
  int charLine = 0;
  int charColumn = 0;
  int leftMargin = _leftBaseMargin +
                   ((_scrollbarLocation == SCROLL_BAR_LEFT &&
                     !_scrollBar->style()->styleHint(
                         QStyle::SH_ScrollBar_Transient, nullptr, _scrollBar))
                        ? _scrollBar->width()
                        : 0);

  getCharacterPosition(ev->pos(), charLine, charColumn);

  // handle filters
  // change link hot-spot appearance on mouse-over
  Filter::HotSpot* spot = _filterChain->hotSpotAt(charLine, charColumn);
  if (spot && spot->type() == Filter::HotSpot::Link) {
    QRegion previousHotspotArea = _mouseOverHotspotArea;
    _mouseOverHotspotArea = QRegion();
    QRect r;
    if (spot->startLine() == spot->endLine()) {
      r.setCoords(spot->startColumn() * _fontWidth + leftMargin,
                  spot->startLine() * _fontHeight + _topBaseMargin,
                  spot->endColumn() * _fontWidth + leftMargin,
                  (spot->endLine() + 1) * _fontHeight - 1 + _topBaseMargin);
      _mouseOverHotspotArea |= r;
    } else {
      r.setCoords(spot->startColumn() * _fontWidth + leftMargin,
                  spot->startLine() * _fontHeight + _topBaseMargin,
                  _columns * _fontWidth - 1 + leftMargin,
                  (spot->startLine() + 1) * _fontHeight + _topBaseMargin);
      _mouseOverHotspotArea |= r;
      for (int line = spot->startLine() + 1; line < spot->endLine(); line++) {
        r.setCoords(0 * _fontWidth + leftMargin,
                    line * _fontHeight + _topBaseMargin,
                    _columns * _fontWidth + leftMargin,
                    (line + 1) * _fontHeight + _topBaseMargin);
        _mouseOverHotspotArea |= r;
      }
      r.setCoords(0 * _fontWidth + leftMargin,
                  spot->endLine() * _fontHeight + _topBaseMargin,
                  spot->endColumn() * _fontWidth + leftMargin,
                  (spot->endLine() + 1) * _fontHeight + _topBaseMargin);
      _mouseOverHotspotArea |= r;
    }

    update(_mouseOverHotspotArea | previousHotspotArea);
  } else if (!_mouseOverHotspotArea.isEmpty()) {
    update(_mouseOverHotspotArea);
    // set hotspot area to an invalid rectangle
    _mouseOverHotspotArea = QRegion();
  }

  // for auto-hiding the cursor, we need mouseTracking
  if (ev->buttons() == Qt::NoButton) return;

  // if the terminal is interested in mouse movements
  // then emit a mouse movement signal, unless the shift
  // key is being held down, which overrides this.
  if (!_mouseMarks && !(ev->modifiers() & Qt::ShiftModifier)) {
    int button = 3;
    if (ev->buttons() & Qt::LeftButton) button = 0;
    if (ev->buttons() & Qt::MiddleButton) button = 1;
    if (ev->buttons() & Qt::RightButton) button = 2;

    emit mouseSignal(button, charColumn + 1,
                     charLine + 1 + _scrollBar->value() - _scrollBar->maximum(),
                     1);

    return;
  }

  if (dragInfo.state == DI_PENDING) {
    // we had a mouse down, but haven't confirmed a drag yet
    // if the mouse has moved sufficiently, we will confirm

    //   int distance = KGlobalSettings::dndEventDelay();
    int distance = QApplication::startDragDistance();
    if (ev->position().x() > dragInfo.start.x() + distance ||
        ev->position().x() < dragInfo.start.x() - distance ||
        ev->position().y() > dragInfo.start.y() + distance ||
        ev->position().y() < dragInfo.start.y() - distance) {
      // we've left the drag square, we can start a real drag operation now
      emit isBusySelecting(false);  // Ok.. we can breath again.

      _screenWindow->clearSelection();
      doDrag();
    }
    return;
  } else if (dragInfo.state == DI_DRAGGING) {
    // this isn't technically needed because mouseMoveEvent is suppressed during
    // Qt drag operations, replaced by dragMoveEvent
    return;
  }

  if (_actSel == 0) return;

  // don't extend selection while pasting
  if (ev->buttons() & Qt::MiddleButton) return;

  extendSelection(ev->pos());
}

void TerminalView::extendSelection(const QPoint& position) {
  QPoint pos = position;

  if (!_screenWindow) return;

  // if ( !contentsRect().contains(ev->pos()) ) return;
  QPoint tL = contentsRect().topLeft();
  int tLx = tL.x();
  int tLy = tL.y();
  int scroll = _scrollBar->value();

  // we're in the process of moving the mouse with the left button pressed
  // the mouse cursor will kept caught within the bounds of the text in
  // this widget.

  int linesBeyondWidget = 0;

  QRect textBounds(tLx + _leftMargin, tLy + _topMargin,
                   _usedColumns * _fontWidth - 1, _usedLines * _fontHeight - 1);

  // Adjust position within text area bounds.
  QPoint oldpos = pos;

  pos.setX(qBound(textBounds.left(), pos.x(), textBounds.right()));
  pos.setY(qBound(textBounds.top(), pos.y(), textBounds.bottom()));

  if (oldpos.y() > textBounds.bottom()) {
    linesBeyondWidget = (oldpos.y() - textBounds.bottom()) / _fontHeight;
    _scrollBar->setValue(_scrollBar->value() + linesBeyondWidget +
                         1);  // scrollforward
  }
  if (oldpos.y() < textBounds.top()) {
    linesBeyondWidget = (textBounds.top() - oldpos.y()) / _fontHeight;
    _scrollBar->setValue(_scrollBar->value() - linesBeyondWidget -
                         1);  // history
  }

  int charColumn = 0;
  int charLine = 0;
  getCharacterPosition(pos, charLine, charColumn);

  QPoint here = QPoint(
      charColumn,
      charLine);  // QPoint((pos.x()-tLx-_leftMargin+(_fontWidth/2))/_fontWidth,(pos.y()-tLy-_topMargin)/_fontHeight);
  QPoint ohere;
  QPoint _iPntSelCorr = _iPntSel;
  _iPntSelCorr.ry() -= _scrollBar->value();
  QPoint _pntSelCorr = _pntSel;
  _pntSelCorr.ry() -= _scrollBar->value();
  bool swapping = false;

  if (_wordSelectionMode) {
    // Extend to word boundaries
    int i;
    QChar selClass;

    bool left_not_right =
        (here.y() < _iPntSelCorr.y() ||
         (here.y() == _iPntSelCorr.y() && here.x() < _iPntSelCorr.x()));
    bool old_left_not_right = (_pntSelCorr.y() < _iPntSelCorr.y() ||
                               (_pntSelCorr.y() == _iPntSelCorr.y() &&
                                _pntSelCorr.x() < _iPntSelCorr.x()));
    swapping = left_not_right != old_left_not_right;

    // Find left (left_not_right ? from here : from start)
    QPoint left = left_not_right ? here : _iPntSelCorr;
    i = loc(left.x(), left.y());
    if (i >= 0 && i <= _imageSize) {
      selClass = charClass(_image[i].character);
      while (
          ((left.x() > 0) ||
           (left.y() > 0 && (_lineProperties[left.y() - 1] & LINE_WRAPPED))) &&
          charClass(_image[i - 1].character) == selClass) {
        i--;
        if (left.x() > 0)
          left.rx()--;
        else {
          left.rx() = _usedColumns - 1;
          left.ry()--;
        }
      }
    }

    // Find right (left_not_right ? from start : from here)
    QPoint right = left_not_right ? _iPntSelCorr : here;
    i = loc(right.x(), right.y());
    if (i >= 0 && i <= _imageSize) {
      selClass = charClass(_image[i].character);
      while (((right.x() < _usedColumns - 1) ||
              (right.y() < _usedLines - 1 &&
               (_lineProperties[right.y()] & LINE_WRAPPED))) &&
             charClass(_image[i + 1].character) == selClass) {
        i++;
        if (right.x() < _usedColumns - 1)
          right.rx()++;
        else {
          right.rx() = 0;
          right.ry()++;
        }
      }
    }

    // Pick which is start (ohere) and which is extension (here)
    if (left_not_right) {
      here = left;
      ohere = right;
    } else {
      here = right;
      ohere = left;
    }
    ohere.rx()++;
  }

  if (_lineSelectionMode) {
    // Extend to complete line
    bool above_not_below = (here.y() < _iPntSelCorr.y());

    QPoint above = above_not_below ? here : _iPntSelCorr;
    QPoint below = above_not_below ? _iPntSelCorr : here;

    while (above.y() > 0 && (_lineProperties[above.y() - 1] & LINE_WRAPPED))
      above.ry()--;
    while (below.y() < _usedLines - 1 &&
           (_lineProperties[below.y()] & LINE_WRAPPED))
      below.ry()++;

    above.setX(0);
    below.setX(_usedColumns - 1);

    // Pick which is start (ohere) and which is extension (here)
    if (above_not_below) {
      here = above;
      ohere = below;
    } else {
      here = below;
      ohere = above;
    }

    QPoint newSelBegin = QPoint(ohere.x(), ohere.y());
    swapping = !(_tripleSelBegin == newSelBegin);
    _tripleSelBegin = newSelBegin;

    ohere.rx()++;
  }

  int offset = 0;
  if (!_wordSelectionMode && !_lineSelectionMode) {
    int i;
    QChar selClass;

    bool left_not_right =
        (here.y() < _iPntSelCorr.y() ||
         (here.y() == _iPntSelCorr.y() && here.x() < _iPntSelCorr.x()));
    bool old_left_not_right = (_pntSelCorr.y() < _iPntSelCorr.y() ||
                               (_pntSelCorr.y() == _iPntSelCorr.y() &&
                                _pntSelCorr.x() < _iPntSelCorr.x()));
    swapping = left_not_right != old_left_not_right;

    // Find left (left_not_right ? from here : from start)
    QPoint left = left_not_right ? here : _iPntSelCorr;

    // Find left (left_not_right ? from start : from here)
    QPoint right = left_not_right ? _iPntSelCorr : here;
    if (right.x() > 0 && !_columnSelectionMode) {
      i = loc(right.x(), right.y());
      if (i >= 0 && i <= _imageSize) {
        selClass = charClass(_image[i - 1].character);
        /* if (selClass == ' ')
         {
           while ( right.x() < _usedColumns-1 &&
         charClass(_image[i+1].character) == selClass &&
         (right.y()<_usedLines-1) &&
                           !(_lineProperties[right.y()] & LINE_WRAPPED))
           { i++; right.rx()++; }
           if (right.x() < _usedColumns-1)
             right = left_not_right ? _iPntSelCorr : here;
           else
             right.rx()++;  // will be balanced later because of offset=-1;
         }*/
      }
    }

    // Pick which is start (ohere) and which is extension (here)
    if (left_not_right) {
      here = left;
      ohere = right;
      offset = 0;
    } else {
      here = right;
      ohere = left;
      offset = -1;
    }
  }

  if ((here == _pntSelCorr) && (scroll == _scrollBar->value()))
    return;  // not moved

  if (here == ohere) return;  // It's not left, it's not right.

  if (_actSel < 2 || swapping) {
    if (_columnSelectionMode && !_lineSelectionMode && !_wordSelectionMode) {
      _screenWindow->setSelectionStart(ohere.x(), ohere.y(), true);
    } else {
      _screenWindow->setSelectionStart(ohere.x() - 1 - offset, ohere.y(),
                                       false);
    }
  }

  _actSel = 2;  // within selection
  _pntSel = here;
  _pntSel.ry() += _scrollBar->value();

  if (_columnSelectionMode && !_lineSelectionMode && !_wordSelectionMode) {
    _screenWindow->setSelectionEnd(here.x(), here.y());
  } else {
    _screenWindow->setSelectionEnd(here.x() + offset, here.y());
  }
}

void TerminalView::wheelEvent(QWheelEvent* ev) {
  qDebug() << "Detected wheel event. y " << ev->angleDelta().y();
  if (ev->angleDelta().y() == 0) return;
  if (!_screenWindow || !_screenWindow->screen()) return;
  if (_screenWindow->screen()->getHistLines() == 0) return;
  qDebug() << "Detected wheel event. y " << ev->angleDelta().y();

  // if the terminal program is not interested mouse events
  // then send the event to the scrollbar if the slider has room to move
  // or otherwise send simulated up / down key presses to the terminal program
  // for the benefit of programs such as 'less'
  if (_mouseMarks) {
    bool canScroll = _scrollBar->maximum() > 0;
    if (canScroll)
      _scrollBar->event(ev);
    else {
      // assume that each Up / Down key event will cause the terminal
      // application to scroll by one line.
      //
      // to get a reasonable scrolling speed, scroll by one line for every 5
      // degrees of mouse wheel rotation.  Mouse wheels typically move in steps
      // of 15 degrees, giving a scroll of 3 lines
      int key = ev->angleDelta().y() > 0 ? Qt::Key_Up : Qt::Key_Down;

      // QWheelEvent::angleDelta().y() gives rotation in eighths of a degree
      int wheelDegrees = ev->angleDelta().y() / 8;
      int linesToScroll = abs(wheelDegrees) / 5;

      QKeyEvent keyScrollEvent(QEvent::KeyPress, key, Qt::NoModifier);

      for (int i = 0; i < linesToScroll; i++)
        emit keyPressedSignal(&keyScrollEvent, false);
    }
  } else {
    // terminal program wants notification of mouse activity

    int charLine;
    int charColumn;
    getCharacterPosition(ev->position(), charLine, charColumn);

    emit mouseSignal(ev->angleDelta().y() > 0 ? 4 : 5, charColumn + 1,
                     charLine + 1 + _scrollBar->value() - _scrollBar->maximum(),
                     0);
  }
}

bool TerminalView::focusNextPrevChild(bool next) { return false; }

void TerminalView::mouseTripleClickEvent(QMouseEvent* ev) {
  if (!_screenWindow) return;

  int charLine;
  int charColumn;
  getCharacterPosition(ev->pos(), charLine, charColumn);
  _iPntSel = QPoint(charColumn, charLine);

  _screenWindow->clearSelection();

  _lineSelectionMode = true;
  _wordSelectionMode = false;

  _actSel = 2;                 // within selection
  emit isBusySelecting(true);  // Keep it steady...

  while (_iPntSel.y() > 0 && (_lineProperties[_iPntSel.y() - 1] & LINE_WRAPPED))
    _iPntSel.ry()--;

  if (_tripleClickMode == SELECT_FORWARDS_FROM_CURSOR) {
    // find word boundary start
    int i = loc(_iPntSel.x(), _iPntSel.y());
    QChar selClass = charClass(_image[i].character);
    int x = _iPntSel.x();

    while (((x > 0) || (_iPntSel.y() > 0 &&
                        (_lineProperties[_iPntSel.y() - 1] & LINE_WRAPPED))) &&
           charClass(_image[i - 1].character) == selClass) {
      i--;
      if (x > 0)
        x--;
      else {
        x = _columns - 1;
        _iPntSel.ry()--;
      }
    }

    _screenWindow->setSelectionStart(x, _iPntSel.y(), false);
    _tripleSelBegin = QPoint(x, _iPntSel.y());
  } else if (_tripleClickMode == SELECT_WHOLE_LINE) {
    _screenWindow->setSelectionStart(0, _iPntSel.y(), false);
    _tripleSelBegin = QPoint(0, _iPntSel.y());
  }

  while (_iPntSel.y() < _lines - 1 &&
         (_lineProperties[_iPntSel.y()] & LINE_WRAPPED))
    _iPntSel.ry()++;

  _screenWindow->setSelectionEnd(_columns - 1, _iPntSel.y());

  setSelection(_screenWindow->selectedText(_preserveLineBreaks));

  _iPntSel.ry() += _scrollBar->value();
}

/* ------------------------------------------------------------------------- */
/*                                                                           */
/*                               Grag And Drop                               */
/*                                                                           */
/* ------------------------------------------------------------------------- */
void TerminalView::dragEnterEvent(QDragEnterEvent* event) {
  if (event->mimeData()->hasFormat(QLatin1String("text/plain")))
    event->acceptProposedAction();
  if (event->mimeData()->urls().count()) event->acceptProposedAction();
}

void TerminalView::dropEvent(QDropEvent* event) {
  // KUrl::List urls = KUrl::List::fromMimeData(event->mimeData());
  QList<QUrl> urls = event->mimeData()->urls();

  QString dropText;
  if (!urls.isEmpty()) {
    // TODO/FIXME: escape or quote pasted things if necessary...
    qDebug() << "TerminalDisplay: handling urls. It can be broken. Report any "
                "errors, please";
    for (int i = 0; i < urls.count(); i++) {
      // KUrl url = KIO::NetAccess::mostLocalUrl( urls[i] , 0 );
      QUrl url = urls[i];

      QString urlText;

      if (url.isLocalFile())
        urlText = url.path();
      else
        urlText = url.toString();

      // in future it may be useful to be able to insert file names with
      // drag-and-drop without quoting them (this only affects paths with spaces
      // in)
      // urlText = KShell::quoteArg(urlText);

      QChar q(QLatin1Char('\''));
      dropText += q + QString(urlText).replace(q, QLatin1String("'\\''")) + q;
      dropText += QLatin1Char(' ');
    }
  } else {
    dropText = event->mimeData()->text();

    dropText.replace(QLatin1String("\r\n"), QLatin1String("\n"));
    dropText.replace(QLatin1Char('\n'), QLatin1Char('\r'));
    if (_trimPastedTrailingNewlines) {
      dropText.replace(rRegularExpression, QString());
    }
    if (_confirmMultilinePaste && dropText.contains(QLatin1Char('\r'))) {
      if (!multilineConfirmation(dropText)) {
        return;
      }
    }
  }

  emit sendStringToEmu(dropText.toLocal8Bit().constData());
}

void TerminalView::doDrag() {
  dragInfo.state = DI_DRAGGING;
  dragInfo.dragObject = new QDrag(this);
  QMimeData* mimeData = new QMimeData;
  mimeData->setText(QApplication::clipboard()->text(QClipboard::Selection));
  dragInfo.dragObject->setMimeData(mimeData);
  dragInfo.dragObject->exec(Qt::CopyAction);
  // Don't delete the QTextDrag object.  Qt will delete it when it's done with
  // it.
}

QChar TerminalView::charClass(QChar qch) const {
  if (qch.isSpace()) return QLatin1Char(' ');

  if (qch.isLetterOrNumber() ||
      _wordCharacters.contains(qch, Qt::CaseInsensitive))
    return QLatin1Char('a');

  return qch;
}

void TerminalView::updateImage() {
  if (!_screenWindow) return;

  // optimization - scroll the existing image where possible and
  // avoid expensive text drawing for parts of the image that
  // can simply be moved up or down
  scrollImage(_screenWindow->scrollCount(), _screenWindow->scrollRegion());
  _screenWindow->resetScrollCount();

  if (!_image) {
    // Create _image.
    // The emitted changedContentSizeSignal also leads to getImage being
    // recreated, so do this first.
    updateImageSize();
  }

  Character* const newimg = _screenWindow->getImage();
  int lines = _screenWindow->windowLines();
  int columns = _screenWindow->windowColumns();

  setScroll(_screenWindow->currentLine(), _screenWindow->lineCount());

  Q_ASSERT(this->_usedLines <= this->_lines);
  Q_ASSERT(this->_usedColumns <= this->_columns);

  int y, x, len;

  QPoint tL = contentsRect().topLeft();
  int tLx = tL.x();
  int tLy = tL.y();
  _hasBlinker = false;

  CharacterColor cf;          // undefined
  CharacterColor _clipboard;  // undefined
  int cr = -1;                // undefined

  const int linesToUpdate = qMin(this->_lines, qMax(0, lines));
  const int columnsToUpdate = qMin(this->_columns, qMax(0, columns));

  wchar_t* disstrU = new wchar_t[columnsToUpdate];
  char* dirtyMask = new char[columnsToUpdate + 2];
  QRegion dirtyRegion;

  // debugging variable, this records the number of lines that are found to
  // be 'dirty' ( ie. have changed from the old _image to the new _image ) and
  // which therefore need to be repainted
  int dirtyLineCount = 0;

  for (y = 0; y < linesToUpdate; ++y) {
    const Character* currentLine = &_image[y * this->_columns];
    Character* const newLine = &newimg[y * columns];

    bool updateLine = false;

    // The dirty mask indicates which characters need repainting. We also
    // mark surrounding neighbours dirty, in case the character exceeds
    // its cell boundaries
    memset(dirtyMask, 0, columnsToUpdate + 2);

    for (x = 0; x < columnsToUpdate; ++x) {
      if (newLine[x] != currentLine[x]) {
        dirtyMask[x] = true;
      }
    }

    if (!_resizing)  // not while _resizing, we're expecting a paintEvent
      for (x = 0; x < columnsToUpdate; ++x) {
        _hasBlinker |= (newLine[x].rendition & RE_BLINK);

        // Start drawing if this character or the next one differs.
        // We also take the next one into account to handle the situation
        // where characters exceed their cell width.
        if (dirtyMask[x]) {
          wchar_t c = newLine[x + 0].character;
          if (!c) continue;
          int p = 0;
          disstrU[p++] = c;  // fontMap(c);
          bool lineDraw = isLineChar(c);
          bool doubleWidth = (x + 1 == columnsToUpdate)
                                 ? false
                                 : (newLine[x + 1].character == 0);
          cr = newLine[x].rendition;
          _clipboard = newLine[x].backgroundColor;
          if (newLine[x].foregroundColor != cf) cf = newLine[x].foregroundColor;
          int lln = columnsToUpdate - x;
          for (len = 1; len < lln; ++len) {
            const Character& ch = newLine[x + len];

            if (!ch.character)
              continue;  // Skip trailing part of multi-col chars.

            bool nextIsDoubleWidth =
                (x + len + 1 == columnsToUpdate)
                    ? false
                    : (newLine[x + len + 1].character == 0);

            if (ch.foregroundColor != cf || ch.backgroundColor != _clipboard ||
                ch.rendition != cr || !dirtyMask[x + len] ||
                isLineChar(c) != lineDraw || nextIsDoubleWidth != doubleWidth)
              break;

            disstrU[p++] = c;  // fontMap(c);
          }

          std::wstring unistr(disstrU, p);

          bool saveFixedFont = _fixedFont;
          if (lineDraw) _fixedFont = false;
          if (doubleWidth) _fixedFont = false;

          updateLine = true;

          _fixedFont = saveFixedFont;
          x += len - 1;
        }
      }

    // both the top and bottom halves of double height _lines must always be
    // redrawn although both top and bottom halves contain the same characters,
    // only the top one is actually drawn.
    if (_lineProperties.count() > y)
      updateLine |= (_lineProperties[y] & LINE_DOUBLEHEIGHT);

    // if the characters on the line are different in the old and the new _image
    // then this line must be repainted.
    if (updateLine) {
      dirtyLineCount++;

      // add the area occupied by this line to the region which needs to be
      // repainted
      QRect dirtyRect =
          QRect(_leftMargin + tLx, _topMargin + tLy + _fontHeight * y,
                _fontWidth * columnsToUpdate, _fontHeight);

      dirtyRegion |= dirtyRect;
    }

    // replace the line of characters in the old _image with the
    // current line of the new _image
    memcpy((void*)currentLine, (const void*)newLine,
           columnsToUpdate * sizeof(Character));
  }

  // if the new _image is smaller than the previous _image, then ensure that the
  // area outside the new _image is cleared
  if (linesToUpdate < _usedLines) {
    dirtyRegion |=
        QRect(_leftMargin + tLx, _topMargin + tLy + _fontHeight * linesToUpdate,
              _fontWidth * this->_columns,
              _fontHeight * (_usedLines - linesToUpdate));
  }
  _usedLines = linesToUpdate;

  if (columnsToUpdate < _usedColumns) {
    dirtyRegion |=
        QRect(_leftMargin + tLx + columnsToUpdate * _fontWidth,
              _topMargin + tLy, _fontWidth * (_usedColumns - columnsToUpdate),
              _fontHeight * this->_lines);
  }
  _usedColumns = columnsToUpdate;

  dirtyRegion |= _inputMethodData.previousPreeditRect;

  // update the parts of the display which have changed
  update(dirtyRegion);

  if (_hasBlinker && !_blinkTimer->isActive())
    _blinkTimer->start(TEXT_BLINK_DELAY);
  if (!_hasBlinker && _blinkTimer->isActive()) {
    _blinkTimer->stop();
    _blinking = false;
  }
  delete[] dirtyMask;
  delete[] disstrU;
}

void TerminalView::updateFilters() {
  if (!_screenWindow) return;

  processFilters();
}

void TerminalView::setUsesMouse(bool on) {
  if (_mouseMarks != on) {
    _mouseMarks = on;
    setCursor(_mouseMarks ? Qt::IBeamCursor : Qt::ArrowCursor);
    emit usesMouseChanged();
  }
}

bool TerminalView::usesMouse() const { return _mouseMarks; }

void TerminalView::setBracketedPasteMode(bool on) { _bracketedPasteMode = on; }

bool TerminalView::bracketedPasteMode() const { return _bracketedPasteMode; }

void TerminalView::bell(const QString& message) {
  if (_bellMode == NO_BELL) return;

  // limit the rate at which bells can occur
  //...mainly for sound effects where rapid bells in sequence
  // produce a horrible noise
  if (_allowBell) {
    _allowBell = false;
    QTimer::singleShot(500, this, SLOT(enableBell()));

    if (_bellMode == SYSTEM_BEEP_BELL) {
      QApplication::beep();
    } else if (_bellMode == NOTIFY_BELL) {
      emit notifyBell(message);
    } else if (_bellMode == VISUAL_BELL) {
      swapColorTable();
      QTimer::singleShot(200, this, SLOT(swapColorTable()));
    }
  }
}

void TerminalView::setBackgroundColor(const QColor& color) {
  _colorTable[DEFAULT_BACK_COLOR].color = color;
  QPalette p = palette();
  p.setColor(backgroundRole(), color);
  setPalette(p);

  // Avoid propagating the palette change to the scroll bar
  _scrollBar->setPalette(QApplication::palette());

  update();
}

void TerminalView::setForegroundColor(const QColor& color) {
  _colorTable[DEFAULT_FORE_COLOR].color = color;

  update();
}

void TerminalView::selectionChanged() {
  emit copyAvailable(_screenWindow->selectedText(false).isEmpty() == false);
}

void TerminalView::updateLineProperties() {
  if (!_screenWindow) return;

  _lineProperties = _screenWindow->getLineProperties();
}

void TerminalView::copyClipboard() {
  if (!_screenWindow) return;

  QString text = _screenWindow->selectedText(_preserveLineBreaks);
  if (!text.isEmpty()) QApplication::clipboard()->setText(text);
}

void TerminalView::pasteClipboard() { emitSelection(false, false); }

void TerminalView::pasteSelection() { emitSelection(true, false); }

void TerminalView::outputSuspended(bool suspended) {
  // create the label when this function is first called
  if (!_outputSuspendedLabel) {
    // This label includes a link to an English language website
    // describing the 'flow control' (Xon/Xoff) feature found in almost
    // all terminal emulators.
    // If there isn't a suitable article available in the target language the
    // link can simply be removed.
    _outputSuspendedLabel = new QLabel(
        tr("<qt>Output has been "
           "<a href=\"http://en.wikipedia.org/wiki/Flow_control\">suspended</a>"
           " by pressing Ctrl+S."
           "  Press <b>Ctrl+Q</b> to resume.</qt>"),
        this);

    QPalette palette(_outputSuspendedLabel->palette());
    // KColorScheme::adjustBackground(palette,KColorScheme::NeutralBackground);
    _outputSuspendedLabel->setPalette(palette);
    _outputSuspendedLabel->setAutoFillBackground(true);
    _outputSuspendedLabel->setBackgroundRole(QPalette::Base);
    _outputSuspendedLabel->setFont(QApplication::font());
    _outputSuspendedLabel->setContentsMargins(5, 5, 5, 5);

    // enable activation of "Xon/Xoff" link in label
    _outputSuspendedLabel->setTextInteractionFlags(
        Qt::LinksAccessibleByMouse | Qt::LinksAccessibleByKeyboard);
    _outputSuspendedLabel->setOpenExternalLinks(true);
    _outputSuspendedLabel->setVisible(false);

    _gridLayout->addWidget(_outputSuspendedLabel);
    _gridLayout->addItem(
        new QSpacerItem(0, 0, QSizePolicy::Expanding, QSizePolicy::Expanding),
        1, 0);
  }

  _outputSuspendedLabel->setVisible(suspended);
}

TerminalView::TerminalView(QWidget* parent)
    : QWidget(parent),
      _nativeCanvas(nullptr),
      _screenWindow(nullptr),
      _gridLayout(nullptr),
      _allowBell(true),
      _boldIntense(true),
      _fixedFont(true),
      _fontHeight(1),
      _fontWidth(1),
      _fontAscend(1),
      _drawTextAdditionHeight(0),
      _leftBaseMargin(5),
      _topBaseMargin(5),
      // staitc text
      _staticTextCache(2 << 15),
      _lines(1),
      _columns(1),
      _usedLines(1),
      _usedColumns(1),
      _contentHeight(1),
      _contentWidth(1),
      _image(nullptr),
      _randomSeed(0),
      _resizing(false),
      _terminalSizeHint(false),
      _terminalSizeStartup(true),
      _bidiEnabled(false),
      _mouseMarks(false),
      _disabledBracketedPasteMode(false),
      _actSel(0),
      _wordSelectionMode(false),
      _lineSelectionMode(false),
      _preserveLineBreaks(false),
      _columnSelectionMode(false),
      _scrollbarLocation(ScrollBarPosition::NO_SCROLL_BAR),
      _wordCharacters(QLatin1String(":@-./_~")),
      _bellMode(BellMode::SYSTEM_BEEP_BELL),
      _blinking(false),
      _hasBlinker(false),
      _cursorBlinking(false),
      _hasBlinkingCursor(false),
      _allowBlinkingText(true),
      _ctrlDrag(false),
      _isFixedSize(false),
      _possibleTripleClick(false),
      _tripleClickMode(TripleClickMode::SELECT_WHOLE_LINE),
      _colorsInverted(false),
      _resizeWidget(nullptr),
      _resizeTimer(nullptr),
      _outputSuspendedLabel(nullptr),
      _lineSpacing(0),
      _opacity(static_cast<qreal>(1)),
      _backgroundMode(BackgroundMode::NONE),
      _filterChain(new TerminalImageFilterChain()),
      _cursorShape(KeyboardCursorShape::BLOCK_CURSOR),
      mMotionAfterPasting(MotionAfterPasting::NO_MOVE_SCREEN_WINDOW),
      _drawLineChars(true) {
  _drawTextAdditionHeight = 0;
  _drawTextTestFlag = false;

  setLayoutDirection(Qt::LeftToRight);

  // offsets are not calculate yet.
  _topMargin = _topBaseMargin;
  _leftMargin = _leftBaseMargin;

  // Scroll Bar:
  _scrollBar = new QScrollBar(this);
  if (!_scrollBar->style()->styleHint(QStyle::SH_ScrollBar_Transient, nullptr,
                                      _scrollBar))
    _scrollBar->setAutoFillBackground(true);
  setScroll(0, 0);
  _scrollBar->setCursor(Qt::ArrowCursor);
  _scrollBar->setAttribute(Qt::WA_Hover, true);
  _scrollBar->setMouseTracking(true);
  connect(_scrollBar, SIGNAL(valueChanged(int)), this,
          SLOT(scrollBarPositionChanged(int)));
  _scrollBar->hide();

  QFile qssFile(qssFilePath);
  if (qssFile.exists() && qssFile.open(QFileDevice::ReadOnly)) {
    QString styleSheet = qssFile.readAll();
    _scrollBar->setStyleSheet(styleSheet);
    qssFile.close();
  }

  // set timers for blinking cursor and text
  _blinkTimer = new QTimer(this);
  connect(_blinkTimer, SIGNAL(timeout()), this, SLOT(blinkEvent()));
  _blinkCursorTimer = new QTimer(this);
  connect(_blinkCursorTimer, SIGNAL(timeout()), this, SLOT(blinkCursorEvent()));

  setUsesMouse(true);
  setBracketedPasteMode(false);
  setColorTable(base_color_table);
  setMouseTracking(true);

  // Enable drag and drop
  setAcceptDrops(true);  // attempt
  dragInfo.state = DI_NONE;

  setFocusPolicy(Qt::WheelFocus);

  // enable input method support
  setAttribute(Qt::WA_InputMethodEnabled, true);

  // this is an important optimization, it tells Qt
  // that TerminalDisplay will handle repainting its entire area.
  setAttribute(Qt::WA_OpaquePaintEvent);

  _gridLayout = new QGridLayout(this);
  _gridLayout->setContentsMargins(0, 0, 0, 0);

  setLayout(_gridLayout);

  new AutoScrollHandler(this);
}

TerminalView::~TerminalView() {
  disconnect(_blinkTimer);
  disconnect(_blinkCursorTimer);
  qApp->removeEventFilter(this);

  delete[] _image;

  delete _gridLayout;
  delete _outputSuspendedLabel;
  delete _filterChain;
}

void TerminalView::setNativeCanvas(nativers::SharedCanvas* nativeCanvas) {
  _nativeCanvas = nativeCanvas;
}

const ColorEntry* TerminalView::getColorTable() const { return _colorTable; }

void TerminalView::setColorTable(const ColorEntry table[]) {
  for (int i = 0; i < TABLE_COLORS; i++) _colorTable[i] = table[i];

  setBackgroundColor(_colorTable[DEFAULT_BACK_COLOR].color);
}

void TerminalView::setRandomSeed(uint seed) { _randomSeed = seed; }

uint TerminalView::randomSeed() const { return _randomSeed; }

void TerminalView::setOpacity(qreal opacity) {
  this->_opacity =
      qBound(static_cast<qreal>(0), opacity, static_cast<qreal>(1));
}

void TerminalView::setBackgroundImage(const QString& backgroundImage) {
  if (!backgroundImage.isEmpty()) {
    this->_backgroundImage.load(backgroundImage);
    setAttribute(Qt::WA_OpaquePaintEvent, false);
  } else {
    this->_backgroundImage = QPixmap();
    setAttribute(Qt::WA_OpaquePaintEvent, true);
  }
}

void TerminalView::setBackgroundMode(BackgroundMode mode) {
  _backgroundMode = mode;
}

void TerminalView::setScrollBarPosition(ScrollBarPosition position) {
  if (_scrollbarLocation == position) return;

  if (position == NO_SCROLL_BAR)
    _scrollBar->hide();
  else
    _scrollBar->show();

  _topMargin = _leftMargin = 1;
  _scrollbarLocation = position;

  propagateSize();
  update();
}

/* ------------------------------------------------------------------------- */
/*                                                                           */
/*                             AutoScrollHandler                             */
/*                                                                           */
/* ------------------------------------------------------------------------- */
AutoScrollHandler::AutoScrollHandler(QWidget* parent)
    : QObject(parent), _timerId(0) {
  parent->installEventFilter(this);
}

void AutoScrollHandler::timerEvent(QTimerEvent* event) {
  if (event->timerId() != _timerId) return;

  QMouseEvent mouseEvent(QEvent::MouseMove,
                         widget()->mapFromGlobal(QCursor::pos()), Qt::NoButton,
                         Qt::LeftButton, Qt::NoModifier);

  QApplication::sendEvent(widget(), &mouseEvent);
}

bool AutoScrollHandler::eventFilter(QObject* watched, QEvent* event) {
  Q_ASSERT(watched == parent());
  Q_UNUSED(watched);

  QMouseEvent* mouseEvent = (QMouseEvent*)event;
  switch (event->type()) {
    case QEvent::MouseMove: {
      bool mouseInWidget = widget()->rect().contains(mouseEvent->pos());

      if (mouseInWidget) {
        if (_timerId) killTimer(_timerId);
        _timerId = 0;
      } else {
        if (!_timerId && (mouseEvent->buttons() & Qt::LeftButton))
          _timerId = startTimer(100);
      }
      break;
    }
    case QEvent::MouseButtonRelease:
      if (_timerId && (mouseEvent->buttons() & ~Qt::LeftButton)) {
        killTimer(_timerId);
        _timerId = 0;
      }
      break;
    default:
      break;
  };

  return false;
}

/* ------------------------------------------------------------------------- */
/*                                                                           */
/*                             ExtendedCharTable                             */
/*                                                                           */
/* ------------------------------------------------------------------------- */
ushort ExtendedCharTable::extendedCharHash(ushort* unicodePoints,
                                           ushort length) const {
  ushort hash = 0;
  for (ushort i = 0; i < length; i++) {
    hash = 31 * hash + unicodePoints[i];
  }
  return hash;
}
bool ExtendedCharTable::extendedCharMatch(ushort hash, ushort* unicodePoints,
                                          ushort length) const {
  ushort* entry = extendedCharTable[hash];

  // compare given length with stored sequence length ( given as the first
  // ushort in the stored buffer )
  if (entry == nullptr || entry[0] != length) return false;
  // if the lengths match, each character must be checked.  the stored buffer
  // starts at entry[1]
  for (int i = 0; i < length; i++) {
    if (entry[i + 1] != unicodePoints[i]) return false;
  }
  return true;
}
ushort ExtendedCharTable::createExtendedChar(ushort* unicodePoints,
                                             ushort length) {
  // look for this sequence of points in the table
  ushort hash = extendedCharHash(unicodePoints, length);

  // check existing entry for match
  while (extendedCharTable.contains(hash)) {
    if (extendedCharMatch(hash, unicodePoints, length)) {
      // this sequence already has an entry in the table,
      // return its hash
      return hash;
    } else {
      // if hash is already used by another, different sequence of unicode
      // character points then try next hash
      hash++;
    }
  }

  // add the new sequence to the table and
  // return that index
  ushort* buffer = new ushort[length + 1];
  buffer[0] = length;
  for (int i = 0; i < length; i++) buffer[i + 1] = unicodePoints[i];

  extendedCharTable.insert(hash, buffer);

  return hash;
}

ushort* ExtendedCharTable::lookupExtendedChar(ushort hash,
                                              ushort& length) const {
  // lookup index in table and if found, set the length
  // argument and return a pointer to the character sequence

  ushort* buffer = extendedCharTable[hash];
  if (buffer) {
    length = buffer[0];
    return buffer + 1;
  } else {
    length = 0;
    return nullptr;
  }
}

ExtendedCharTable::ExtendedCharTable() {}
ExtendedCharTable::~ExtendedCharTable() {
  // free all allocated character buffers
  QHashIterator<ushort, ushort*> iter(extendedCharTable);
  while (iter.hasNext()) {
    iter.next();
    delete[] iter.value();
  }
}

// global instance
ExtendedCharTable ExtendedCharTable::instance;
