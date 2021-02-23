package com.toocol.ssh.core.configuration.vert;

import com.toocol.ssh.common.annotation.PreloadDeployment;
import com.toocol.ssh.common.utils.PrintUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/20 17:46
 */
@PreloadDeployment
public class ConfigurationVerticle extends AbstractVerticle {

    /**
     * the address of Git-Bash
     */
    public static String GIT_BASH_DIR;

    /**
     * the address of bash script 'openssh.sh'
     */
    public static String SCRIPT_SSH_DIR;

    @Override
    public void start() throws Exception {
        Buffer buffer = vertx.fileSystem().readFileBlocking("F:/ssh_terminal_starter/configuration.properties");
        String config = buffer.getString(0, buffer.length());
        InputStream configInputStream = new ByteArrayInputStream(config.getBytes());
        Properties properties = new Properties();
        properties.load(configInputStream);
        GIT_BASH_DIR = properties.getProperty("ssh.terminal.git.bash.dir.bash");
        SCRIPT_SSH_DIR = properties.getProperty("ssh.terminal.script.openssh");
        PrintUtil.println("success start the configuration verticle.");
    }
}
