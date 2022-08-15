package com.toocol.termio.core.term.core;

import com.toocol.termio.utilities.anis.AnisStringBuilder;
import com.toocol.termio.utilities.log.Loggable;
import java.text.SimpleDateFormat;
import java.util.*;

public final class HistoryOutputInfoHelper implements Loggable {
    private static final int PAGE_SIZE = 2;
    private int showIndex = 1;
    private int totalPage = 1;
    private static HistoryOutputInfoHelper instance = new HistoryOutputInfoHelper();
    private final Map<Integer, List<String>> msgList = new HashMap<>();
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ");

    public void add(String message) {
        List<String> pageMsgs = msgList.getOrDefault(totalPage, new ArrayList<>());
        message = simpleDateFormat.format(new Date()) + "\n" + message + "\n";
        if (pageMsgs.size() < PAGE_SIZE) {
            pageMsgs.add(message);
        } else {
            pageMsgs = msgList.getOrDefault(++totalPage, new ArrayList<>());
            pageMsgs.add(message);
        }
        showIndex = totalPage;
        msgList.put(totalPage, pageMsgs);
    }

    public void displayInformation() {
        List<String> msgs = msgList.get(showIndex);
        AnisStringBuilder builder = new AnisStringBuilder();
        msgs.forEach(msg -> builder.append(msg));
        Term term = Term.getInstance();
        term.termPrinter.cleanDisplay();
        term.printDisplayWithRecord(builder.toString());
    }

    public void pageLeft() {
        if (showIndex == 1) {
            return;
        }
        showIndex--;
        displayInformation();
    }

    public void pageRight() {
        if (showIndex >= totalPage) {
            return;
        }
        showIndex++;
        displayInformation();
    }

    public synchronized static HistoryOutputInfoHelper getInstance() {
        return instance;
    }
}
