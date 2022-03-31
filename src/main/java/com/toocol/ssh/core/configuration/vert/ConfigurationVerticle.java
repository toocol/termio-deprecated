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

    @Override
    public void start() throws Exception {
        PrintUtil.println("success start the configuration verticle.");
    }

    public static String getExtraCmd() {
        return BOOT_TYPE_CMD.equals(BOOT_TYPE) ? "/c" : "-c";
    }

    public static String getClearCmd() {
        return BOOT_TYPE_CMD.equals(BOOT_TYPE) ? "cls" : "clear";
    }
}
