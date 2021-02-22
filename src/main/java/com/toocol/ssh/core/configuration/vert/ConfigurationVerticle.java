package com.toocol.ssh.core.configuration.vert;

import com.toocol.ssh.common.annotation.PreloadDeployment;
import com.toocol.ssh.common.utils.PrintUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import jdk.nashorn.internal.runtime.ECMAException;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/20 17:46
 */
@PreloadDeployment
public class ConfigurationVerticle extends AbstractVerticle {

    public static String GIT_BASH_DIR;

    @Override
    public void start() throws Exception {
        Buffer buffer = vertx.fileSystem().readFileBlocking("F:/workspace/github/ssh_terminal_starter/configuration.properties");
        String config = buffer.getString(0, buffer.length());
        InputStream inputStream = new ByteArrayInputStream(config.getBytes());
        Properties properties = new Properties();
        properties.load(inputStream);
        GIT_BASH_DIR = properties.getProperty("ssh.terminal.git.bash.dir.bash");
        PrintUtil.println("success start the configuration verticle.");
    }
}
