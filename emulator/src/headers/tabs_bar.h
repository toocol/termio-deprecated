#ifndef TITLE_TAB_H
#define TITLE_TAB_H

#include <QHBoxLayout>
#include <QList>
#include <QWidget>

#include "tab.h"

class TabsBar : public QWidget {
  Q_OBJECT
 public:
  explicit TabsBar(QWidget *parent = nullptr);
  void addTab(Tab *);

 private:
  QHBoxLayout *_tabsLayout;
  QList<Tab *> *_tabs;

 signals:
};

#endif  // TITLE_TAB_H
