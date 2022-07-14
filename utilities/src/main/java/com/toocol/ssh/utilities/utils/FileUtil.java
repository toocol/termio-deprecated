package com.toocol.ssh.utilities.utils;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/17 13:58
 */
public class FileUtil {
    /**
     * .class file extension name
     */
    public static final String CLASS_EXT = FileNameUtil.EXT_CLASS;
    /**
     * .jar file extension name
     */
    public static final String JAR_FILE_EXT = FileNameUtil.EXT_JAR;
    /**
     * .jar file extension name in jar file.
     */
    public static final String JAR_PATH_EXT = ".jar!";
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
     * Transfer relative file path to fixed file path.
     *
     * @param relativePath relative path
     * @return fixed path
     */
    public static String relativeToFixed(String relativePath) {
        String fixedPath;
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

    /**
     * whether is Windows environment
     *
     * @return is Windows
     */
    public static boolean isWindows() {
        return FileNameUtil.WINDOWS_SEPARATOR == File.separatorChar;
    }

}
