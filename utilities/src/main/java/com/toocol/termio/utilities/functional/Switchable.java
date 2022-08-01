package com.toocol.termio.utilities.functional;

import com.toocol.termio.utilities.utils.Asable;

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
        int aliveThis = alive() ? 1 : 0;
        int aliveO = o.alive() ? 1 : 0;
        return aliveThis == aliveO ? (this.weight() == o.weight() ? this.uri().compareTo(o.uri()) : this.weight() - o.weight()) : aliveO - aliveThis;
    }
}
