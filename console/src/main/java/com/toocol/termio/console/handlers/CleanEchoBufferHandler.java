package com.toocol.termio.console.handlers;

import com.toocol.termio.core.term.TermAddress;
import com.toocol.termio.core.term.commands.TermCommand;
import com.toocol.termio.core.term.core.Term;
import com.toocol.termio.core.term.core.TermPrinter;
import com.toocol.termio.utilities.ansi.AnsiStringBuilder;
import com.toocol.termio.utilities.ansi.Printer;
import com.toocol.termio.utilities.module.IAddress;
import com.toocol.termio.utilities.module.NonBlockingMessageHandler;
import com.toocol.termio.utilities.utils.StrUtil;
import com.toocol.termio.utilities.utils.Tuple2;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.atomic.AtomicBoolean;


/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 11:09
 */
public final class CleanEchoBufferHandler extends NonBlockingMessageHandler {

    private final Term term = Term.getInstance();

    public CleanEchoBufferHandler(Vertx vertx, Context context) {
        super(vertx, context);
    }

    @Override
    public IAddress consume() {
        return TermAddress.TERMINAL_ECHO_CLEAN_BUFFER;
    }

    @Override
    public <T> void handleInline(Message<T> message) {
        DynamicEchoHandler.lastInput = StrUtil.EMPTY;
    }
}
