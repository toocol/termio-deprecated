#ifndef TAB_H
#define TAB_H

#include <QWidget>

class Tab : public QWidget {
  Q_OBJECT
 public:
  explicit Tab(QWidget *parent = nullptr);

 protected:
  void paintEvent(QPaintEvent *) override;

 signals:
};

#endif  // TAB_H
