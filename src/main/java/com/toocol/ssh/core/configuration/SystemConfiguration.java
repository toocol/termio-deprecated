package com.toocol.ssh.core.configuration;

import com.toocol.ssh.common.utils.OsUtil;
import org.checkerframework.checker.units.qual.C;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/20 17:46
 */
public class SystemConfiguration {

    public static final String BOOT_TYPE_CMD = "cmd";
    public static final String BOOT_TYPE_BASH = "bash";

    public static final boolean HAVE_INSTALLED_VIM;
    static {
        Map<String, String> map = System.getenv();
        boolean flag = false;
        for (String key : map.keySet()) {
            if (OsUtil.isWindows() && "Path".equals(key)) {
                for (String path : map.get(key).split(";")) {
                    String[] split = path.split("\\\\");
                    if (split[split.length - 1].toLowerCase().contains("vim")) {
                        flag = true;
                        break;
                    }
                }
            } else if ("PATH".equals(key)){
                for (String path : map.get(key).split(";")) {
                    String[] split = path.split("\\\\");
                    if (split[split.length - 1].toLowerCase().contains("vim")) {
                        flag = true;
                        break;
                    }
                    split = path.split("/");
                    if (split[split.length - 1].toLowerCase().contains("vim")) {
                        flag = true;
                        break;
                    }
                }
            }
        }
        HAVE_INSTALLED_VIM = flag;
    }

    public static String BOOT_TYPE;

    public static Optional<String> getExecuteMode() {
        return switch (BOOT_TYPE) {
            case BOOT_TYPE_CMD -> Optional.of("/c");
            case BOOT_TYPE_BASH -> Optional.of("-c");
            default -> Optional.empty();
        };
    }

    public static Optional<String> getClearCmd() {
        return switch (BOOT_TYPE) {
            case BOOT_TYPE_CMD -> Optional.of("cls");
            case BOOT_TYPE_BASH -> Optional.of("clear");
            default -> Optional.empty();
        };
    }
}
