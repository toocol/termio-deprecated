package com.toocol.termio.utilities.escape;

import com.toocol.termio.utilities.escape.actions.AnsiEscapeAction;

import java.util.List;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/16 23:54
 * @version: 0.0.1
 */
public interface EscapeCodeSequenceSupporter<T> {

    List<AnsiEscapeAction<T>> registerActions();

    void printOut(String text);

}
