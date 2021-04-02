package com.toocol.ssh.core.router;

import com.toocol.ssh.core.command.vert.ClearScreenVerticle;
import com.toocol.ssh.core.view.vert.TerminalViewVerticle;
import io.vertx.core.AbstractVerticle;
import lombok.AllArgsConstructor;

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/4/2 10:12
 */
@AllArgsConstructor
public enum MessageAddress {
    /* TerminalViewVerticle */
    /**
     * when the screen has been cleaned.
     */
    ADDRESS_SCREEN_HAS_CLEARED("ssh.terminal.view", TerminalViewVerticle.class),
    /**
     * when user selected the launch mode.
     */
    ADDRESS_MODE_SELECTION_DONE("ssh.mode.selection.done", TerminalViewVerticle.class),

    /* ClearScreenVerticle */
    /**
     * to clean the screen
     */
    ADDRESS_CLEAR("ssh.command.clear", ClearScreenVerticle.class)

    /* CommandAcceptorVerticle */
    ;

    /**
     * the address string of message
     */
    public final String address;

    /**
     * the class that message located
     */
    public final Class<? extends AbstractVerticle> clazz;
}
