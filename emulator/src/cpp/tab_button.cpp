#include "tab_button.h"

#include <QMouseEvent>
#include <QPainter>

TabButton::TabButton(QString code, QString name, int size, QWidget *parent)
    : QWidget{parent}, _name(name) {
  QFont font;
  font.setFamily("Segoe Fluent Icons");
  font.setPixelSize(size);
  font.setBold(true);

  _label = new QLabel(code, this);
  _label->setFont(font);

  _label->setStyleSheet("color:#9e9e9e");
  _label->move(8, 3);
}

void TabButton::mousePressEvent(QMouseEvent *evt) {
  if (_name == nullptr) return;
  emit mousePressed(_name, evt->button());
}

void TabButton::mouseReleaseEvent(QMouseEvent *evt) {
  if (_name == nullptr) return;
  emit mouseRelease(_name, evt->button());
}
