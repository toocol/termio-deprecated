package com.toocol.ssh.core.command.vert;

import com.toocol.ssh.TerminalSystem;
import com.toocol.ssh.common.anno.Deployment;
import com.toocol.ssh.common.utils.PrintUtil;
import com.toocol.ssh.core.view.vert.TerminalViewVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.EventBus;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/19 18:08
 */
@Deployment
public class CommandExecutorVerticle extends AbstractVerticle {

    public static final String ADDRESS_EXECUTE = "ssh.command.execute";
    public static final String ADDRESS_CLEAR = "ssh.command.clear";

    private static final String COLON = ":";
    private static final String GIT_BASH = "git-bash";
    private static final String SSH = "ssh";

    @Override
    public void start() throws Exception {
        EventBus eventBus = vertx.eventBus();
        eventBus.consumer(ADDRESS_CLEAR, cmdMessage -> {
            WorkerExecutor executor = vertx.createSharedWorkerExecutor("command-executor-worker");
            executor.executeBlocking(future -> {
                try {
                    new ProcessBuilder("bash", "-c", "clear")
                            .inheritIO()
                            .start()
                            .waitFor();
                    future.complete("cleared");
                } catch (Exception e) {
                    PrintUtil.printErr("execute command failed!!");
                    future.complete("failed");
                }
            }, res -> {
                eventBus.send(TerminalViewVerticle.ADDRESS_SCREEN_HAS_CLEARED, "cleared");
            });
        });

        eventBus.consumer(ADDRESS_EXECUTE, cmdMessage -> {
            WorkerExecutor executor = vertx.createSharedWorkerExecutor("command-executor-worker");
            executor.executeBlocking(future -> {
                try {
                    String[] splitCmd = String.valueOf(cmdMessage.body()).split(COLON);
                    String cmdType = splitCmd[0];
                    String cmd = splitCmd[1];

                    Process process = new ProcessBuilder("bash", "-c", cmd)
                            .inheritIO()
                            .start();
                    processOperation(executor, process, cmdType);
                    process.waitFor();
                    future.complete(cmdMessage.body());
                } catch (Exception e) {
                    PrintUtil.printErr("execute command failed!!");
                    if (!future.isComplete()) {
                        future.complete("failed");
                    }
                }
            }, res -> {
                eventBus.send(CommandExecutorVerticle.ADDRESS_CLEAR, null);
            });
        });

        PrintUtil.println("success start the command executor verticle.");
    }

    private void processOperation(WorkerExecutor executor, Process process, String cmdType) {
        switch (cmdType) {
            case GIT_BASH:
                executor.executeBlocking(f -> {
                    try {
                        Thread.sleep(1000);
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                        writer.write("@joezeo951219\n");
                        writer.flush();
                        writer.close();
                        f.complete();
                    } catch (Exception e) {
                        PrintUtil.printErr("operate GitBash failed!!");
                    }
                }, r -> {});
                break;
            case SSH:
                executor.executeBlocking(f -> {
                    try {
                        Thread.sleep(1000);
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                        writer.write("@joezeo951219\n");
                        writer.flush();
                        writer.close();
                        f.complete();
                    } catch (Exception e) {
                        PrintUtil.printErr("operate OpenSSH failed!!");
                    }
                }, r -> {});
                break;
            default:
                break;
        }
    }

}
