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

    public static void setOs(String type) {
        switch (type) {
            case BOOT_TYPE_CMD -> System.setProperty("os.name", "Windows");
            case BOOT_TYPE_BASH -> System.setProperty("os.name", "Unix");
            default -> System.exit(-1);
        }
        BOOT_TYPE = type;
    }
}
