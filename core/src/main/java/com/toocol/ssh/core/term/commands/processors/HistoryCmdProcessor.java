package com.toocol.ssh.core.term.commands.processors;
import com.toocol.ssh.core.term.core.HistoryOutputInfoHelper;
import com.toocol.ssh.core.term.core.Term;
import com.toocol.ssh.utilities.anis.AnisStringBuilder;
import com.toocol.ssh.utilities.utils.Tuple2;
import io.vertx.core.eventbus.EventBus;

import java.util.List;

public class HistoryCmdProcessor extends  TestCmdProcessor{
    private final HistoryOutputInfoHelper historyOutputInfoHelper = HistoryOutputInfoHelper.getInstance();

    public void process(EventBus eventBus, String cmd, Tuple2<Boolean, String> resultAndMsg) {
        List<List<String>> msgList = historyOutputInfoHelper.getMsgList();
        Term term = new Term();
        AnisStringBuilder stringBuilder = new AnisStringBuilder();
        for (List<String> strings : msgList) {
            for (String string : strings) {
                stringBuilder.append(string).append("\n");
            }
            stringBuilder.append("\n");
        }

        term.printDisplay(stringBuilder.deFront().toString(),true);
    }
}