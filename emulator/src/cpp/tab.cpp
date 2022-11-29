#include "tab.h"

Tab::Tab(QWidget* parent) : QWidget{parent} {
  resize(tabMaxWidth, tabMaxHeight);
  setMaximumWidth(tabMaxWidth);
  setMaximumHeight(tabMaxHeight);
  setMinimumHeight(tabMaxHeight);

  _title = new QLabel(this);
  QPalette pe;
  pe.setColor(QPalette::WindowText, Qt::white);
  _title->setPalette(pe);
  _title->setAlignment(Qt::AlignmentFlag::AlignCenter);
}

QString Tab::nameTitle() const { return _nameTitle; }

void Tab::setNameTitle(QString nameTitle) {
  _nameTitle = nameTitle;
  _title->setText(nameTitle);
  _title->update();
  update();
}

QString Tab::userTitle() const { return _userTitle; }

void Tab::setUserTitle(QString userTitle) {
  _userTitle = userTitle;
  _title->setText(userTitle);
  _title->update();
  update();
}

QString Tab::displayTitle() const { return _displayTitle; }

void Tab::setDisplayTitle(QString displayTitle) {
  _displayTitle = displayTitle;
  _title->setText(displayTitle);
  _title->update();
  update();
}

QString Tab::iconName() const { return _iconName; }

void Tab::setIconName(QString iconName) { _iconName = iconName; }

QString Tab::iconText() const { return _iconText; }

void Tab::setIconText(QString iconText) { _iconText = iconText; }

void Tab::paintEvent(QPaintEvent* evt) {}
