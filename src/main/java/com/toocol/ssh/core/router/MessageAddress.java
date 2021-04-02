package com.toocol.ssh.core.router;

import lombok.AllArgsConstructor;

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/4/2 10:12
 */
public class MessageAddress {

    @AllArgsConstructor
    public enum TerminalViewVerticle {
        /**
         * when the screen has been cleaned.
         */
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
    public enum ClearScreenVerticle {
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
}
