package com.toocol.ssh.core.command.vert;

import com.toocol.ssh.common.utils.PrintUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.EventBus;

import java.io.InputStreamReader;
import java.io.LineNumberReader;

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/19 18:08
 */
public class CommandExecutorVerticle extends AbstractVerticle {

    public static final String ADDRESS = "ssh.command.executor";

    @Override
    public void start() throws Exception {
        EventBus eventBus = vertx.eventBus();
        eventBus.consumer(ADDRESS, cmd -> {
            WorkerExecutor executor = vertx.createSharedWorkerExecutor("command-executor-worker");
            executor.executeBlocking(future -> {
                try {
                    String[] cmdA = {"cmd", "-C", String.valueOf(cmd)};
                    Process process = Runtime.getRuntime().exec(cmdA);
                    LineNumberReader br = new LineNumberReader(new InputStreamReader(
                            process.getInputStream()));
                    StringBuffer sb = new StringBuffer();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, res -> {

            });
        });

        PrintUtil.println("success start the command executor verticle.");
    }

}
