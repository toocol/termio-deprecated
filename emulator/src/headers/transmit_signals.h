#ifndef TRANSMIT_SIGNALS_H
#define TRANSMIT_SIGNALS_H

#include <QObject>

class TransmitSignals : public QObject {
  Q_OBJECT
 public:
  explicit TransmitSignals(QObject *parent = nullptr);

 signals:
  void sigTabRightClick();

  void sigTabButtonMousePressed(QString, int);
  void sigTabButtonMouseRelease(QString, int);
};

#endif  // TRANSMITSIGNALS_H
