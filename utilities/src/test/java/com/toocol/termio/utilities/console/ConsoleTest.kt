package com.toocol.termio.utilities.console

import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/7/11 17:38
 */
internal class ConsoleTest {
    @Test
    fun clearUnsupportedCharacterTest() {
        val console = Console.get()
        var msg = """
            
            :@
            ��"�[?25l
            bin/        data/       etc/        lib/        lost+found/ mnt/        proc/       run/        srv/        tmp/        var/
            boot/       dev/        home/       lib64/      media/      opt/        root/       sbin/       sys/        usr/        
            [root@vultrguest /]# cd /[?25h
            """.trimIndent()
        var diff = console.cleanUnsupportedCharacter(msg.toByteArray(StandardCharsets.UTF_8))
        println(String(diff, StandardCharsets.UTF_8))
        msg = """
            :@
            
            .,"* pwd[?25l
            /
            [root@vultrguest /]# [?25h
            """.trimIndent()
        diff = console.cleanUnsupportedCharacter(msg.toByteArray(StandardCharsets.UTF_8))
        println(String(diff, StandardCharsets.UTF_8))
        msg = """
            :@
            CA"?ls[?25l
            [0;1;34mcpp[0m  run.sh
            [root@vultrguest ~]# [?25h
            """.trimIndent()
        diff = console.cleanUnsupportedCharacter(msg.toByteArray(StandardCharsets.UTF_8))
        println(String(diff, StandardCharsets.UTF_8))
        msg = "\u001B[0;1;34mcpp\u001B[0m  run.sh\u001B[K\u001B[38;22H\u001B[?25h"
        diff = console.cleanUnsupportedCharacter(msg.toByteArray(StandardCharsets.UTF_8))
        println(String(diff, StandardCharsets.UTF_8))
        msg = """
            :@Z
            YW"U[?25l[27;22H[K
            [root@vultrguest ~]# ls
            [0;1;34mcpp[0m  run.sh[K[38;22H[?25h
            """.trimIndent()
        diff = console.cleanUnsupportedCharacter(msg.toByteArray(StandardCharsets.UTF_8))
        println(String(diff, StandardCharsets.UTF_8))
        msg = "U\u0012S\"QLast metadata expiration check: 0:51:33 ago on Mon 11 Jul 2022 07:00:18 PM CST."
        diff = console.cleanUnsupportedCharacter(msg.toByteArray(StandardCharsets.UTF_8))
        println(String(diff, StandardCharsets.UTF_8))
        msg = "X\u0012V\"TDependencies resolved.\u001B[?25l"
        diff = console.cleanUnsupportedCharacter(msg.toByteArray(StandardCharsets.UTF_8))
        println(String(diff, StandardCharsets.UTF_8))
        msg = "C\u0012A\"?ls\u001B[?25l"
        diff = console.cleanUnsupportedCharacter(msg.toByteArray(StandardCharsets.UTF_8))
        println(String(diff, StandardCharsets.UTF_8))
        msg = """��"�[0;37;44mMosh: You have 14 detached Mosh sessions on this server, with PIDs:[?25l
        - mosh [2242688]
        - mosh [2242802]
        - mosh [2242917]
        - mosh [2243316]
        - mosh [2243426]
        - mosh [2243517]
        - mosh [2243603]
        - mosh [2243738]
        - mosh [2243898]
        - mosh [2243961]
        - mosh [2244031]
        - mosh [2244149]
        - mosh [2244394]
        - mosh [2244523]

[?25h[0m"""
        diff = console.cleanUnsupportedCharacter(msg.toByteArray(StandardCharsets.UTF_8))
        println(String(diff, StandardCharsets.UTF_8))
        msg = """
            B@">]0;root@vultrguest:/usr[?25l
            [root@vultrguest usr]# [?25h
            """.trimIndent()
        diff = console.cleanUnsupportedCharacter(msg.toByteArray(StandardCharsets.UTF_8))
        println(String(diff, StandardCharsets.UTF_8))
        msg = "\b\u0012\u0006\"\u0004ls"
        diff = console.cleanUnsupportedCharacter(msg.toByteArray(StandardCharsets.UTF_8))
        println(String(diff, StandardCharsets.UTF_8))
        msg = """
            "[?25l
            [K[1;22H[?25h
            """.trimIndent()
        diff = console.cleanUnsupportedCharacter(msg.toByteArray(StandardCharsets.UTF_8))
        println(String(diff, StandardCharsets.UTF_8))
    }
}