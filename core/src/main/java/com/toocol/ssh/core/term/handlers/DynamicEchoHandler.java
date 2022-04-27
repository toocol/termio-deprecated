package com.toocol.ssh.core.term.handlers;

import com.toocol.ssh.core.auth.core.SshCredential;
import com.toocol.ssh.core.cache.CredentialCache;
import com.toocol.ssh.core.cache.SshSessionCache;
import com.toocol.ssh.core.term.commands.TermioCommand;
import com.toocol.ssh.core.term.core.Term;
import com.toocol.ssh.utilities.address.IAddress;
import com.toocol.ssh.utilities.anis.AnisStringBuilder;
import com.toocol.ssh.utilities.anis.ColorHelper;
import com.toocol.ssh.utilities.handler.AbstractMessageHandler;
import com.toocol.ssh.utilities.utils.CharUtil;
import com.toocol.ssh.utilities.utils.StrUtil;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

import static com.toocol.ssh.core.term.TermAddress.TERMINAL_ECHO;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/23 2:59
 * @version: 0.0.1
 */
public final class DynamicEchoHandler extends AbstractMessageHandler {

    private static final Map<String, TermioCommand> COMMANDS = TermioCommand.COMMANDS;

    volatile public static String lastInput = StrUtil.EMPTY;

    private final SshSessionCache sshSessionCache = SshSessionCache.getInstance();
    private final Term term = Term.getInstance();

    public DynamicEchoHandler(Vertx vertx, Context context) {
        super(vertx, context);
    }

    @Override
    public <T> void handle(Message<T> message) {
        String cmd = cast(message.body());
        int backgroundColor = Term.theme.displayBackGroundColor;
        int commandHighlightColor = Term.theme.commandHighlightColor;
        String finalCmd = cmd.trim();

        TermioCommand command = COMMANDS.get(finalCmd);
        if (command == null) {
            if (StringUtils.isEmpty(finalCmd)) {
                lastInput = StrUtil.EMPTY;
                term.printDisplayEcho(StrUtil.EMPTY);
                return;
            }
            if (StringUtils.isNumeric(finalCmd)) {
                AnisStringBuilder connectionPrompt = new AnisStringBuilder().background(backgroundColor);
                connectionPrompt.append("Connection [").append(finalCmd.length() == 1 ? "0" + finalCmd : finalCmd).append("]");
                connectionPrompt.append("\n\n");

                int index;
                SshCredential credential = null;
                try {
                    index = Integer.parseInt(finalCmd);
                    credential = CredentialCache.getCredential(index);
                } catch (Exception e) {
                    // exceed Integer range
                }

                if (credential == null) {
                    connectionPrompt.append("the index corresponded connection not found.");
                } else {
                    String status = sshSessionCache.isAlive(credential.getHost()) ? ColorHelper.front("alive", Term.theme.sessionAliveColor) : "offline";
                    connectionPrompt
                            .append("Host:").append(" ".repeat(15 - 5)).append(ColorHelper.front(credential.getHost(), Term.theme.hostHighlightColor)).append("\n")
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
                AnisStringBuilder builder = new AnisStringBuilder().background(backgroundColor)
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
        return TERMINAL_ECHO;
    }

    private void spaceProcess(Term term, String cmd) {
        int commandHighlightColor = Term.theme.commandHighlightColor;
        int backgroundColor = Term.theme.displayBackGroundColor;

        if (cmd.contains(StrUtil.SPACE)) {
            String[] split = cmd.split(StrUtil.SPACE);
            TermioCommand splitCommand = COMMANDS.get(split[0]);
            if (splitCommand == null) {
                AnisStringBuilder printMsg = new AnisStringBuilder().background(backgroundColor)
                        .append("Didn't find command '")
                        .front(commandHighlightColor).append(split[0]).deFront().append("'");
                String alikeCommand = TermioCommand.findAlike(split[0]);
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
            AnisStringBuilder titleMsg = new AnisStringBuilder().background(backgroundColor).append("Alternative commands: ");
            AnisStringBuilder printMsg = new AnisStringBuilder().background(backgroundColor);
            for (TermioCommand value : TermioCommand.values()) {
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
                AnisStringBuilder builder = new AnisStringBuilder().background(backgroundColor)
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
