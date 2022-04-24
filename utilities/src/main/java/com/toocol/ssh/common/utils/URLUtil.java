package com.toocol.ssh.common.utils;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.jar.JarFile;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/7 15:50
 */
public class URLUtil {

    public static String decode(String content, String charset) {
        return decode(content, CharsetUtil.charset(charset));
    }

    public static String decode(String content, Charset charset) {
        if (null == charset) {
            return content;
        }
        return URLDecoder.decode(content, charset);
    }

    public static JarFile getJarFile(URL url) {
        try {
            JarURLConnection urlConnection = (JarURLConnection) url.openConnection();
            return urlConnection.getJarFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
