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
  void initEndIcon();

  QHBoxLayout *_mainLayout;
  QHBoxLayout *_startIconLayout;
  QHBoxLayout *_tabsLayout;
  QHBoxLayout *_endIconLayout;
  QList<Tab *> *_tabs;

 protected:
  void paintEvent(QPaintEvent *) override;

 signals:

 protected slots:
  void onBackgroundChange(const QColor &);
};

#endif  // TITLE_TAB_H
