package com.toocol.termio.core.term.handlers;

import com.toocol.termio.core.term.commands.TermioCommand;
import com.toocol.termio.core.term.core.Term;
import com.toocol.termio.core.term.core.TermPrinter;
import com.toocol.termio.utilities.address.IAddress;
import com.toocol.termio.utilities.anis.AnisStringBuilder;
import com.toocol.termio.utilities.anis.Printer;
import com.toocol.termio.utilities.handler.NonBlockingMessageHandler;
import com.toocol.termio.utilities.utils.StrUtil;
import com.toocol.termio.utilities.utils.Tuple2;
import com.toocol.termio.core.term.TermAddress;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.atomic.AtomicBoolean;


/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 11:09
 */
public final class ExecuteCommandHandler extends NonBlockingMessageHandler {

    private final Term term = Term.getInstance();

    public ExecuteCommandHandler(Vertx vertx, Context context) {
        super(vertx, context);
    }

    @Override
    public IAddress consume() {
        return TermAddress.EXECUTE_OUTSIDE;
    }

    @Override
    public <T> void handleInline(Message<T> message) {
        String cmd = String.valueOf(message.body());

        Tuple2<Boolean, String> resultAndMessage = new Tuple2<>();
        AtomicBoolean isBreak = new AtomicBoolean();
        boolean isCommand = TermioCommand.cmdOf(cmd)
                .map(termioCommand -> {
                    try {
                        termioCommand.processCmd(eventBus, cmd, resultAndMessage);
                        if ((TermioCommand.CMD_NUMBER.equals(termioCommand) || TermioCommand.CMD_MOSH.equals(termioCommand))
                                && StringUtils.isEmpty(resultAndMessage._2())) {
                            isBreak.set(true);
                        }
                    } catch (Exception e) {
                        Printer.printErr("Execute command failed, message = " + e.getMessage());
                    }
                    return true;
                }).orElse(false);

        String msg = resultAndMessage._2();
        if (StringUtils.isNotEmpty(msg)) {
            term.printDisplay(msg);
        } else {
            TermPrinter.DISPLAY_BUFF = StrUtil.EMPTY;
        }

        if (!isCommand && StringUtils.isNotEmpty(cmd)) {
            AnisStringBuilder builder = new AnisStringBuilder().background(Term.theme.displayBackGroundColor)
                    .front(Term.theme.commandHighlightColor)
                    .append(cmd)
                    .deFront()
                    .append(": command not found.");
            term.printDisplay(builder.toString());
        }

        message.reply(isBreak.get());
    }
}
