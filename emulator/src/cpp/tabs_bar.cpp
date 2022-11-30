#include "tabs_bar.h"

#include <QPushButton>

TabsBar::TabsBar(QWidget *parent) : QWidget{parent} {
  _tabsLayout = new QHBoxLayout();
  _tabsLayout->setContentsMargins(QMargins(0, 0, 0, 0));
  _tabsLayout->setDirection(QBoxLayout::LeftToRight);
  //  _tabsLayout->setAlignment(Qt::AlignLeft | Qt::AlignTop);

  setLayout(_tabsLayout);
  setMaximumHeight(tabMaxHeight);
  setMinimumHeight(tabMaxHeight);
  setAutoFillBackground(true);

  _tabs = new QList<Tab *>();
}

void TabsBar::addTab(Tab *tab) {
  _tabsLayout->addWidget(tab);
  _tabs->push_back(tab);
}

void TabsBar::onBackgroundChange(const QColor &color) {
  QPalette pe = palette();
  pe.setColor(backgroundRole(), color);
  setPalette(pe);
  update();
}
