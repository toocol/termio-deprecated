#include "terminalview.h"

#include <QApplication>
#include <QPainter>
#include <QStyle>

#include "wcwidth.h"

using namespace TConsole;

#ifndef loc
#define loc(X, Y) ((Y)*columns + (X))
#endif

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
  // this slot is used to change status in ScreenWindow, it's not sure whether
  // need the Screen Window or not yet for now.
}

void TerminalView::blinkEvent() {}

void TerminalView::blinkCursorEvent() {}

void TerminalView::enableBell() {}

void TerminalView::clearImage() {
  // We initialize _image[_imageSize] too. See makeImage()
  for (int i = 0; i <= imageSize; i++) {
    image[i].character = ' ';
    image[i].foregroundColor =
        CharacterColor(COLOR_SPACE_DEFAULT, DEFAULT_FORE_COLOR);
    image[i].backgroundColor =
        CharacterColor(COLOR_SPACE_DEFAULT, DEFAULT_BACK_COLOR);
    image[i].rendition = DEFAULT_RENDITION;
  }
}

int TerminalView::textWidth(const int startColumn, const int length,
                            const int line) const {
  QFontMetrics fontMetrics(font());
  int result = 0;
  for (int column = 0; column < length; column++) {
    result += fontMetrics.horizontalAdvance(
        image[loc(startColumn + column, line)].character);
  }
  return result;
}

QRect TerminalView::calculateTextArea(int topLeftX, int topLeftY,
                                      int startColumn, int line, int length) {
  int left =
      fixedFont ? fontWidth * startColumn : textWidth(0, startColumn, line);
  int top = fontHeight * line;
  int width =
      fixedFont ? fontWidth * length : textWidth(startColumn, length, line);
  return {leftMargin + topLeftX + left, topMargin + topLeftY + top, width,
          fontHeight};
}

