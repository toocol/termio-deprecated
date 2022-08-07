package com.toocol.termio.core.term.core;



import com.toocol.termio.utilities.log.Loggable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class HistoryOutputInfoHelper implements Loggable {
    private static HistoryOutputInfoHelper instance ;
    private final List<String> msgList = new ArrayList<>();

    public void add(String message) {
        String prefix = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ").format(new Date(System.currentTimeMillis())) + "\n";
        String s = new StringBuilder(message).insert(0, prefix).toString();
        String[] split = s.split("\n");
        for (String msg : split) {
            msgList.add(msg);
        }
    }

    public List<String> getMsgList() {
        return msgList;
    }

    public synchronized static HistoryOutputInfoHelper getInstance() {
        if (instance == null) {
            instance = new HistoryOutputInfoHelper();
        }
        return instance;
    }
}
