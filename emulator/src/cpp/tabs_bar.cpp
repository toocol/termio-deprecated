#include "tabs_bar.h"

TabsBar::TabsBar(QWidget *parent) : QWidget{parent} {
  _tabsLayout = new QHBoxLayout();
  _tabsLayout->setContentsMargins(QMargins(0, 0, 0, 0));
  setLayout(_tabsLayout);
}
