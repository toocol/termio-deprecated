package com.toocol.termio.desktop.api.ssh.handlers;

import com.toocol.termio.core.cache.StatusCache;
import com.toocol.termio.core.cache.StatusConstants;
import com.toocol.termio.core.shell.ShellAddress;
import com.toocol.termio.core.shell.core.Shell;
import com.toocol.termio.core.ssh.handlers.AbstractBlockingEstablishSshSessionHandler;
import com.toocol.termio.core.term.TermAddress;
import com.toocol.termio.core.term.core.Term;
import com.toocol.termio.core.term.core.TermStatus;
import com.toocol.termio.desktop.ui.terminal.DesktopTerminalPanel;
import com.toocol.termio.platform.component.ComponentsContainer;
import com.toocol.termio.utilities.functional.Ordered;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import org.jetbrains.annotations.NotNull;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 11:43
 */
@Ordered
public final class BlockingEstablishSshSessionHandler extends AbstractBlockingEstablishSshSessionHandler {

    public BlockingEstablishSshSessionHandler(Vertx vertx, Context context, boolean parallel) {
        super(vertx, context, parallel);
    }

    @Override
    protected <T> void resultBlocking(@NotNull AsyncResult<Long> asyncResult, @NotNull Message<T> message) throws Exception {
        Long sessionId = asyncResult.result();
        if (sessionId != null) {
            Shell shell = shellCache.getShell(sessionId);
            if (shell == null) {
                warn("Get Shell is null when try to entry shell.");
                eventBus.send(TermAddress.ACCEPT_COMMAND.address(), StatusConstants.CONNECT_FAILED);
                return;
            }

            DesktopTerminalPanel panel = ComponentsContainer.get(DesktopTerminalPanel.class, 1);
            panel.activeTerminal();

            shell.printAfterEstablish();
            StatusCache.SHOW_WELCOME = true;

            StatusCache.MONITOR_SESSION_ID = sessionId;
            Term.status = TermStatus.SHELL;

            eventBus.send(ShellAddress.DISPLAY_SHELL.address(), sessionId);
            eventBus.send(ShellAddress.RECEIVE_SHELL.address(), sessionId);

        } else {

            warn("Establish ssh connection failed.");
            eventBus.send(TermAddress.ACCEPT_COMMAND.address(), StatusConstants.CONNECT_FAILED);

        }
    }
}
