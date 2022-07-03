package com.toocol.ssh.utilities.utils;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/7/2 19:34
 * @version:
 */
class AnisControlTest {

    @Test
    void testAnsiControl() {
        String str1 = "\u0004:\u0002@\u0007";
        String str2 = "�\u0005\u0012�\u0005\"�\u0005ps -ef|grep mosh\u001B[?25l";

        String enter1 = "\u0004:\u0002@\u0001\n" +
                "<\u0012:\"8\u001B]0;root@vultrguest:~\u0007\u001B[?25l";
        String enter2 = "\u0004:\u0002@\u0002\n" +
                "S\u0012Q\"O\u001B]0;root@vultrguest:~\u0007\u001B[?25l";
        System.out.println(enter1.getBytes(StandardCharsets.UTF_8).length + ":" + enter2.getBytes(StandardCharsets.UTF_8).length);

        String pwd1 = "\u0004:\u0002@G\n" +
                "+\u0012)\"'cd /\u001B[?25l\n" +
                "[root@vultrguest /]# \u001B[?25h";
        String pwd2 = "\u0004:\u0002@H\n" +
                ".\u0012,\"* pwd\u001B[?25l\n" +
                "/\n" +
                "[root@vultrguest /]# \u001B[?25h";
        System.out.println(pwd1.getBytes(StandardCharsets.UTF_8).length + ":" + pwd2.getBytes(StandardCharsets.UTF_8).length);
    }

}