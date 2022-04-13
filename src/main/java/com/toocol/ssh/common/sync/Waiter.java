package com.toocol.ssh.common.sync;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/13 20:54
 */
public class Waiter {

    private volatile boolean isWaiting = false;

    public void waitFor() {
        try {
            isWaiting = true;
            wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void tryInvoke() {
        if (isWaiting) {
            notify();
        }
    }

}
