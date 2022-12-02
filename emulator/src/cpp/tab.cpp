#include "tab.h"

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

  paint.setFont(QFont("Times", 10, QFont::Bold));
  paint.setPen(QColor(255, 255, 255));

  QSize size = this->size();
  paint.drawText(QPoint(5, (size.height() - 10) / 2 + 10), _userTitle);
  paint.end();
}

void Tab::mouseReleaseEvent(QMouseEvent* evt) { emit tabActivate(); }
