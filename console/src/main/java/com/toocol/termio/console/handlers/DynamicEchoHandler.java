package com.toocol.termio.console.handlers;

import com.toocol.termio.core.auth.core.SshCredential;
import com.toocol.termio.core.cache.CredentialCache;
import com.toocol.termio.core.cache.SshSessionCache;
import com.toocol.termio.core.term.TermAddress;
import com.toocol.termio.core.term.commands.TermCommand;
import com.toocol.termio.core.term.core.Term;
import com.toocol.termio.utilities.module.IAddress;
import com.toocol.termio.utilities.ansi.AnsiStringBuilder;
import com.toocol.termio.utilities.ansi.ColorHelper;
import com.toocol.termio.utilities.module.NonBlockingMessageHandler;
import com.toocol.termio.utilities.utils.CharUtil;
import com.toocol.termio.utilities.utils.StrUtil;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/23 2:59
 * @version: 0.0.1
 */
public final class DynamicEchoHandler extends NonBlockingMessageHandler {

    private static final Map<String, TermCommand> COMMANDS = TermCommand.COMMANDS;

    volatile public static String lastInput = StrUtil.EMPTY;

    private final CredentialCache.Instance credentialCache = CredentialCache.Instance;
    private final SshSessionCache.Instance sshSessionCache = SshSessionCache.Instance;
    private final Term term = Term.getInstance();

    public DynamicEchoHandler(Vertx vertx, Context context) {
        super(vertx, context);
    }

    @Override
    public <T> void handleInline(Message<T> message) {
        String cmd = cast(message.body());
        int backgroundColor = Term.theme.displayBackGroundColor.color;
        int commandHighlightColor = Term.theme.commandHighlightColor.color;
        String finalCmd = cmd.trim();

        TermCommand command = COMMANDS.get(finalCmd);
        if (command == null) {
            if (StringUtils.isEmpty(finalCmd)) {
                lastInput = StrUtil.EMPTY;
                term.printDisplayEcho(StrUtil.EMPTY);
                return;
            }
            if (StringUtils.isNumeric(finalCmd)) {
                AnsiStringBuilder connectionPrompt = new AnsiStringBuilder().background(backgroundColor);
                connectionPrompt.append("Connection [").append(finalCmd.length() == 1 ? "0" + finalCmd : finalCmd).append("]");
                connectionPrompt.append("\n\n");

                int index;
                SshCredential credential = null;
                try {
                    index = Integer.parseInt(finalCmd);
                    credential = credentialCache.getCredential(index);
                } catch (Exception e) {
                    // exceed Integer range
                }

                if (credential == null) {
                    connectionPrompt.append("the index corresponded connection not found.");
                } else {
                    String status = sshSessionCache.isAlive(credential.getHost()) ? ColorHelper.front("alive", Term.theme.sessionAliveColor.color) : "offline";
                    connectionPrompt
                            .append("Host:").append(" ".repeat(15 - 5)).front(Term.theme.hostHighlightColor.color).append(credential.getHost()).deFront().append("\n")
                            .append("User:").append(" ".repeat(15 - 5)).append(credential.getUser()).append("\n")
                            .append("Port:").append(" ".repeat(15 - 5)).append(credential.getPort()).append("\n")
                            .append("Type:").append(" ".repeat(15 - 5)).append("SSH").append("\n")
                            .append("Status:").append(" ".repeat(15 - 7)).append(status).append("\n")
                    ;
                }

                if (!lastInput.equals(connectionPrompt.toString())) {
                    term.printDisplayEcho(connectionPrompt.toString());
                }
                lastInput = connectionPrompt.toString();
                return;
            }

            spaceProcess(term, finalCmd);
        } else {
            if (StringUtils.isNotEmpty(command.getSpecify())) {
                if (!lastInput.equals(command.getSpecify())) {
                    term.printDisplayEcho(command.getSpecify());
                }
                lastInput = command.getSpecify();
            } else {
                AnsiStringBuilder builder = new AnsiStringBuilder().background(backgroundColor)
                        .append("Didn't find command '")
                        .front(commandHighlightColor).append(cmd).deFront()
                        .append("'\n\n")
                        .append("Press ")
                        .front(commandHighlightColor)
                        .append("Ctrl+U").deFront()
                        .append(" to clear input. ");
                if (!lastInput.equals(builder.toString())) {
                    term.printDisplayEcho(builder.toString());
                }
                lastInput = builder.toString();
            }
        }
    }

    @Override
    public IAddress consume() {
        return TermAddress.TERMINAL_ECHO;
    }

    private void spaceProcess(Term term, String cmd) {
        int commandHighlightColor = Term.theme.commandHighlightColor.color;
        int backgroundColor = Term.theme.displayBackGroundColor.color;

        if (cmd.contains(StrUtil.SPACE)) {
            String[] split = cmd.split(StrUtil.SPACE);
            TermCommand splitCommand = COMMANDS.get(split[0]);
            if (splitCommand == null) {
                AnsiStringBuilder printMsg = new AnsiStringBuilder().background(backgroundColor)
                        .append("Didn't find command '")
                        .front(commandHighlightColor).append(split[0]).deFront().append("'");
                String alikeCommand = TermCommand.findAlike(split[0]);
                if (alikeCommand != null) {
                    printMsg.append(", do you mean: ").front(commandHighlightColor).append(alikeCommand).deFront();
                } else {
                    printMsg.append("\n\n")
                            .append("Press ")
                            .front(commandHighlightColor)
                            .append("Ctrl+U").deFront()
                            .append(" to clear input. ");
                }

                if (!lastInput.equals(printMsg.toString())) {
                    term.printDisplayEcho(printMsg.toString());
                }
                lastInput = printMsg.toString();
            } else {
                if (StringUtils.isNotEmpty(splitCommand.getSpecify())) {
                    if (!lastInput.equals(splitCommand.getSpecify())) {
                        term.printDisplayEcho(splitCommand.getSpecify());
                    }
                    lastInput = splitCommand.getSpecify();
                }
            }
        } else {
            AnsiStringBuilder titleMsg = new AnsiStringBuilder().background(backgroundColor).append("Alternative commands: ");
            AnsiStringBuilder printMsg = new AnsiStringBuilder().background(backgroundColor);
            for (TermCommand value : TermCommand.values()) {
                if (value.cmd().startsWith(cmd)) {
                    if (StringUtils.isNotEmpty(value.getSpecify())) {
                        printMsg.front(commandHighlightColor).append(value.cmd()).deFront().append(CharUtil.TAB);
                    }
                }
            }
            if (printMsg.length() != 0) {
                titleMsg.append(printMsg);
                if (!lastInput.equals(titleMsg.toString())) {
                    term.printDisplayEcho(titleMsg.toString());
                }
                lastInput = titleMsg.toString();
            } else {
                AnsiStringBuilder builder = new AnsiStringBuilder().background(backgroundColor)
                        .append("Didn't find command '")
                        .front(commandHighlightColor).append(cmd).deFront()
                        .append("'\n\n")
                        .append("Press ")
                        .front(commandHighlightColor)
                        .append("Ctrl+U").deFront()
                        .append(" to clear input. ");
                if (!lastInput.equals(builder.toString())) {
                    term.printDisplayEcho(builder.toString());
                }
                lastInput = builder.toString();
            }
        }
    }
}
