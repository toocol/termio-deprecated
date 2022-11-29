#include "tabs_bar.h"

TabsBar::TabsBar(QWidget *parent) : QWidget{parent} {
  _tabsLayout = new QHBoxLayout();
  _tabsLayout->setContentsMargins(QMargins(0, 0, 0, 0));
  //  _tabsLayout->setAlignment(Qt::AlignmentFlag::AlignLeft);
  _tabs = new QList<Tab *>();

  setLayout(_tabsLayout);
  setMaximumHeight(tabMaxHeight);
  setMinimumHeight(tabMaxHeight);
}

void TabsBar::addTab(Tab *tab) {
  _tabsLayout->addWidget(tab);
  _tabs->push_back(tab);
}
