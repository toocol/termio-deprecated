package com.toocol.termio.core.term.commands.processors;

import com.toocol.termio.core.term.commands.TermCommandProcessor;
import com.toocol.termio.core.term.core.HistoryOutputInfoHelper;
import com.toocol.termio.core.term.core.Term;
import com.toocol.termio.core.term.core.TermStatus;
import com.toocol.termio.utilities.utils.Tuple2;
import io.vertx.core.eventbus.EventBus;

public class HistoryCmdProcessor extends TermCommandProcessor {

      private final HistoryOutputInfoHelper historyOutputInfoHelper = HistoryOutputInfoHelper.getInstance();

      public void process(EventBus eventBus, String cmd, Tuple2<Boolean, String> resultAndMsg) {
          Term.status = TermStatus.HISTORY_OUTPUT;
          historyOutputInfoHelper.displayInformation();
      }

}
