#include "tab_button.h"

#include <QPainter>

TabButton::TabButton(QString code, int size, QWidget *parent)
    : QWidget{parent} {
  QFont font;
  font.setFamily("Segoe Fluent Icons");
  font.setPixelSize(size);
  font.setBold(true);

  _label = new QLabel(code, this);
  _label->setFont(font);

  _label->setStyleSheet("color:#9e9e9e");
  _label->move(8, 3);
}
