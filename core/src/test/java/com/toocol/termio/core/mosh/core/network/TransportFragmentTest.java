package com.toocol.termio.core.mosh.core.network;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/7/2 0:24
 * @version:
 */
class TransportFragmentTest {

    @Test
    void testPool() {
        Runtime runtime = Runtime.getRuntime();
        int loop = 100000000;
        long usePool;
        long notPool;
        long useMem;
        long notMem;

        List<TransportFragment.Fragment> list = new ArrayList<>(100000000);
        long start = System.currentTimeMillis();
        TransportFragment.Pool pool = new TransportFragment.Pool();
        pool.init();
        long sm = runtime.totalMemory() - runtime.freeMemory();
        for (int i = 0; i < loop; i++) {
            list.add(pool.getObject());
            pool.recycle();
        }
        long end = System.currentTimeMillis();
        long em = runtime.totalMemory() - runtime.freeMemory();
        usePool = end - start;
        useMem = em - sm;

        list.clear();
        start = System.currentTimeMillis();
        sm = runtime.totalMemory() - runtime.freeMemory();
        for (int i = 0; i < loop; i++) {
            list.add(new TransportFragment.Fragment());
        }
        end = System.currentTimeMillis();
        em = runtime.totalMemory() - runtime.freeMemory();
        notPool = end - start;
        notMem = em - sm;
        System.out.println("usePoll: " + usePool);
        System.out.println("useMem: " + useMem / 1024 / 1024);
        System.out.println("notPoll: " + notPool);
        System.out.println("notMem: " + notMem / 1024 / 1024);
    }

}