void TerminalView::drawContents(QPainter &painter, const QRect &rect) {
  QPoint tL = contentsRect().topLeft();
  int tLx = tL.x();
  int tLy = tL.y();

  int lux = qMin(usedColumns - 1,
                 qMax(0, (rect.left() - tLx - leftMargin) / fontWidth));
  int luy =
      qMin(usedLines - 1, qMax(0, (rect.top() - tLy - topMargin) / fontHeight));
  int rlx = qMin(usedColumns - 1,
                 qMax(0, (rect.right() - tLx - leftMargin) / fontWidth));
  int rly = qMin(usedLines - 1,
                 qMax(0, (rect.bottom() - tLy - topMargin) / fontHeight));

  const int bufferSize = usedColumns;
  std::wstring unistr;
  unistr.reserve(bufferSize);
  for (int y = luy; y <= rly; y++) {
    quint32 c = image[loc(lux, y)].character;
    int x = lux;
    if (!c && x) x--;  // Search for start of multi-column character
    for (; x <= rlx; x++) {
      int len = 1;
      int p = 0;

      // reset our buffer to the maximal size
      unistr.resize(bufferSize);

      // is this a single character or a sequence of characters ?
      if (image[loc(x, y)].rendition & RE_EXTENDED_CHAR) {
        // sequence of characters
        ushort extendedCharLength = 0;
        ushort *chars = ExtendedCharTable::instance.lookupExtendedChar(
            image[loc(x, y)].charSequence, extendedCharLength);
        for (int index = 0; index < extendedCharLength; index++) {
          Q_ASSERT(p < bufferSize);
          unistr[p++] = chars[index];
        }
      } else {
        // single character
        c = image[loc(x, y)].character;
        if (c) {
          Q_ASSERT(p < bufferSize);
          unistr[p++] = c;  // fontMap(c);
        }
      }

      bool lineDraw = isLineChar(c);
      bool doubleWidth = (image[qMin(loc(x, y) + 1, imageSize)].character == 0);
      CharacterColor currentForeground = image[loc(x, y)].foregroundColor;
      CharacterColor currentBackground = image[loc(x, y)].backgroundColor;
      quint8 currentRendition = image[loc(x, y)].rendition;

      while (x + len <= rlx &&
             image[loc(x + len, y)].foregroundColor == currentForeground &&
             image[loc(x + len, y)].backgroundColor == currentBackground &&
             image[loc(x + len, y)].rendition == currentRendition &&
             (image[qMin(loc(x + len, y) + 1, imageSize)].character == 0) ==
                 doubleWidth &&
             isLineChar(c = image[loc(x + len, y)].character) ==
                 lineDraw)  // Assignment!
      {
        if (c) unistr[p++] = c;  // fontMap(c);
        if (doubleWidth)  // assert((_image[loc(x+len,y)+1].character == 0)),
                          // see above if condition
          len++;          // Skip trailing part of multi-column character
        len++;
      }
      if ((x + len < usedColumns) && (!image[loc(x + len, y)].character))
        len++;  // Adjust for trailing part of multi-column character

      bool save__fixedFont = fixedFont;
      if (lineDraw) fixedFont = false;
      unistr.resize(p);

      // Create a text scaling matrix for double width and double height lines.
      QTransform textScale;

      if (y < lineProperties.size()) {
        if (lineProperties[y] & LINE_DOUBLEWIDTH) textScale.scale(2, 1);

        if (lineProperties[y] & LINE_DOUBLEHEIGHT) textScale.scale(1, 2);
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
      drawTextFragment(painter, textArea, unistr, &image[loc(x, y)]);  //,
                                                                       // 0,
      //!_isPrinting );

      fixedFont = save__fixedFont;

      // reset back to single-width, single-height _lines
      painter.setWorldTransform(textScale.inverted(), true);

      if (y < lineProperties.size() - 1) {
        // double-height _lines are represented by two adjacent _lines
        // containing the same characters
        // both _lines will have the LINE_DOUBLEHEIGHT attribute.
        // If the current line has the LINE_DOUBLEHEIGHT attribute,
        // we can therefore skip the next line
        if (lineProperties[y] & LINE_DOUBLEHEIGHT) y++;
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
  const QColor foregroundColor = style->foregroundColor.color(colorTable);
  const QColor backgroundColor = style->backgroundColor.color(colorTable);

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
    if (backgroundImage.isNull()) {
      QColor color(backgroundColor);
      color.setAlphaF(opacity);

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
  cursorRect.setHeight(fontHeight - lineSpacing - 1);

  if (!cursorBlinking) {
    if (cursorColor.isValid())
      painter.setPen(cursorColor);
    else
      painter.setPen(foregroundColor);

    if (cursorShape == CursorShape::BLOCK_CURSOR) {
      // draw the cursor outline, adjusting the area so that
      // it is draw entirely inside 'rect'
      float penWidth = qMax(1, painter.pen().width());

      painter.drawRect(cursorRect.adjusted(penWidth / 2, penWidth / 2,
                                           -penWidth / 2, -penWidth / 2));
      if (hasFocus()) {
        painter.fillRect(cursorRect,
                         cursorColor.isValid() ? cursorColor : foregroundColor);

        if (!cursorColor.isValid()) {
          // invert the colour used to draw the text to ensure that the
          // character at the cursor position is readable
          invertCharacterColor = true;
        }
      }
    } else if (cursorShape == CursorShape::UNDERLINE_CURSOR)
      painter.drawLine(cursorRect.left(), cursorRect.bottom(),
                       cursorRect.right(), cursorRect.bottom());
    else if (cursorShape == CursorShape::IBEAM_CURSOR)
      painter.drawLine(cursorRect.left(), cursorRect.top(), cursorRect.left(),
                       cursorRect.bottom());
  }
}

void TerminalView::drawCharacters(QPainter &painter, const QRect &rect,
                                  const std::wstring &text,
                                  const Character *style,
                                  bool invertCharacterColor) {
  // don't draw text which is currently blinking
  if (blinking && (style->rendition & RE_BLINK)) return;

  // don't draw concealed characters
  if (style->rendition & RE_CONCEAL) return;

  // setup bold and underline
  bool useBold = ((style->rendition & RE_BOLD) && boldIntense) || font().bold();
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
  const QColor color = textColor.color(colorTable);
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

    if (bidiEnabled) {
      painter.drawText(rect.x(), rect.y() + fontAscend + lineSpacing,
                       QString::fromStdWString(text));
    } else {
      {
        QRect drawRect(rect.topLeft(), rect.size());
        drawRect.setHeight(rect.height() + drawTextAdditionHeight);
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

  if ((attributes->rendition & RE_BOLD) && boldIntense) {
    QPen boldPen(currentPen);
    boldPen.setWidth(3);
    painter.setPen(boldPen);
  }

  for (size_t i = 0; i < str.length(); i++) {
    uint8_t code = static_cast<uint8_t>(str[i] & 0xffU);
    if (LineChars[code])
      drawLineChar(painter, x + (fontWidth * i), y, fontWidth, fontHeight,
                   code);
    else
      drawOtherChar(painter, x + (fontWidth * i), y, fontWidth, fontHeight,
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
  const int preeditLength = string_width(inputMethodData.preeditString);

  if (preeditLength == 0) return {};

  return QRect(leftMargin + fontWidth * cursorPosition().x(),
               topMargin + fontHeight * cursorPosition().y(),
               fontWidth * preeditLength, fontHeight);
}

bool TerminalView::isLineChar(wchar_t c) const {
  return drawLineChars && ((c & 0xFF80) == 0x2500);
}

bool TerminalView::isLineCharString(const std::wstring &string) const {
  return (string.length() > 0) && (isLineChar(string[0]));
}

void TerminalView::showResizeNotification() {
  if (terminalSizeHint && isVisible()) {
    if (terminalSizeStartup) {
      terminalSizeStartup = false;
      return;
    }
    if (!resizeWidget) {
      const QString label = tr("Size: XXX x XXX");
      resizeWidget = new QLabel(label, this);
      resizeWidget->setMinimumWidth(
          resizeWidget->fontMetrics().horizontalAdvance(label));
      resizeWidget->setMinimumHeight(resizeWidget->sizeHint().height());
      resizeWidget->setAlignment(Qt::AlignCenter);

      resizeWidget->setStyleSheet(
          QLatin1String("background-color:palette(window);border-style:solid;"
                        "border-width:1px;border-color:palette(dark)"));

      resizeTimer = new QTimer(this);
      resizeTimer->setSingleShot(true);
      connect(resizeTimer, SIGNAL(timeout()), resizeWidget, SLOT(hide()));
    }
    resizeWidget->setText(tr("Size: %1 x %2").arg(columns).arg(lines));
    resizeWidget->move((width() - resizeWidget->width()) / 2,
                       (height() - resizeWidget->height()) / 2 + 20);
    resizeWidget->show();
    resizeTimer->start(1000);
  }
}

QPoint TerminalView::cursorPosition() const {
  //  if (screenWindow)
  //    return screenWindow->cursorPosition();
  //  else
  return {0, 0};
}

void TerminalView::calcGeometry() {}

void TerminalView::propagateSize() {
  if (isFixedSize) {
    setSize(columns, lines);
    QWidget::setFixedSize(sizeHint());
    parentWidget()->adjustSize();
    parentWidget()->setFixedSize(parentWidget()->sizeHint());
    return;
  }
  if (image) updateImageSize();
}

void TerminalView::updateImageSize() {
  Character *oldimg = image;
  int oldlin = lines;
  int oldcol = columns;

  makeImage();

  // copy the old image to reduce flicker
  int mLines = qMin(oldlin, lines);
  int mColumns = qMin(oldcol, columns);

  if (oldimg) {
    for (int line = 0; line < mLines; line++) {
      memcpy((void *)&image[columns * line], (void *)&oldimg[oldcol * line],
             mColumns * sizeof(Character));
    }
    delete[] oldimg;
  }

  //  if (screenWindow)
  //      screenWindow->setWindowLines(_lines);

  resizing = (oldlin != lines) || (oldcol != columns);

  if (resizing) {
    showResizeNotification();
    emit changedContentSizeSignal(contentHeight,
                                  contentWidth);  // expose resizeEvent
  }

  resizing = false;
}

void TerminalView::makeImage() {
  calcGeometry();

  // confirm that array will be of non-zero size, since the painting code
  // assumes a non-zero array length
  Q_ASSERT(lines > 0 && columns > 0);
  Q_ASSERT(usedLines <= lines && usedColumns <= columns);

  imageSize = lines * columns;

  // We over-commit one character so that we can be more relaxed in dealing with
  // certain boundary conditions: _image[_imageSize] is a valid but unused
  // position
  image = new Character[imageSize + 1];

  clearImage();
}

void TerminalView::setScroll(int cursor, int lines) {
  if (scrollBar->minimum() == 0 &&
      scrollBar->maximum() == (lines - this->lines) &&
      scrollBar->value() == cursor)
    return;

  disconnect(scrollBar, SIGNAL(valueChanged(int)), this,
             SLOT(scrollBarPositionChanged(int)));
  scrollBar->setRange(0, lines - this->lines);
  scrollBar->setSingleStep(1);
  scrollBar->setPageStep(lines);
  scrollBar->setValue(cursor);
  connect(scrollBar, SIGNAL(valueChanged(int)), this,
          SLOT(scrollBarPositionChanged(int)));
}

void TerminalView::scrollToEnd() {}

void TerminalView::setBlinkingCursor(bool blink) {}

void TerminalView::setBlinkingTextEnabled(bool blink) {}

void TerminalView::setSize(int cols, int lins) {
  int scrollBarWidth = (scrollBar->isHidden() ||
                        scrollBar->style()->styleHint(
                            QStyle::SH_ScrollBar_Transient, nullptr, scrollBar))
                           ? 0
                           : scrollBar->sizeHint().width();
  int horizontalMargin = 2 * leftBaseMargin;
  int verticalMargin = 2 * topBaseMargin;

  QSize newSize =
      QSize(horizontalMargin + scrollBarWidth + (columns * fontWidth),
            verticalMargin + (lines * fontHeight));

  if (newSize != QWidget::size()) {
    size = newSize;
    updateGeometry();
  }
}

void TerminalView::setBellMode(int mode) { bellMode = mode; }

/* ------------------------------------------------------------------------- */
/*                                                                           */
/*                               Events handle                               */
/*                                                                           */
/* ------------------------------------------------------------------------- */
void TerminalView::paintEvent(QPaintEvent *event) {
  qDebug() << "Trigger";
  QPainter paint(this);
  QRect cr = contentsRect();

  if (!backgroundImage.isNull()) {
    QColor background = colorTable[DEFAULT_BACK_COLOR].color;
    if (opacity < static_cast<qreal>(1)) {
      background.setAlphaF(opacity);
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

    if (backgroundMode == STRETCH) {  // scale the image without keeping its
                                      // proportions to fill the screen
      paint.drawPixmap(cr, backgroundImage, backgroundImage.rect());
    } else if (backgroundMode == ZOOM) {  // zoom in/out the image to fit it
      QRect r = backgroundImage.rect();
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
      paint.drawPixmap(r, backgroundImage, backgroundImage.rect());
    } else if (backgroundMode == FIT) {  // if the image is bigger than the
                                         // terminal, zoom it out to fit it
      QRect r = backgroundImage.rect();
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
      paint.drawPixmap(r, backgroundImage, backgroundImage.rect());
    } else if (backgroundMode ==
               CENTER) {  // center the image without scaling/zooming
      QRect r = backgroundImage.rect();
      r.moveCenter(cr.center());
      paint.drawPixmap(r.topLeft(), backgroundImage);
    } else  // if (_backgroundMode == None)
    {
      paint.drawPixmap(0, 0, backgroundImage);
    }

    paint.restore();
  }

  qDebug() << "Step 1";
  if (testFlag) {
    //    calDrawTextAdditionHeight(paint);
  }

  const QRegion regToDraw = event->region() & cr;
  qDebug() << "Step " << event->region().begin() << ":"
           << event->region().end();
  qDebug() << "Step " << cr.width() << ":" << cr.height();
  qDebug() << "Step " << regToDraw.end();
  for (auto rect = regToDraw.begin(); rect != regToDraw.end(); rect++) {
    drawBackground(paint, *rect, palette().window().color(),
                   true /* use opacity setting */);
    qDebug() << "Loop1";
    drawContents(paint, *rect);
    qDebug() << "Loop2";
  }
  qDebug() << "Step 2";

  drawInputMethodPreeditString(paint, preeditRect());
  qDebug() << "Step 3";

  paintFilters(paint);
  qDebug() << "Finish";
}

void TerminalView::showEvent(QShowEvent *) {}

void TerminalView::hideEvent(QHideEvent *) {}

void TerminalView::resizeEvent(QResizeEvent *) {}

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

void TerminalView::setUsesMouse(bool on) {
  if (mouseMarks != on) {
    mouseMarks = on;
    setCursor(mouseMarks ? Qt::IBeamCursor : Qt::ArrowCursor);
    emit useMouseChanged();
  }
}

void TerminalView::setBracketedPasteMode(bool on) { bracketedPasteMode = on; }

void TerminalView::setBackgroundColor(const QColor &color) {
  colorTable[DEFAULT_BACK_COLOR].color = color;
  QPalette p = palette();
  p.setColor(backgroundRole(), color);
  setPalette(p);

  // Avoid propagating the palette change to the scroll bar
  scrollBar->setPalette(QApplication::palette());

  update();
}

void TerminalView::setForegroundColor(const QColor &color) {
  colorTable[DEFAULT_FORE_COLOR].color = color;

  update();
}

TerminalView::TerminalView(QWidget *parent)
    : QWidget(parent),
      gridLayout(nullptr),
      allowBell(true),
      boldIntense(true),
      fixedFont(true),
      fontHeight(1),
      fontWidth(1),
      fontAscend(1),
      drawTextAdditionHeight(0),
      lines(1),
      columns(1),
      usedLines(1),
      usedColumns(1),
      contentHeight(1),
      contentWidth(1),
      image(nullptr),
      randomSeed(0),
      resizing(false),
      terminalSizeHint(false),
      terminalSizeStartup(true),
      bidiEnabled(false),
      mouseMarks(false),
      disabledBracketedPasteMode(false),
      actSel(0),
      wordSelectionMode(false),
      lineSelectionMode(false),
      preserveLineBreaks(false),
      columnSelectionMode(false),
      scrollbarLocation(ScrollBarPosition::NO_SCROLL_BAR),
      wordCharacters(QLatin1String(":@-./_~")),
      bellMode(BellMode::SYSTEM_BEEP_BELL),
      blinking(false),
      hasBlinker(false),
      cursorBlinking(false),
      hasBlinkingCursor(false),
      allowBlinkingText(true),
      tripleClickMode(TripleClickMode::SELECT_WHOLE_LINE),
      isFixedSize(false),
      possibleTripleClick(false),
      resizeWidget(nullptr),
      resizeTimer(nullptr),
      outputSuspendedLabel(nullptr),
      lineSpacing(0),
      colorsInverted(false),
      opacity(static_cast<qreal>(1)),
      backgroundMode(BackgroundMode::NONE),
      cursorShape(CursorShape::BLOCK_CURSOR),
      motionAfterPasting(MotionAfterPasting::NO_MOVE_SCREEN_WINDOW),
      leftBaseMargin(1),
      topBaseMargin(1),
      drawLineChars(true) {
  drawTextAdditionHeight = 0;
  testFlag = false;

  setLayoutDirection(Qt::LeftToRight);

  // offsets are not calculate yet.
  topMargin = topBaseMargin;
  leftMargin = leftBaseMargin;

  // Scroll Bar:
  scrollBar = new QScrollBar(this);
  if (!scrollBar->style()->styleHint(QStyle::SH_ScrollBar_Transient, nullptr,
                                     scrollBar))
    scrollBar->setAutoFillBackground(true);
  setScroll(0, 0);
  scrollBar->setCursor(Qt::ArrowCursor);
  connect(scrollBar, SIGNAL(valueChanged(int)), this,
          SLOT(scrollBarPositionChanged(int)));
  scrollBar->hide();

  // set timers for blinking cursor and text
  blinkTimer = new QTimer(this);
  connect(blinkTimer, SIGNAL(timeout()), this, SLOT(blinkEvent()));
  blinkCursorTimer = new QTimer(this);
  connect(blinkCursorTimer, SIGNAL(timeout()), this, SLOT(blinkCursorEvent()));

  setUsesMouse(true);
  setBracketedPasteMode(false);
  // setColorTable(base_color_table);
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

  gridLayout = new QGridLayout(this);
  gridLayout->setContentsMargins(0, 0, 0, 0);

  setLayout(gridLayout);

  new AutoScrollHandler(this);
}

TerminalView::~TerminalView() {
  disconnect(blinkTimer);
  disconnect(blinkCursorTimer);
  qApp->removeEventFilter(this);

  //  delete[] image;

  delete gridLayout;
  delete outputSuspendedLabel;
  //  delete filterChain;
}

const ColorEntry *TerminalView::getColorTable() const { return colorTable; }

void TerminalView::setColorTable(const ColorEntry table[]) {
  for (int i = 0; i < TABLE_COLORS; i++) colorTable[i] = table[i];

  setBackgroundColor(colorTable[DEFAULT_BACK_COLOR].color);
}

void TerminalView::setRandomSeed(uint seed) { randomSeed = seed; }

uint TerminalView::getRandomSeed() const { return randomSeed; }

void TerminalView::setOpacity(qreal opacity) {
  this->opacity = qBound(static_cast<qreal>(0), opacity, static_cast<qreal>(1));
}

void TerminalView::setBackgroundImage(const QString &backgroundImage) {
  if (!backgroundImage.isEmpty()) {
    this->backgroundImage.load(backgroundImage);
    setAttribute(Qt::WA_OpaquePaintEvent, false);
  } else {
    this->backgroundImage = QPixmap();
    setAttribute(Qt::WA_OpaquePaintEvent, true);
  }
}

void TerminalView::setBackgroundMode(BackgroundMode mode) {
  backgroundMode = mode;
}

void TerminalView::setScrollBarPosition(ScrollBarPosition position) {
  if (scrollbarLocation == position) return;

  if (position == NO_SCROLL_BAR)
    scrollBar->hide();
  else
    scrollBar->show();

  topMargin = leftMargin = 1;
  scrollbarLocation = position;

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
