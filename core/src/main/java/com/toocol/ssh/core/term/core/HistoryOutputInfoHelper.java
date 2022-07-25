package com.toocol.ssh.core.term.core;

import com.toocol.ssh.utilities.log.Loggable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public final class HistoryOutputInfoHelper implements Loggable {
    private static HistoryOutputInfoHelper instance;
    private final List<List<String>> MsgList = new ArrayList<>();

    public void add(String message) {
        String prefix = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ").format(new Date(System.currentTimeMillis())) + "\n";
        String s = new StringBuilder(message).insert(0, prefix).toString();
        MsgList.add(Arrays.stream(s.split("\n")).toList());
    }

    public List<List<String>> getMsgList() {
        return MsgList;
    }

    public synchronized static HistoryOutputInfoHelper getInstance() {
        if (instance == null) {
            instance = new HistoryOutputInfoHelper();
        }
        return instance;
    }
}
