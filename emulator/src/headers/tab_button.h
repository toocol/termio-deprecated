#ifndef TABBUTTON_H
#define TABBUTTON_H

#include <QLabel>
#include <QWidget>

#include "tab.h"

class TabButton : public QWidget {
  Q_OBJECT
 public:
  explicit TabButton(QString code, int size, QWidget *parent = nullptr);

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

 signals:
};

#endif  // TABBUTTON_H
