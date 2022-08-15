package com.toocol.termio.core.term.core;

import com.toocol.termio.utilities.ansi.AnsiStringBuilder;
import com.toocol.termio.utilities.log.Loggable;
import org.apache.commons.lang3.StringUtils;
import java.text.SimpleDateFormat;
import java.util.*;

public final class HistoryOutputInfoHelper implements Loggable {
    private static final int PAGE_SIZE = 5;
    private static final HistoryOutputInfoHelper instance = new HistoryOutputInfoHelper();
    private int showIndex = 1;
    private int totalPage = 1;
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
        AnsiStringBuilder builder = new AnsiStringBuilder();
        msgs.forEach(builder::append);
        builder.append("\n")
                .append("index:")
                .append(showIndex)
                .append(StringUtils.repeat(" ",40))
                .append("Press '←'/'→' to change page,'Esc' to quit.");
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
