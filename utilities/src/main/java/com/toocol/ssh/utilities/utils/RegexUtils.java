package com.toocol.ssh.utilities.utils;

import java.util.regex.Pattern;

/**
 * 正则表达式工具类
 *
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/11 11:18
 */
public class RegexUtils {
    public static final int MATCH_IP = 1;
    public static final int MATCH_EMAIL = 2;

    private static final Pattern IP_PATTERN = Pattern.compile("^([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$");

    public static boolean match(String text, int matchType) {
        return switch (matchType) {
            case MATCH_IP -> matchIp(text);
            case MATCH_EMAIL -> matchEmail(text);
            default -> false;
        };
    }

    public static boolean matchIp(String text) {
        return match(text, IP_PATTERN);
    }

    public static boolean matchEmail(String text) {
        return match(text, EMAIL_PATTERN);
    }

    public static boolean match(String text, Pattern pattern) {
        return pattern.matcher(text).matches();
    }
}
