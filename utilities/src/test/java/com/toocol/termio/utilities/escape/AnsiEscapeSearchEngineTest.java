package com.toocol.termio.utilities.escape;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/17 11:20
 */
class AnsiEscapeSearchEngineTest {

    private final String msg = """
            \u001B[0m\u001B[1;49r\u001B[49;1H
            \u001B[r\u001B[25;1H[root@vultrguest /]# ll -a
            total 80
            dr-xr-xr-x.  19 root root  4096 Jul  2 21:35 \u001B[0;1;34m.
            \u001B[0mdr-xr-xr-x.  19 root root  4096 Jul  2 21:35 \u001B[0;1;34m..
            \u001B[0mlrwxrwxrwx.   1 root root     7 Jun 22  2021 \u001B[0;1;36mbin\u001B[0m -> \u001B[0;1;34musr/bin
            \u001B[0mdr-xr-xr-x.   5 root root  4096 Jun 18 16:54 \u001B[0;1;34mboot
            \u001B[0mdrwxr-xr-x.   3 root root  4096 Jan 24  2021 \u001B[0;1;34mdata
            \u001B[0mdrwxr-xr-x.  19 root root  2920 Jun 22  2021 \u001B[0;1;34mdev
            \u001B[0mdrwxr-xr-x. 107 root root 12288 Jul 10 23:46 \u001B[0;1;34metc
            \u001B[0mdrwxr-xr-x.   2 root root  4096 Jun 22  2021 \u001B[0;1;34mhome
            \u001B[0mlrwxrwxrwx.   1 root root     7 Jun 22  2021 \u001B[0;1;36mlib\u001B[0m -> \u001B[0;1;34musr/lib
            \u001B[0mlrwxrwxrwx.   1 root root     9 Jun 22  2021 \u001B[0;1;36mlib64\u001B[0m -> \u001B[0;1;34musr/lib64
            \u001B[0mdrwx------.   2 root root 16384 Feb 13  2020 \u001B[0;1;34mlost+found
            \u001B[0mdrwxr-xr-x.   2 root root  4096 Jun 22  2021 \u001B[0;1;34mmedia
            \u001B[0mdrwxr-xr-x.   2 root root  4096 Jun 22  2021 \u001B[0;1;34mmnt
            \u001B[0mdrwxr-xr-x.   4 root root  4096 Jun 22  2021 \u001B[0;1;34mopt
            \u001B[0mdr-xr-xr-x. 101 root root     0 Apr  6 10:19 \u001B[0;1;34mproc
            \u001B[0mdr-xr-x---.  10 root root  4096 Jul 25 20:01 \u001B[0;1;34mroot
            \u001B[0mdrwxr-xr-x.  35 root root  1000 Jul 24 03:06 \u001B[0;1;34mrun
            \u001B[0mlrwxrwxrwx.   1 root root     8 Jun 22  2021 \u001B[0;1;36msbin\u001B[0m -> \u001B[0;1;34musr/sbin
            \u001B[0mdrwxr-xr-x.   2 root root  4096 Jun 22  2021 \u001B[0;1;34msrv
            \u001B[0mdr-xr-xr-x.  13 root root     0 Apr  6 10:19 \u001B[0;1;34msys
            \u001B[0mdrwxrwxrwt.   4 root root  4096 Jul 25 22:26 \u001B[0;30;42mtmp
            \u001B[0mdrwxr-xr-x.  13 root root  4096 Jun 18 16:48 \u001B[0;1;34musr
            \u001B[0mdrwxr-xr-x.  21 root root  4096 Jun 18 16:42 \u001B[0;1;34mvar\u001B[50;22H\u001B[0m""";

    private static final String uberEscapeModePattern =
            "(\\u001b\\[\\d{1,4};\\d{1,4}[Hf])" +
                    "|((\\u001b\\[\\d{0,4}([HABCDEFGsu]|(6n)))|(\\u001b [M78]))" +
                    "|(\\u001b\\[[0123]?[JK])" +
                    "|(\\u001b\\[((?!38)(?!48)\\d{1,3};?)+m)" +
                    "|(\\u001b\\[(38)?(48)?;5;\\d{1,3}m)" +
                    "|(\\u001b\\[(38)?(48)?;2;\\d{1,3};\\d{1,3};\\d{1,3}m)" +
                    "|(\\u001b\\[=\\d{1,2}h)" +
                    "|(\\u001b\\[=\\d{1,2}l)" +
                    "|(\\u001b\\[\\?\\d{2,4}[lh])" +
                    "|(\\u001b\\[((\\d{1,3};){1,2}(((\\\\\")|'|\")[\\w ]+((\\\\\")|'|\");?)|(\\d{1,2};?))+p)";
    @Test
    void testRegex() {
        String[] split = msg.split(uberEscapeModePattern);
        System.out.println(Arrays.toString(split));
    }

    @Test
    void testSplit() {
        String msg = "abc\t\t\t\tabc";
        System.out.println(Arrays.toString(msg.split("\t")));
    }

}