#ifndef TAB_H
#define TAB_H

#include <QLabel>
#include <QWidget>

const int tabMaxWidth = 200;
const int tabMaxHeight = 20;

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

 protected:
  void paintEvent(QPaintEvent *) override;

 private:
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
 signals:
};

#endif  // TAB_H
