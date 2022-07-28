package com.toocol.ssh.utilities.functional;

import com.toocol.ssh.utilities.utils.Asable;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/7/28 15:14
 */
public interface Switchable extends Asable, Comparable<Switchable> {
    /**
     * username@host
     */
    String uri();

    /**
     * SSH/MOSH
     */
    String protocol();

    /**
     * system file path
     */
    String currentPath();

    /**
     * whether is alive
     */
    boolean alive();

    /**
     * the weight when sort
     * the greater the weight, the lower the ranking
     */
    int weight();

    @Override
    default int compareTo(Switchable o) {
        return this.weight() - o.weight();
    }
}
