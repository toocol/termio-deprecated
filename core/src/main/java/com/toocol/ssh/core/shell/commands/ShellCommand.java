package com.toocol.ssh.core.shell.commands;

import com.toocol.ssh.core.shell.commands.processors.*;
import com.toocol.ssh.core.shell.core.Shell;
import com.toocol.ssh.core.term.core.Term;
import com.toocol.ssh.utilities.anis.AnisStringBuilder;
import com.toocol.ssh.utilities.command.ICommand;
import com.toocol.ssh.utilities.execeptions.RemoteDisconnectException;
import com.toocol.ssh.utilities.utils.CharUtil;
import com.toocol.ssh.utilities.utils.StrUtil;
import com.toocol.ssh.utilities.utils.Tuple2;
import io.vertx.core.eventbus.EventBus;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/22 13:21
 */
public enum ShellCommand implements ICommand {
    /**
     * shell's command enums
     */
    CMD_EXIT("exit", new ShellExitCmdProcessor(), "Exit current shell, close ssh connection and destroy connect channel."),
    CMD_HANG("hang", new ShellHangCmdProcessor(), "Will not close the connection, exit shell with connection running in the background."),
    CMD_UF("uf", new ShellUfCmdProcessor(), "Batch upload local files to remote connection."),
    CMD_DF("df", new ShellDfCmdProcessor(), "Batch download remote files to local."),
    CMD_LC("lc", new ShellLcCmdProcessor(), "List all the connection properties to quick switch."),
    CMD_CLEAR("clear", new ShellClearCmdProcessor(), null),
    ;

    public static final Map<String, ShellCommand> COMMANDS = new HashMap<>();

    static {
        Arrays.stream(values())
                .forEach(command -> COMMANDS.put(command.cmd, command));
    }

    private final String cmd;
    private final ShellCommandProcessor commandProcessor;
    private final String comment;

    ShellCommand(String cmd, ShellCommandProcessor commandProcessor, String comment) {
        this.cmd = cmd;
        this.commandProcessor = commandProcessor;
        this.comment = comment;
    }

    public static Optional<ShellCommand> cmdOf(String cmd) {
        String originCmd = cmd.trim().replaceAll(" {2,}", " ").split(" ")[0];
        ShellCommand shellCommand = COMMANDS.get(originCmd);
        return Optional.ofNullable(shellCommand);
    }

    public static String help() {
        AnisStringBuilder helpBuilder = new AnisStringBuilder().background(Term.theme.displayBackGroundColor);
        helpBuilder.append("Shell commands:\t[param] means optional param\n");
        for (ShellCommand command : values()) {
            if (StringUtils.isEmpty(command.comment)) {
                continue;
            }
            helpBuilder.front(Term.theme.commandHighlightColor).append(command.cmd).deFront()
                    .append(" ".repeat(23 - command.cmd.length())).append(command.comment).append(CharUtil.LF);
        }
        return helpBuilder.toString();
    }

    public final Tuple2<String, Long> processCmd(EventBus eventBus, Shell shell, AtomicBoolean isBreak, String msg) {
        if (this.commandProcessor == null) {
            return new Tuple2<>(StrUtil.EMPTY, null);
        }
        Tuple2<String, Long> result = new Tuple2<>();
        try {
            if (shell.getRemoteCmd().length() > 0) {
                for (int idx = 0; idx < shell.getRemoteCmd().length(); idx++) {
                    shell.write('\u007F');
                }
                shell.flush();
            }

            result = this.commandProcessor.process(eventBus, shell, isBreak, msg);
        } catch (RemoteDisconnectException e) {
            isBreak.set(true);
            result.second(shell.getSessionId());
        }
        return result;
    }

    public String cmd() {
        return cmd;
    }
}
