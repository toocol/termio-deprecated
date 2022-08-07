package com.toocol.termio.core.term.commands.processors;


import com.toocol.termio.core.term.commands.TermCommandProcessor;
import com.toocol.termio.core.term.core.HistoryOutputInfoHelper;
import com.toocol.termio.core.term.core.Term;
import com.toocol.termio.core.term.core.TermStatus;
import com.toocol.termio.utilities.anis.AnisStringBuilder;
import com.toocol.termio.utilities.utils.Tuple2;
import io.vertx.core.eventbus.EventBus;
import java.util.List;

public class HistoryCmdProcessor extends TermCommandProcessor {
      private  final HistoryOutputInfoHelper historyOutputInfoHelper = HistoryOutputInfoHelper.getInstance();
      private  final List<String> msgList = historyOutputInfoHelper.getMsgList();

      public void process(EventBus eventBus, String cmd, Tuple2<Boolean, String> resultAndMsg) {
          Term term = Term.getInstance();
          term.status = TermStatus.HISTORY_OUTPUT;
          AnisStringBuilder stringBuilder = new AnisStringBuilder();
          for (String s : msgList) {
              stringBuilder.append(s).append("\n");
          }
          term.printDisplay(stringBuilder.deFront().toString(), true);
      }
}
