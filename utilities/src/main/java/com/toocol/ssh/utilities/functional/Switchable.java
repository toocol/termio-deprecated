package com.toocol.ssh.utilities.functional;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/7/28 15:14
 */
public interface Switchable {
    String uri();

    String protocol();

    String currentPath();

    boolean alive();
}
