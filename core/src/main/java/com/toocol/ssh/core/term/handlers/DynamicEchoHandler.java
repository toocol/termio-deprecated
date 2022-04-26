package com.toocol.ssh.core.term.handlers;

import com.toocol.ssh.core.auth.core.SshCredential;
import com.toocol.ssh.core.cache.CredentialCache;
import com.toocol.ssh.core.cache.SshSessionCache;
import com.toocol.ssh.core.term.commands.TermioCommand;
import com.toocol.ssh.core.term.core.Term;
import com.toocol.ssh.utilities.address.IAddress;
import com.toocol.ssh.utilities.anis.AnisStringBuilder;
import com.toocol.ssh.utilities.anis.HighlightHelper;
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

    private final SshSessionCache sshSessionCache = SshSessionCache.getInstance();
    private final Term term = Term.getInstance();

    public DynamicEchoHandler(Vertx vertx, Context context) {
        super(vertx, context);
    }

    @Override
    public <T> void handle(Message<T> message) {
        String cmd = cast(message.body());
        int backgroundColor = Term.theme.backgroundColor;
        String finalCmd = cmd.trim();

        TermioCommand command = COMMANDS.get(finalCmd);
        if (command == null) {
            if (StringUtils.isEmpty(finalCmd)) {
                term.printDisplayEcho(StrUtil.EMPTY);
                return;
            }
            if (StringUtils.isNumeric(finalCmd)) {
                AnisStringBuilder connectionPrompt = new AnisStringBuilder().background(backgroundColor);
                connectionPrompt.append("Connection [").append(finalCmd).append("]");
                connectionPrompt.append("\n\n");

                SshCredential credential = CredentialCache.getCredential(Integer.parseInt(finalCmd));
                if (credential == null) {
                    connectionPrompt.append("the index corresponded connection not found.");
                } else {
                    String status = sshSessionCache.isAlive(credential.getHost()) ? HighlightHelper.assembleColor("alive", Term.theme.sessionAliveColor) : "offline";
                    connectionPrompt.append("Host:\t\t").append(HighlightHelper.assembleColor(credential.getHost(), Term.theme.hostHighlightColor)).append("\n")
                            .append("User:\t\t").append(credential.getUser()).append("\n")
                            .append("Port:\t\t").append(credential.getPort()).append("\n")
                            .append("Type:\t\t").append("SSH").append("\n")
                            .append("Status:\t\t").append(status).append("\n")
                    ;
                }

                term.printDisplayEcho(connectionPrompt.toString());
                return;
            }

            spaceProcess(term, finalCmd);
        } else {
            if (StringUtils.isNotEmpty(command.getSpecify())) {
                term.printDisplayEcho(command.getSpecify());
            }
        }
    }

    @Override
    public IAddress consume() {
        return TERMINAL_ECHO;
    }

    private void spaceProcess(Term term, String cmd) {
        int commandHighlightColor = Term.theme.commandHighlightColor;
        int backgroundColor = Term.theme.backgroundColor;

        if (cmd.contains(StrUtil.SPACE)) {
            String[] split = cmd.split(StrUtil.SPACE);
            TermioCommand splitCommand = COMMANDS.get(split[0]);
            if (splitCommand == null) {
                AnisStringBuilder printMsg = new AnisStringBuilder().background(backgroundColor)
                        .append("Didn't find command '")
                        .front(commandHighlightColor).append(split[0]).clearFront().append("'");
                String alikeCommand = TermioCommand.findAlike(split[0]);
                if (alikeCommand != null) {
                    printMsg.append(", do you mean: ").front(commandHighlightColor).append(alikeCommand).clearFront();
                } else {
                    printMsg.append("\n")
                            .append("Press ")
                            .front(commandHighlightColor)
                            .append("Ctrl+U").clearFront()
                            .append(" to clear input. ");
                }
                term.printDisplayEcho(printMsg.toString());
            } else {
                if (StringUtils.isNotEmpty(splitCommand.getSpecify())) {
                    term.printDisplayEcho(splitCommand.getSpecify());
                }
            }
        } else {
            AnisStringBuilder titleMsg = new AnisStringBuilder().background(backgroundColor).append("Alternative commands: ");
            AnisStringBuilder printMsg = new AnisStringBuilder().background(backgroundColor);
            for (TermioCommand value : TermioCommand.values()) {
                if (value.cmd().startsWith(cmd)) {
                    if (StringUtils.isNotEmpty(value.getSpecify())) {
                        printMsg.front(commandHighlightColor).append(value.cmd()).clearFront().append(CharUtil.TAB);
                    }
                }
            }
            if (printMsg.length() != 0) {
                titleMsg.append(printMsg);
                term.printDisplayEcho(titleMsg.toString());
            } else {
                AnisStringBuilder builder = new AnisStringBuilder().background(backgroundColor)
                        .append("Didn't find command '")
                        .front(commandHighlightColor).append(cmd).clearFront()
                        .append("'\n")
                        .append("Press ")
                        .front(commandHighlightColor)
                        .append("Ctrl+U").clearFront()
                        .append(" to clear input. ");
                term.printDisplayEcho(builder.toString());
            }
        }
    }
}
