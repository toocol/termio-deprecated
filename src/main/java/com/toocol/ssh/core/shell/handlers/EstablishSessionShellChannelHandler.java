package com.toocol.ssh.core.shell.handlers;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.toocol.ssh.common.address.IAddress;
import com.toocol.ssh.common.handler.AbstractMessageHandler;
import com.toocol.ssh.common.utils.SnowflakeGuidGenerator;
import com.toocol.ssh.core.auth.vo.SshCredential;
import com.toocol.ssh.core.cache.CredentialCache;
import com.toocol.ssh.core.cache.SessionCache;
import com.toocol.ssh.core.cache.StatusCache;
import com.toocol.ssh.core.shell.core.Shell;
import com.toocol.ssh.core.shell.core.SshUserInfo;
import com.toocol.ssh.core.term.core.Printer;
import com.toocol.ssh.core.term.core.Termio;
import io.vertx.core.AsyncResult;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.Message;

import java.util.Properties;

import static com.toocol.ssh.core.shell.ShellAddress.*;
import static com.toocol.ssh.core.term.TermAddress.ADDRESS_ACCEPT_COMMAND;
import static com.toocol.ssh.core.term.TermAddress.MONITOR_TERMINAL;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 11:43
 */
public class EstablishSessionShellChannelHandler extends AbstractMessageHandler<Long> {

    private final SessionCache sessionCache = SessionCache.getInstance();

    private final JSch jSch = new JSch();
    private final SnowflakeGuidGenerator guidGenerator = new SnowflakeGuidGenerator();

    public EstablishSessionShellChannelHandler(Vertx vertx, WorkerExecutor executor, boolean parallel) {
        super(vertx, executor, parallel);
    }

    @Override
    public IAddress consume() {
        return ESTABLISH_SESSION;
    }

    @Override
    protected <T> void handleWithinBlocking(Promise<Long> promise, Message<T> message) throws Exception {
        int index = cast(message.body());
        SshCredential credential = CredentialCache.getCredential(index);
        assert credential != null;

        long sessionId = sessionCache.containSession(credential.getHost());
        boolean success = true;

        if (sessionId == 0) {
            StatusCache.HANGED_ENTER = false;
            try {
                Session session = jSch.getSession(credential.getUser(), credential.getHost(), credential.getPort());
                session.setPassword(credential.getPassword());
                session.setUserInfo(new SshUserInfo());
                Properties config = new Properties();
                config.put("StrictHostKeyChecking", "no");
                session.setConfig(config);
                session.setTimeout(30000);
                session.connect();

                sessionId = guidGenerator.nextId();
                sessionCache.putSession(sessionId, session);

                ChannelShell channelShell = cast(session.openChannel("shell"));
                int width = Termio.getInstance().getWidth();
                int height = Termio.getInstance().getHeight();
                channelShell.setPtyType("xterm", width, height, width, height);
                channelShell.connect();
                sessionCache.putChannelShell(sessionId, channelShell);

                Shell shell = new Shell(channelShell.getOutputStream(), channelShell.getInputStream());
                sessionCache.putShell(sessionId, shell);
            } catch (Exception e) {
                Printer.println("Connect failed, message = " + e.getMessage());
                success = false;
            } finally {
                // invoke gc() to clean up already un-use object during initial processing. (it's very efficacious :))
                System.gc();
            }
        } else {
            boolean reopenChannelShell = false;
            boolean regenerateShell = false;
            Session session = sessionCache.getSession(sessionId);
            if (!session.isConnected()) {
                session.connect();
                reopenChannelShell = true;
                regenerateShell = true;
            }

            ChannelShell channelShell = sessionCache.getChannelShell(sessionId);
            if (reopenChannelShell) {
                sessionCache.stopChannelShell(sessionId);
                channelShell = cast(session.openChannel("shell"));
                channelShell.connect();
                sessionCache.putChannelShell(sessionId, channelShell);
            } else if (channelShell.isClosed() || !channelShell.isConnected()) {
                channelShell.connect();
                regenerateShell = true;
            }

            if (regenerateShell) {
                Shell shell = new Shell(channelShell.getOutputStream(), channelShell.getInputStream());
                sessionCache.putShell(sessionId, shell);
            }

            StatusCache.HANGED_ENTER = true;
        }
        StatusCache.HANGED_QUIT = false;

        if (success) {
            promise.complete(sessionId);
        } else {
            promise.fail("Session establish failed.");
        }
    }

    @Override
    protected <T> void resultWithinBlocking(AsyncResult<Long> asyncResult, Message<T> message) throws Exception {
        Long sessionId = asyncResult.result();
        if (sessionId != null) {

            Printer.clear();

            if (StatusCache.HANGED_ENTER) {
                Printer.println("Invoke hanged session.\n");
            } else {
                Printer.println("Session established.\n");
            }

            StatusCache.SHOW_WELCOME = true;

            eventBus.send(MONITOR_TERMINAL.address(), sessionId);
            eventBus.send(EXHIBIT_SHELL.address(), sessionId);
            eventBus.send(ACCEPT_SHELL_CMD.address(), sessionId);

        } else {

            eventBus.send(ADDRESS_ACCEPT_COMMAND.address(), 2);

        }
    }

}
