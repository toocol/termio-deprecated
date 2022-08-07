package com.toocol.termio.core.term.core;


import java.util.List;

public class HistoryOutputInfoQuickSwitchHelper {
    private static HistoryOutputInfoQuickSwitchHelper instance;
    private  final Term term = Term.getInstance();
    private  final HistoryOutputInfoHelper historyOutputInfoHelper = HistoryOutputInfoHelper.getInstance();
    private  final List<String> msgList = historyOutputInfoHelper.getMsgList();

    public void rightInformation(int index) {
        if (index > msgList.size() - 1) {
            term.termPrinter.cleanDisplay();
            term.termPrinter.printDisplay(msgList.get(msgList.size() - 1));
        }
        term.termPrinter.cleanDisplay();
        term.termPrinter.printDisplay(msgList.get(index), true);
    }

    public void leftInformation(int index) {
        if (index == 0) {
            term.termPrinter.printDisplay(msgList.get(0));
        } else {
            term.termPrinter.cleanDisplay();
            term.termPrinter.printDisplay(msgList.get(index), true);
        }

    }

    public synchronized static HistoryOutputInfoQuickSwitchHelper getInstance() {
        if (instance == null) {
            instance = new HistoryOutputInfoQuickSwitchHelper();
        }
        return instance;
    }

}
