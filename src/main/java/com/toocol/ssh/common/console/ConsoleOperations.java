/*
 * Copyright (c) 2002-2007, Marc Prud'hommeaux. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package com.toocol.ssh.common.console;

import java.awt.event.KeyEvent;

/**
 *  Symbolic constants for Console operations and virtual key bindings.
 *  @see KeyEvent
 *
 *  @author  <a href="mailto:mwp1@cornell.edu">Marc Prud'hommeaux</a>
 */
public interface ConsoleOperations {

    char BACKSPACE = '\b';
    char RESET_LINE = '\r';
    char KEYBOARD_BELL = '\07';
    char CTRL_A = 1;
    char CTRL_B = 2;
    char CTRL_C = 3;
    char CTRL_D = 4;
    char CTRL_E = 5;
    char CTRL_F = 6;
    char CTRL_K = 11;
    char CTRL_L = 12;
    char CTRL_N = 14;
    char CTRL_P = 16;
    char CTRL_OB = 27;
    char DELETE = 127;
    char CTRL_QM = 127;

    /**
     *  Operation that issues a newline.
     */
    short NEWLINE = -6;
    /**
     *  Operation that
     */
    short ADD = -42;
    /**
     *  Operation that pastes the contents of the clipboard into the line
     */
    short PASTE = -60;
}
