#include "tab.h"

#include <QMouseEvent>
#include <QPainter>
#include <QSizePolicy>

Tab::Tab(QWidget* parent) : QWidget{parent} {
  resize(tabMaxWidth, tabMaxHeight);
  setMaximumWidth(tabMaxWidth);
  setMaximumHeight(tabMaxHeight);
  setMinimumHeight(tabMaxHeight);
  setAutoFillBackground(true);
  setWindowFlags(Qt::Window);
  QSizePolicy policy(QSizePolicy::Maximum, QSizePolicy::Fixed);
  setSizePolicy(policy);
}

QString Tab::nameTitle() const { return _nameTitle; }

void Tab::setNameTitle(QString nameTitle) {
  _nameTitle = nameTitle;
  update();
}

QString Tab::userTitle() const { return _userTitle; }

void Tab::setUserTitle(QString userTitle) {
  _userTitle = userTitle;
  update();
}

QString Tab::displayTitle() const { return _displayTitle; }

void Tab::setDisplayTitle(QString displayTitle) {
  _displayTitle = displayTitle;
  update();
}

QString Tab::iconName() const { return _iconName; }

void Tab::setIconName(QString iconName) { _iconName = iconName; }

QString Tab::iconText() const { return _iconText; }

void Tab::setIconText(QString iconText) { _iconText = iconText; }

void Tab::onBackgroundChange(const QColor& color) {
  QPalette pe = palette();
  pe.setColor(backgroundRole(), color);
  setPalette(pe);
  update();
}

void Tab::paintEvent(QPaintEvent* evt) {
  QPainter paint(this);

  paint.setFont(QFont("Courier New", 10, QFont::Bold));
  paint.setPen(QColor(38, 38, 38));

  QSize size = this->size();
  paint.drawText(QPoint(5, (size.height() - 10) / 2 + 10), _userTitle);

  // Bottom border line
  if (_activate) {
    paint.setPen(QColor(30, 30, 30));
    paint.drawLine(0, height() - 1, width(), height() - 1);
  } else {
    paint.setPen(QColor(0xCD, 0xCD, 0xCD));
    paint.drawLine(0, height() - 1, width(), height() - 1);
  }

  // Right border line
  paint.setPen(QColor(158, 158, 158));
  paint.drawLine(width(), 5, width(), height() - 5);

  paint.end();
}

void Tab::mouseReleaseEvent(QMouseEvent* evt) {
  if (evt->button() == Qt::LeftButton) {
    emit tabActivate();
  } else if (evt->button() == Qt::RightButton) {
    emit tabRightClick();
  }
}
