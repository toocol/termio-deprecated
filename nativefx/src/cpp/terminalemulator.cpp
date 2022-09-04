#include "terminalemulator.h"

TerminalEmulator::TerminalEmulator(QWidget *parent) : QWidget(parent) {
  resize(1280, 800);
  textDisplay = new QTextEdit(this);
  textDisplay->setReadOnly(true);
  textDisplay->resize(1280, 800);
  textDisplay->setTextColor(QColor(0xba, 0xb1, 0x2d));
  textDisplay->setText(
      "kkkkkkkkkk\n"
      "kkkkkkkkkk\n"
      "kkkkkkkkkk\n");
  textDisplay->setTextColor(QColor(0xbb, 0x11, 0xaa));
  QTextDocument *document = textDisplay->document();
  QTextBlock block = document->findBlockByLineNumber(2);
  QTextCursor cursor = textDisplay->textCursor();
  cursor.setPosition(block.position());
  cursor.insertText("HelloWorld~");
}

TerminalEmulator::~TerminalEmulator() { delete textDisplay; }
