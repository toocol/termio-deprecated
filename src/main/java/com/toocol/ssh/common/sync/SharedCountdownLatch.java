package com.toocol.ssh.common.sync;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/12 16:43
 */
public class SharedCountdownLatch {

    private SharedCountdownLatch() {

    }

    private static final Map<String, CountDownLatch> sharedCountdownLatchMap = new HashMap<>();
    private static final ReentrantLock lock = new ReentrantLock();

    public static void countdown(Class<?> awaitClass, Class<?> workClass) {
        lock.lock();
        try {
            sharedCountdownLatchMap.computeIfPresent(transferKey(awaitClass, workClass), (k, v) -> {
                v.countDown();
                return v;
            });
        } catch (Exception e) {
            // do nothing
        } finally {
            lock.unlock();
        }
    }

    public static void await(Runnable runBeforeAwait, Class<?> awaitClass, Class<?>... workClasses) {
        CountDownLatch latch = null;
        lock.lock();
        try {
            latch = new CountDownLatch(workClasses.length);
            for (Class<?> workClass : workClasses) {
                sharedCountdownLatchMap.put(transferKey(awaitClass, workClass), latch);
            }
        } catch (Exception e) {
            // do nothing
        } finally {
            lock.unlock();
        }

        runBeforeAwait.run();

        if (latch == null) {
            return;
        }
        try {
            latch.await();
            for (Class<?> workClass : workClasses) {
                sharedCountdownLatchMap.put(transferKey(awaitClass, workClass), null);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static String transferKey(Class<?>... classes) {
        StringBuilder keyBuilder = new StringBuilder();
        for (Class<?> aClass : classes) {
            keyBuilder.append(aClass.getName()).append(":");
        }
        keyBuilder.deleteCharAt(keyBuilder.length() - 1);
        return keyBuilder.toString();
    }

}
