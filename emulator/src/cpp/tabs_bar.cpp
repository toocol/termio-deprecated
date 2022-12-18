#include "tabs_bar.h"

#include <QPainter>
#include <QPushButton>

TabsBar::TabsBar(QWidget *parent) : QWidget{parent} {
  _mainLayout = new QHBoxLayout();
  _mainLayout->setContentsMargins(QMargins(0, 0, 0, 0));
  _mainLayout->setDirection(QBoxLayout::LeftToRight);
  _mainLayout->setAlignment(Qt::AlignLeft);
  _mainLayout->setSpacing(0);

  _startIconLayout = new QHBoxLayout();
  _startIconLayout->setContentsMargins(QMargins(0, 0, 0, 0));
  _startIconLayout->setDirection(QBoxLayout::LeftToRight);
  _startIconLayout->setAlignment(Qt::AlignLeft);
  _startIconLayout->setSpacing(0);

  _tabsLayout = new QHBoxLayout();
  _tabsLayout->setContentsMargins(QMargins(0, 0, 0, 0));
  _tabsLayout->setDirection(QBoxLayout::LeftToRight);
  _tabsLayout->setAlignment(Qt::AlignLeft);
  _tabsLayout->setSpacing(0);

  _endIconLayout = new QHBoxLayout();
  _endIconLayout->setContentsMargins(QMargins(0, 0, 0, 0));
  _endIconLayout->setDirection(QBoxLayout::LeftToRight);
  _endIconLayout->setAlignment(Qt::AlignLeft);
  _endIconLayout->setSpacing(0);

  _mainLayout->addLayout(_startIconLayout);
  _mainLayout->addLayout(_tabsLayout);
  _mainLayout->addLayout(_endIconLayout);

  setLayout(_mainLayout);
  setMaximumHeight(tabMaxHeight);
  setMinimumHeight(tabMaxHeight);
  setAutoFillBackground(true);

  _tabs = QList<Tab *>();
  _buttons = QList<TabButton *>();

  initEndIcon();
}

void TabsBar::addTab(Tab *tab) {
  _tabs.push_back(tab);
  _tabsLayout->addWidget(tab);
  tab->show();
}

QList<TabButton *> *TabsBar::buttons() { return &_buttons; }

void TabsBar::initEndIcon() {
  TabButton *addNewTabButton = new TabButton("\ue710", "tab-button-new", 16);
  _endIconLayout->addWidget(addNewTabButton);
  _buttons.push_back(addNewTabButton);
}

void TabsBar::onBackgroundChange(const QColor &color) {
  QPalette pe = palette();
  pe.setColor(backgroundRole(), color);
  setPalette(pe);
  update();
}

void TabsBar::paintEvent(QPaintEvent *) {
  QPainter paint(this);
  paint.setPen(QColor(0xCD, 0xCD, 0xCD));
  paint.drawLine(0, height() - 1, width(), height() - 1);
  paint.end();
}
