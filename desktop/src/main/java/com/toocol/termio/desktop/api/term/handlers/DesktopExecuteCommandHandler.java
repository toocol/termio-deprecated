package com.toocol.termio.desktop.api.term.handlers;

import com.toocol.termio.core.term.TermAddress;
import com.toocol.termio.core.term.commands.TermCommand;
import com.toocol.termio.core.term.core.Term;
import com.toocol.termio.core.term.core.TermPrinter;
import com.toocol.termio.utilities.module.IAddress;
import com.toocol.termio.utilities.ansi.AnsiStringBuilder;
import com.toocol.termio.utilities.ansi.Printer;
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
public final class DesktopExecuteCommandHandler extends NonBlockingMessageHandler {

    public DesktopExecuteCommandHandler(Vertx vertx, Context context) {
        super(vertx, context);
    }

    @Override
    public IAddress consume() {
        return TermAddress.EXECUTE_OUTSIDE_DESKTOP;
    }

    @Override
    public <T> void handleInline(Message<T> message) {
        String cmd = String.valueOf(message.body());

        Tuple2<Boolean, String> resultAndMessage = new Tuple2<>();
        AtomicBoolean isBreak = new AtomicBoolean();
        boolean isCommand = TermCommand.cmdOf(cmd)
                .map(termCommand -> {
                    try {
                        termCommand.processCmd(eventBus, cmd, resultAndMessage);
                        if ((TermCommand.CMD_NUMBER.equals(termCommand) || TermCommand.CMD_MOSH.equals(termCommand))
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
            Printer.println(msg);
        } else {
            TermPrinter.displayBuffer = StrUtil.EMPTY;
        }

        if (!isCommand && StringUtils.isNotEmpty(cmd)) {
            AnsiStringBuilder builder = new AnsiStringBuilder()
                    .background(Term.theme.displayBackGroundColor.color)
                    .front(Term.theme.commandHighlightColor.color)
                    .append(cmd)
                    .deFront()
                    .append(": command not found.");
            Printer.println(builder.toString());
        }

        message.reply(isBreak.get());
    }
}
