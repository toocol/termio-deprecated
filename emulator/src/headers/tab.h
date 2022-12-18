#ifndef TAB_H
#define TAB_H

#include <QLabel>
#include <QWidget>

const int tabMinWidth = 100;
const int tabMaxWidth = 200;
const int tabMaxHeight = 23;

class Tab : public QWidget {
  Q_OBJECT
 public:
  explicit Tab(QWidget *parent = nullptr);

  QString nameTitle() const;
  void setNameTitle(QString);
  QString userTitle() const;
  void setUserTitle(QString);
  QString displayTitle() const;
  void setDisplayTitle(QString);

  QString iconName() const;
  void setIconName(QString);
  QString iconText() const;
  void setIconText(QString);

  void setActivate(const bool is) { _activate = is; }

 protected:
  void paintEvent(QPaintEvent *) override;
  void mouseReleaseEvent(QMouseEvent *) override;

 private:
  /**
   * @brief sizeHint
   * @return the recommended size for the widget
   */
  QSize sizeHint() const override { return QSize(tabMaxWidth, tabMaxHeight); }
  /**
   * @brief minimumSizeHint
   * @return the recommended minimum size for the widget
   */
  QSize minimumSizeHint() const override {
    return QSize(tabMinWidth, tabMaxHeight);
  }

  int _index;
  /**
   * The session's id correspond to this Tab.
   *
   * @brief _sessionId
   */
  long _sessionId;
  QLabel *_title;

  QString _nameTitle;
  QString _displayTitle;
  QString _userTitle;

  QString _iconName;
  QString _iconText;  // as set by: echo -en '\033]1;IconText\007

  bool _activate;

 signals:
  void tabActivate();
  void tabRightClick();

 protected slots:
  void onBackgroundChange(const QColor &);
};

#endif  // TAB_H
