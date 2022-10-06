package com.toocol.termio.utilities.utils;

import java.io.IOException;
import java.util.Properties;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/11 14:27
 */
public class PomUtil {

    private static String mainClass;
    private static String version;

    static {
        Properties properties = new Properties();
        try {
            properties.load(PomUtil.class.getClassLoader().getResourceAsStream("app.properties"));
            if (!properties.isEmpty()) {
                version = properties.getProperty("revision");
                mainClass = properties.getProperty("main.class");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getVersion() {
        return version;
    }

    public static String getMainClass() {
        return mainClass;
    }

}
