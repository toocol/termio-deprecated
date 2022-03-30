package com.toocol.ssh.core.command.commands.processors;

import com.toocol.ssh.common.utils.PrintUtil;
import com.toocol.ssh.core.command.commands.AbstractCommandProcessor;

import static com.toocol.ssh.core.configuration.vert.ConfigurationVerticle.BOOT_TYPE;
import static com.toocol.ssh.core.configuration.vert.ConfigurationVerticle.getExtraCmd;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 14:33
 */
public class ExecuteExternalShellProcessor extends AbstractCommandProcessor {

    @Override
    public <T> void process(T param) {
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
    }

}
