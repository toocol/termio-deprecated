#include "terminalview.h"

#include <QApplication>
#include <QPainter>
#include <QStyle>

#include "wcwidth.h"

using namespace TConsole;

#ifndef loc
#define loc(X, Y) ((Y)*_columns + (X))
#endif

#define TEXT_BLINK_DELAY 500

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
static void drawLineChar(QPainter &paint, int x, int y, int w, int h,
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

static void drawOtherChar(QPainter &paint, int x, int y, int w, int h,
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

void TerminalView::blinkEvent() {}

void TerminalView::blinkCursorEvent() {}

void TerminalView::enableBell() {}

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

void TerminalView::drawContents(QPainter &painter, const QRect &rect) {
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
  for (int y = luy; y <= rly; y++) {
    quint32 c = _image[loc(lux, y)].character;
    int x = lux;
    if (!c && x) x--;  // Search for start of multi-column character
    for (; x <= rlx; x++) {
      int len = 1;
      int p = 0;

      // reset our buffer to the maximal size
      unistr.resize(bufferSize);

      // is this a single character or a sequence of characters ?
      if (_image[loc(x, y)].rendition & RE_EXTENDED_CHAR) {
        // sequence of characters
        ushort extendedCharLength = 0;
        ushort *chars = ExtendedCharTable::instance.lookupExtendedChar(
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

void TerminalView::drawTextFragment(QPainter &painter, const QRect &rect,
                                    const std::wstring &text,
                                    const Character *style) {
  painter.save();
  // setup painter
  const QColor foregroundColor = style->foregroundColor.color(_colorTable);
  const QColor backgroundColor = style->backgroundColor.color(_colorTable);

  if (backgroundColor != palette().window().color())
    drawBackground(painter, rect, backgroundColor, false);

  bool invertCharacterColor = false;
  if (style->rendition & RE_CURSOR)
    drawCursor(painter, rect, foregroundColor, backgroundColor,
               invertCharacterColor);

  // draw text
  drawCharacters(painter, rect, text, style, invertCharacterColor);

  painter.restore();
}

void TerminalView::drawBackground(QPainter &painter, const QRect &rect,
                                  const QColor &backgroundColor,
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

void TerminalView::drawCursor(QPainter &painter, const QRect &rect,
                              const QColor &foregroundColor,
                              const QColor &backgroundColor,
                              bool &invertCharacterColor) {
  QRectF cursorRect = rect;
  cursorRect.setHeight(_fontHeight - _lineSpacing - 1);

  if (!_cursorBlinking) {
    if (_cursorColor.isValid())
      painter.setPen(_cursorColor);
    else
      painter.setPen(foregroundColor);

    if (_cursorShape == CursorShape::BLOCK_CURSOR) {
      // draw the cursor outline, adjusting the area so that
      // it is draw entirely inside 'rect'
      float penWidth = qMax(1, painter.pen().width());

      painter.drawRect(cursorRect.adjusted(penWidth / 2, penWidth / 2,
                                           -penWidth / 2, -penWidth / 2));
      if (hasFocus()) {
        painter.fillRect(cursorRect, _cursorColor.isValid() ? _cursorColor
                                                            : foregroundColor);

        if (!_cursorColor.isValid()) {
          // invert the colour used to draw the text to ensure that the
          // character at the cursor position is readable
          invertCharacterColor = true;
        }
      }
    } else if (_cursorShape == CursorShape::UNDERLINE_CURSOR)
      painter.drawLine(cursorRect.left(), cursorRect.bottom(),
                       cursorRect.right(), cursorRect.bottom());
    else if (_cursorShape == CursorShape::IBEAM_CURSOR)
      painter.drawLine(cursorRect.left(), cursorRect.top(), cursorRect.left(),
                       cursorRect.bottom());
  }
}

void TerminalView::drawCharacters(QPainter &painter, const QRect &rect,
                                  const std::wstring &text,
                                  const Character *style,
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
    font.setUnderline(useUnderline);
    font.setItalic(useItalic);
    font.setStrikeOut(useStrikeOut);
    font.setOverline(useOverline);
    painter.setFont(font);
  }

  // setup pen
  const CharacterColor &textColor =
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
      painter.drawText(rect.x(), rect.y() + _fontAscend + _lineSpacing,
                       QString::fromStdWString(text));
    } else {
      {
        QRect drawRect(rect.topLeft(), rect.size());
        drawRect.setHeight(rect.height() + _drawTextAdditionHeight);
        painter.drawText(drawRect, Qt::AlignBottom,
                         LTR_OVERRIDE_CHAR + QString::fromStdWString(text));
      }
    }
  }
}

void TerminalView::drawLineCharString(QPainter &painter, int x, int y,
                                      const std::wstring &str,
                                      const Character *attributes) const {
  const QPen &currentPen = painter.pen();

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

void TerminalView::drawInputMethodPreeditString(QPainter &painter,
                                                const QRect &rect) {}

void TerminalView::paintFilters(QPainter &painter) {
  // todo: fulfil the filter
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

bool TerminalView::isLineCharString(const std::wstring &string) const {
  return (string.length() > 0) && (isLineChar(string[0]));
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

void TerminalView::scrollImage(int lines, const QRect &screenWindowRegion) {
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
  void *firstCharPos = &_image[region.top() * this->_columns];
  void *lastCharPos = &_image[(region.top() + abs(lines)) * this->_columns];

  int top = _topMargin + (region.top() * _fontHeight);
  int linesToMove = region.height() - abs(lines);
  int bytesToMove = linesToMove * this->_columns * sizeof(Character);

  Q_ASSERT(linesToMove > 0);
  Q_ASSERT(bytesToMove > 0);

  // scroll internal image
  if (lines > 0) {
    // check that the memory areas that we are going to move are valid
    Q_ASSERT((char *)lastCharPos + bytesToMove <
             (char *)(_image + (this->_lines * this->_columns)));

    Q_ASSERT((lines * this->_columns) < _imageSize);

    // scroll internal image down
    memmove(firstCharPos, lastCharPos, bytesToMove);

    // set region of display to scroll
    scrollRect.setTop(top);
  } else {
    // check that the memory areas that we are going to move are valid
    Q_ASSERT((char *)firstCharPos + bytesToMove <
             (char *)(_image + (this->_lines * this->_columns)));

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

QPoint TerminalView::cursorPosition() const {
  if (_screenWindow)
    return _screenWindow->cursorPosition();
  else
    return {0, 0};
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
  Character *oldimg = _image;
  int oldlin = _lines;
  int oldcol = _columns;

  makeImage();

  // copy the old image to reduce flicker
  int mLines = qMin(oldlin, _lines);
  int mColumns = qMin(oldcol, _columns);

  if (oldimg) {
    for (int line = 0; line < mLines; line++) {
      memcpy((void *)&_image[_columns * line], (void *)&oldimg[oldcol * line],
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

void TerminalView::scrollToEnd() {}

void TerminalView::setBlinkingCursor(bool blink) {}

void TerminalView::setBlinkingTextEnabled(bool blink) {}

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

void TerminalView::setBellMode(int mode) { _bellMode = mode; }

void TerminalView::setCursorShape(CursorShape shape) { _cursorShape = shape; }

CursorShape TerminalView::getCursorShape() const { return _cursorShape; }

void TerminalView::setScreenWindow(ScreenWindow *window) {
  // disconnect existing screen window if any
  if (_screenWindow) {
    disconnect(_screenWindow, nullptr, this, nullptr);
  }

  _screenWindow = window;

  if (window) {
    // TODO: Determine if this is an issue.
    //#warning "The order here is not specified - does it matter whether
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

ScreenWindow *TerminalView::getScreenWindow() const { return _screenWindow; }

/* ------------------------------------------------------------------------- */
/*                                                                           */
/*                               Events handle                               */
/*                                                                           */
/* ------------------------------------------------------------------------- */
void TerminalView::paintEvent(QPaintEvent *event) {
  QPainter paint(this);
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

  if (_testFlag) {
    //    calDrawTextAdditionHeight(paint);
  }

  const QRegion regToDraw = event->region() & cr;
  for (auto rect = regToDraw.begin(); rect != regToDraw.end(); rect++) {
    drawBackground(paint, *rect, palette().window().color(),
                   true /* use opacity setting */);
    drawContents(paint, *rect);
  }

  drawInputMethodPreeditString(paint, preeditRect());
  paintFilters(paint);
}

void TerminalView::showEvent(QShowEvent *) {
  emit changedContentSizeSignal(_contentHeight, _contentWidth);
}

void TerminalView::hideEvent(QHideEvent *) {
  emit changedContentSizeSignal(_contentHeight, _contentWidth);
}

void TerminalView::resizeEvent(QResizeEvent *) {
  updateImageSize();
  //  processFilters();
}

void TerminalView::fontChange(const QFont &font) {}

void TerminalView::focusInEvent(QFocusEvent *event) {}

void TerminalView::focusOutEvent(QFocusEvent *event) {}

void TerminalView::keyPressEvent(QKeyEvent *event) {}

void TerminalView::mouseDoubleClickEvent(QMouseEvent *ev) {}

void TerminalView::mousePressEvent(QMouseEvent *) {}

void TerminalView::mouseReleaseEvent(QMouseEvent *) {}

void TerminalView::mouseMoveEvent(QMouseEvent *) {}

void TerminalView::extendSelection(const QPoint &pos) {}

void TerminalView::wheelEvent(QWheelEvent *) {}

bool TerminalView::focusNextPrevChild(bool next) { return false; }

/* ------------------------------------------------------------------------- */
/*                                                                           */
/*                               Grag And Drop                               */
/*                                                                           */
/* ------------------------------------------------------------------------- */
void TerminalView::dragEnterEvent(QDragEnterEvent *event) {}

void TerminalView::dropEvent(QDropEvent *event) {}

void TerminalView::doDrag() {}

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

  Character *const newimg = _screenWindow->getImage();
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

  wchar_t *disstrU = new wchar_t[columnsToUpdate];
  char *dirtyMask = new char[columnsToUpdate + 2];
  QRegion dirtyRegion;

  // debugging variable, this records the number of lines that are found to
  // be 'dirty' ( ie. have changed from the old _image to the new _image ) and
  // which therefore need to be repainted
  int dirtyLineCount = 0;

  for (y = 0; y < linesToUpdate; ++y) {
    const Character *currentLine = &_image[y * this->_columns];
    const Character *const newLine = &newimg[y * columns];

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
            const Character &ch = newLine[x + len];

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
    memcpy((void *)currentLine, (const void *)newLine,
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

void TerminalView::setUsesMouse(bool on) {
  if (_mouseMarks != on) {
    _mouseMarks = on;
    setCursor(_mouseMarks ? Qt::IBeamCursor : Qt::ArrowCursor);
    emit useMouseChanged();
  }
}

void TerminalView::setBracketedPasteMode(bool on) { _bracketedPasteMode = on; }

void TerminalView::setBackgroundColor(const QColor &color) {
  _colorTable[DEFAULT_BACK_COLOR].color = color;
  QPalette p = palette();
  p.setColor(backgroundRole(), color);
  setPalette(p);

  // Avoid propagating the palette change to the scroll bar
  _scrollBar->setPalette(QApplication::palette());

  update();
}

void TerminalView::setForegroundColor(const QColor &color) {
  _colorTable[DEFAULT_FORE_COLOR].color = color;

  update();
}

TerminalView::TerminalView(QWidget *parent)
    : QWidget(parent),
      _gridLayout(nullptr),
      _allowBell(true),
      _boldIntense(true),
      _fixedFont(true),
      _fontHeight(1),
      _fontWidth(1),
      _fontAscend(1),
      _drawTextAdditionHeight(0),
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
      _tripleClickMode(TripleClickMode::SELECT_WHOLE_LINE),
      _isFixedSize(false),
      _possibleTripleClick(false),
      _resizeWidget(nullptr),
      _resizeTimer(nullptr),
      _outputSuspendedLabel(nullptr),
      _lineSpacing(0),
      _colorsInverted(false),
      _opacity(static_cast<qreal>(1)),
      _backgroundMode(BackgroundMode::NONE),
      _cursorShape(CursorShape::BLOCK_CURSOR),
      _motionAfterPasting(MotionAfterPasting::NO_MOVE_SCREEN_WINDOW),
      _leftBaseMargin(1),
      _topBaseMargin(1),
      _drawLineChars(true) {
  _drawTextAdditionHeight = 0;
  _testFlag = false;

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
  connect(_scrollBar, SIGNAL(valueChanged(int)), this,
          SLOT(scrollBarPositionChanged(int)));
  _scrollBar->hide();

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
  //  delete filterChain;
}

const ColorEntry *TerminalView::getColorTable() const { return _colorTable; }

void TerminalView::setColorTable(const ColorEntry table[]) {
  for (int i = 0; i < TABLE_COLORS; i++) _colorTable[i] = table[i];

  setBackgroundColor(_colorTable[DEFAULT_BACK_COLOR].color);
}

void TerminalView::setRandomSeed(uint seed) { _randomSeed = seed; }

uint TerminalView::getRandomSeed() const { return _randomSeed; }

void TerminalView::setOpacity(qreal opacity) {
  this->_opacity =
      qBound(static_cast<qreal>(0), opacity, static_cast<qreal>(1));
}

void TerminalView::setBackgroundImage(const QString &backgroundImage) {
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
AutoScrollHandler::AutoScrollHandler(QWidget *parent)
    : QObject(parent), _timerId(0) {
  parent->installEventFilter(this);
}

void AutoScrollHandler::timerEvent(QTimerEvent *event) {
  if (event->timerId() != _timerId) return;

  QMouseEvent mouseEvent(QEvent::MouseMove,
                         widget()->mapFromGlobal(QCursor::pos()), Qt::NoButton,
                         Qt::LeftButton, Qt::NoModifier);

  QApplication::sendEvent(widget(), &mouseEvent);
}

bool AutoScrollHandler::eventFilter(QObject *watched, QEvent *event) {
  Q_ASSERT(watched == parent());
  Q_UNUSED(watched);

  QMouseEvent *mouseEvent = (QMouseEvent *)event;
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
ushort ExtendedCharTable::extendedCharHash(ushort *unicodePoints,
                                           ushort length) const {
  ushort hash = 0;
  for (ushort i = 0; i < length; i++) {
    hash = 31 * hash + unicodePoints[i];
  }
  return hash;
}
bool ExtendedCharTable::extendedCharMatch(ushort hash, ushort *unicodePoints,
                                          ushort length) const {
  ushort *entry = extendedCharTable[hash];

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
ushort ExtendedCharTable::createExtendedChar(ushort *unicodePoints,
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
  ushort *buffer = new ushort[length + 1];
  buffer[0] = length;
  for (int i = 0; i < length; i++) buffer[i + 1] = unicodePoints[i];

  extendedCharTable.insert(hash, buffer);

  return hash;
}

ushort *ExtendedCharTable::lookupExtendedChar(ushort hash,
                                              ushort &length) const {
  // lookup index in table and if found, set the length
  // argument and return a pointer to the character sequence

  ushort *buffer = extendedCharTable[hash];
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
  QHashIterator<ushort, ushort *> iter(extendedCharTable);
  while (iter.hasNext()) {
    iter.next();
    delete[] iter.value();
  }
}

// global instance
ExtendedCharTable ExtendedCharTable::instance;
