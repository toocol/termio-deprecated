package com.toocol.ssh.core.configuration.vert;

import com.toocol.ssh.common.annotation.PreloadDeployment;
import com.toocol.ssh.common.utils.FileUtils;
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
    public static final String BOOT_TYPE_CMD = "cmd";
    public static final String BOOT_TYPE_BASH = "bash";

    public static String BOOT_TYPE;

    /**
     * the address of Git-Bash
     */
    public static String GIT_BASH_DIR;

    /**
     * the address of bash script 'openssh.sh'
     */
    public static String SCRIPT_SSH_DIR;

    /**
     * the start up selection: 1.[Single Window] 2.[Multiple Window]
     */
    public static int START_UP_MODE;

    @Override
    public void start() throws Exception {
        Buffer buffer = vertx.fileSystem().readFileBlocking(FileUtils.relativeToFixed("/starter/configuration.properties"));
        String config = buffer.getString(0, buffer.length());
        InputStream configInputStream = new ByteArrayInputStream(config.getBytes());
        Properties properties = new Properties();
        properties.load(configInputStream);
        GIT_BASH_DIR = properties.getProperty("ssh.terminal.git.bash.dir.bash");
        SCRIPT_SSH_DIR = FileUtils.relativeToFixed("/starter/openssh.sh");
        PrintUtil.println("success start the configuration verticle.");
    }

    public static String getExtraCmd() {
        return BOOT_TYPE_CMD.equals(BOOT_TYPE) ? "/c" : "-c";
    }

    public static String getClearCmd() {
        return BOOT_TYPE_CMD.equals(BOOT_TYPE) ? "cls" : "clear";
    }
}
