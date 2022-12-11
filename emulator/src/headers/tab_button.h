#ifndef TABBUTTON_H
#define TABBUTTON_H

#include <QLabel>
#include <QWidget>

#include "tab.h"

class TabButton : public QWidget {
  Q_OBJECT
 public:
  explicit TabButton(QString code, QString name, int size,
                     QWidget *parent = nullptr);

 protected:
  void mousePressEvent(QMouseEvent *) override;
  void mouseReleaseEvent(QMouseEvent *) override;

 private:
  /**
   * @brief sizeHint
   * @return the recommended size for the widget
   */
  QSize sizeHint() const override {
    return QSize(tabMaxHeight * 1.5, tabMaxHeight);
  }
  /**
   * @brief minimumSizeHint
   * @return the recommended minimum size for the widget
   */
  QSize minimumSizeHint() const override {
    return QSize(tabMaxHeight * 1.5, tabMaxHeight);
  }
  QLabel *_label;
  QString _name;

 signals:
  void mousePressed(QString, int);
  void mouseRelease(QString, int);
};

#endif  // TABBUTTON_H
