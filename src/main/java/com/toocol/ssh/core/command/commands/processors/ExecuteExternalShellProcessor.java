package com.toocol.ssh.core.command.commands.processors;

import com.toocol.ssh.common.utils.PrintUtil;
import com.toocol.ssh.core.command.commands.AbstractCommandProcessor;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.concurrent.*;

import static com.toocol.ssh.core.configuration.vert.ConfigurationVerticle.BOOT_TYPE;
import static com.toocol.ssh.core.configuration.vert.ConfigurationVerticle.getExtraCmd;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 14:33
 */
public class ExecuteExternalShellProcessor extends AbstractCommandProcessor {

    private final ThreadFactory factory = new DefaultThreadFactory("external_shell_pool", true);
    private final ExecutorService executorService = new ThreadPoolExecutor(10, 10, 0,
            TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(1024), factory);

    @Override
    public <T> void process(T param) {
        executorService.submit(() -> {
            try {
                String cmd = String.valueOf(param);
                Process process = new ProcessBuilder(BOOT_TYPE, getExtraCmd(), cmd)
                        .inheritIO()
                        .start();
                process.waitFor();
                process.destroy();
            } catch (Exception e) {
                PrintUtil.printErr("execute command failed!!");
            }
        });
    }
}
