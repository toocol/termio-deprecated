package com.toocol.ssh.core.router;

import com.toocol.ssh.core.router.annotation.Route;
import lombok.AllArgsConstructor;

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/4/2 10:12
 */
public class MessageAddress {

    public interface IAddress {
    }

    @AllArgsConstructor
    public enum TerminalViewVerticle implements IAddress{
        /**
         * when the screen has been cleaned.
         */
        @Route(nextAddress = {})
        ADDRESS_SCREEN_HAS_CLEARED("ssh.terminal.view"),

        /**
         * when user selected the launch mode.
         */
        ADDRESS_MODE_SELECTION_DONE("ssh.mode.selection.done"),
        ;

        /**
         * the address string of message
         */
        public final String address;
    }

    @AllArgsConstructor
    public enum ClearScreenVerticle implements IAddress{
        /**
         * to clean the screen
         */
        ADDRESS_CLEAR("ssh.command.clear")
        ;

        /**
         * the address string of message
         */
        public final String address;
    }

    @AllArgsConstructor
    public enum CommandAcceptorVerticle implements IAddress {
        /**
         * to accept the user command input
         */
        ADDRESS_ACCEPT_COMMAND("ssh.command.accept"),
        /**
         * accept anykey input
         */
        ADDRESS_ACCEPT_ANYKEY("ssh.accept.anykey"),
        /**
         * accept user select the launch mode
         */
        ADDRESS_ACCEPT_SELECTION("ssh.accept.selection")
        ;
        /**
         * the address string of message
         */
        public final String address;
    }

    @AllArgsConstructor
    public enum CommandExecutorVerticle implements IAddress {
        ;
        /**
         * the address string of message
         */
        public final String address;
    }
}
