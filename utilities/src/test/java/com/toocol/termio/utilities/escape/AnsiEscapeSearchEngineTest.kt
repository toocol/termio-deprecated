package com.toocol.termio.utilities.escape

import kotlin.test.Test

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/17 11:20
 */
internal class AnsiEscapeSearchEngineTest {

    @Test
    fun testRegex() {
        val regex = Regex(pattern = uberEscapeModePattern)
        val split = msg.split(regex).toTypedArray()
        println(split.contentToString())
        println("---")
        regex.findAll(msg).forEach { line -> println(line.value.replace("\u001b", "ESC")) }
    }

    @Test
    fun testSearchEngine() {
        val searchEngine: AnsiEscapeSearchEngine<TestEscapeSupporter> = AnsiEscapeSearchEngine()
        searchEngine.actionOnEscapeMode(msg, TestEscapeSupporter())
    }

    companion object {
        private const val uberEscapeModePattern = """(\u001b\[\d{1,4};\d{1,4}[Hf])""" +
                """|((\u001b\[\d{0,4}([HABCDEFGsu]|(6n)))|(\u001b [M78]))""" +
                """|(\u001b\[[0123]?[JK])""" +
                """|(\u001b\[((?!38)(?!48)\d{1,3};?)+m)""" +
                """|(\u001b\[(38)?(48)?;5;\d{1,3}m)""" +
                """|(\u001b\[(38)?(48)?;2;\d{1,3};\d{1,3};\d{1,3}m)""" +
                """|(\u001b\[=\d{1,2}h)""" +
                """|(\u001b\[=\d{1,2}l)""" +
                """|(\u001b\[\?\d{2,4}[lh])""" +
                """|(\u001b\[((\d{1,3};){1,2}(((\\")|'|")[\w ]+((\\")|'|");?)|(\d{1,2};?))+p)"""

        private const val msg = """[0;84;"Hello";0;110;"World"p
            [?25l[?1049h
            [=7l[=3l
            [=0h[=3h[=7h[=19h
            [38;2;123;33;23m[48;2;32;44;23m
            [38;5;123m[48;5;32m
            [32m[43m[101m
            [J[0J[K[2K
            [6E 7[6n[0m[1;49r[49;1H
            [r[25;1H[root@vultrguest /]# ll -a\[6A
            total 80
            dr-xr-xr-x.  19 root root  4096 Jul  2 21:35 [0;1;34m.
            [0mdr-xr-xr-x.  19 root root  4096 Jul  2 21:35 [0;1;34m..
            [0mlrwxrwxrwx.   1 root root     7 Jun 22  2021 [0;1;36mbin[0m -> [0;1;34musr/bin
            [0mdr-xr-xr-x.   5 root root  4096 Jun 18 16:54 [0;1;34mboot
            [0mdrwxr-xr-x.   3 root root  4096 Jan 24  2021 [0;1;34mdata
            [0mdrwxr-xr-x.  19 root root  2920 Jun 22  2021 [0;1;34mdev
            [0mdrwxr-xr-x. 107 root root 12288 Jul 10 23:46 [0;1;34metc
            [0mdrwxr-xr-x.   2 root root  4096 Jun 22  2021 [0;1;34mhome
            [0mlrwxrwxrwx.   1 root root     7 Jun 22  2021 [0;1;36mlib[0m -> [0;1;34musr/lib
            [0mlrwxrwxrwx.   1 root root     9 Jun 22  2021 [0;1;36mlib64[0m -> [0;1;34musr/lib64
            [0mdrwx------.   2 root root 16384 Feb 13  2020 [0;1;34mlost+found
            [0mdrwxr-xr-x.   2 root root  4096 Jun 22  2021 [0;1;34mmedia
            [0mdrwxr-xr-x.   2 root root  4096 Jun 22  2021 [0;1;34mmnt
            [0mdrwxr-xr-x.   4 root root  4096 Jun 22  2021 [0;1;34mopt
            [0mdr-xr-xr-x. 101 root root     0 Apr  6 10:19 [0;1;34mproc
            [0mdr-xr-x---.  10 root root  4096 Jul 25 20:01 [0;1;34mroot
            [0mdrwxr-xr-x.  35 root root  1000 Jul 24 03:06 [0;1;34mrun
            [0mlrwxrwxrwx.   1 root root     8 Jun 22  2021 [0;1;36msbin[0m -> [0;1;34musr/sbin
            [0mdrwxr-xr-x.   2 root root  4096 Jun 22  2021 [0;1;34msrv
            [0mdr-xr-xr-x.  13 root root     0 Apr  6 10:19 [0;1;34msys
            [0mdrwxrwxrwt.   4 root root  4096 Jul 25 22:26 [0;30;42mtmp
            [0mdrwxr-xr-x.  13 root root  4096 Jun 18 16:48 [0;1;34musr
            [0mdrwxr-xr-x.  21 root root  4096 Jun 18 16:42 [0;1;34mvar[50;22H[0m
            """
    }
}