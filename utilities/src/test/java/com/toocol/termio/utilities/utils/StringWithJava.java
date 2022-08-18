package com.toocol.termio.utilities.utils;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/18 20:41
 * @version: 0.0.1
 */
@SuppressWarnings("all")
public class StringWithJava {
    public void processingString() {
        var str = "";
        var start = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            str += "Hello world~" + i;
        }
        System.out.println("Java String use time: " + (System.currentTimeMillis() - start));

        var builder = new StringBuilder();
        start = System.currentTimeMillis();
        for (int i = 0; i < 100000000; i++) {
            builder.append("Hello world~" + i);
        }
        System.out.println("Java StringBuilder use time: " + (System.currentTimeMillis() - start));
    }
}
