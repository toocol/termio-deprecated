#ifndef TITLE_TAB_H
#define TITLE_TAB_H

#include <QHBoxLayout>
#include <QWidget>

class TabsBar : public QWidget {
  Q_OBJECT
 public:
  explicit TabsBar(QWidget *parent = nullptr);

 private:
  QHBoxLayout *_tabsLayout;

 signals:
};

#endif  // TITLE_TAB_H
