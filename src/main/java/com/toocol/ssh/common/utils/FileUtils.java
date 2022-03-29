package com.toocol.ssh.common.utils;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/17 13:58
 */
public class FileUtils {
    private static final String TWO_POINT = "..";
    private static final String POINT = ".";

    public static String userDir;
    static {
        userDir = System.getProperty("user.dir");
        userDir = userDir.replaceAll("\\\\\\\\", "/");
        userDir = userDir.replaceAll("\\\\", "/");
        userDir = userDir.split("/starter")[0];
    }

    /**
     * 将相对地址转化为固定地址
     *
     * @param relativePath 相对地址
     * @return 固定地址
     */
    public static String relativeToFixed(String relativePath) {
        String fixedPath = "";
        if (StringUtils.startsWith(relativePath, TWO_POINT)) {
            int index = StringUtils.lastIndexOf(userDir, "/");
            fixedPath = userDir.substring(0, index) + relativePath.substring(2);
        } else if (StringUtils.startsWith(relativePath, POINT)) {
            fixedPath = userDir + relativePath.substring(1);
        } else {
            fixedPath = toRootPath(relativePath);
        }
        return fixedPath;
    }

    private static String toRootPath(String path) {
        return userDir + path;
    }

    public static boolean checkAndCreateDir(String dirPath) {
        File file = new File(dirPath);
        if (!file.exists()) {
            return file.mkdir();
        }
        return file.isDirectory();
    }

    public static boolean checkAndCreateFile(String dirPath) throws IOException {
        File file = new File(dirPath);
        if (!file.exists()) {
            return file.createNewFile();
        }
        return file.isFile();
    }
}
