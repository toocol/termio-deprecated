package com.toocol.ssh.core.configuration;

import java.util.Optional;

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/20 17:46
 */
public class SystemConfiguration {

    public static final String BOOT_TYPE_CMD = "cmd";
    public static final String BOOT_TYPE_BASH = "bash";

    public static String BOOT_TYPE;

    public static Optional<String> getExecuteMode() {
        switch (BOOT_TYPE) {
            case BOOT_TYPE_CMD:
                return Optional.of("/c");
            case BOOT_TYPE_BASH:
                return Optional.of("-c");
            default:
                return Optional.empty();
        }
    }

    public static Optional<String> getClearCmd() {
        switch (BOOT_TYPE) {
            case BOOT_TYPE_CMD:
                return Optional.of("cls");
            case BOOT_TYPE_BASH:
                return Optional.of("clear");
            default:
                return Optional.empty();
        }
    }

    public static void setOs(String type) {
        switch (type) {
            case BOOT_TYPE_CMD:
                System.setProperty("os.name", "Windows");
                break;
            case BOOT_TYPE_BASH:
                System.setProperty("os.name", "Unix");
                break;
            default:
                System.exit(-1);
                break;
        }
        BOOT_TYPE = type;
    }
}
