package utilities.console;

import com.toocol.ssh.utilities.console.Console;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/7/11 17:38
 */
class ConsoleTest {

    @Test
    void clearUnsupportedCharacterTest() {
        Console console = Console.get();
        String msg = "\n\u0004:\u0002@\u0013\n" +
                "�\u0002\u0012�\u0002\"�\u0002\u001B[?25l\n" +
                "bin/        data/       etc/        lib/        lost+found/ mnt/        proc/       run/        srv/        tmp/        var/\n" +
                "boot/       dev/        home/       lib64/      media/      opt/        root/       sbin/       sys/        usr/        \n" +
                "[root@vultrguest /]# cd /\u001B[?25h";
        byte[] diff = console.cleanUnsupportedCharacter(msg.getBytes(StandardCharsets.UTF_8));
        System.out.println(new String(diff, StandardCharsets.UTF_8));

        msg = "\u0004:\u0002@\n" +
                "\n" +
                ".\u0012,\"* pwd\u001B[?25l\n" +
                "/\n" +
                "[root@vultrguest /]# \u001B[?25h";
        diff = console.cleanUnsupportedCharacter(msg.getBytes(StandardCharsets.UTF_8));
        System.out.println(new String(diff, StandardCharsets.UTF_8));

        msg = "\u0004:\u0002@\b\n" +
                "C\u0012A\"?ls\u001B[?25l\n" +
                "\u001B[0;1;34mcpp\u001B[0m  run.sh\n" +
                "[root@vultrguest ~]# \u001B[?25h";
        diff = console.cleanUnsupportedCharacter(msg.getBytes(StandardCharsets.UTF_8));
        System.out.println(new String(diff, StandardCharsets.UTF_8));

        msg = "\u001B[0;1;34mcpp\u001B[0m  run.sh\u001B[K\u001B[38;22H\u001B[?25h";
        diff = console.cleanUnsupportedCharacter(msg.getBytes(StandardCharsets.UTF_8));
        System.out.println(new String(diff, StandardCharsets.UTF_8));

        msg = "\u0004:\u0002@Z\n" +
                "Y\u0012W\"U\u001B[?25l\u001B[27;22H\u001B[K\n" +
                "[root@vultrguest ~]# ls\n" +
                "\u001B[0;1;34mcpp\u001B[0m  run.sh\u001B[K\u001B[38;22H\u001B[?25h";
        diff = console.cleanUnsupportedCharacter(msg.getBytes(StandardCharsets.UTF_8));
        System.out.println(new String(diff, StandardCharsets.UTF_8));

        msg = "U\u0012S\"QLast metadata expiration check: 0:51:33 ago on Mon 11 Jul 2022 07:00:18 PM CST.";
        diff = console.cleanUnsupportedCharacter(msg.getBytes(StandardCharsets.UTF_8));
        System.out.println(new String(diff, StandardCharsets.UTF_8));

        msg = "X\u0012V\"TDependencies resolved.\u001B[?25l";
        diff = console.cleanUnsupportedCharacter(msg.getBytes(StandardCharsets.UTF_8));
        System.out.println(new String(diff, StandardCharsets.UTF_8));

        msg = "C\u0012A\"?ls\u001B[?25l";
        diff = console.cleanUnsupportedCharacter(msg.getBytes(StandardCharsets.UTF_8));
        System.out.println(new String(diff, StandardCharsets.UTF_8));

        msg = "�\u0003\u0012�\u0003\"�\u0003\u001B[0;37;44mMosh: You have 14 detached Mosh sessions on this server, with PIDs:\u001B[?25l\n" +
                "        - mosh [2242688]\n" +
                "        - mosh [2242802]\n" +
                "        - mosh [2242917]\n" +
                "        - mosh [2243316]\n" +
                "        - mosh [2243426]\n" +
                "        - mosh [2243517]\n" +
                "        - mosh [2243603]\n" +
                "        - mosh [2243738]\n" +
                "        - mosh [2243898]\n" +
                "        - mosh [2243961]\n" +
                "        - mosh [2244031]\n" +
                "        - mosh [2244149]\n" +
                "        - mosh [2244394]\n" +
                "        - mosh [2244523]\n" +
                "\n" +
                "\u001B[?25h\u001B[0m";
        diff = console.cleanUnsupportedCharacter(msg.getBytes(StandardCharsets.UTF_8));
        System.out.println(new String(diff, StandardCharsets.UTF_8));

        msg = "B\u0012@\">\u001B]0;root@vultrguest:/usr\u0007\u001B[?25l\n" +
                "[root@vultrguest usr]# \u001B[?25h";
        diff = console.cleanUnsupportedCharacter(msg.getBytes(StandardCharsets.UTF_8));
        System.out.println(new String(diff, StandardCharsets.UTF_8));

        msg = "\b\u0012\u0006\"\u0004ls";
        diff = console.cleanUnsupportedCharacter(msg.getBytes(StandardCharsets.UTF_8));
        System.out.println(new String(diff, StandardCharsets.UTF_8));

        msg = "\u001B\u0012\u0019\"\u0017\u001B[?25l\n" +
                "\u001B[K\u001B[1;22H\u001B[?25h";
        diff = console.cleanUnsupportedCharacter(msg.getBytes(StandardCharsets.UTF_8));
        System.out.println(new String(diff, StandardCharsets.UTF_8));
    }

}