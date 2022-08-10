package com.toocol.termio.utilities.escape;


import com.toocol.termio.utilities.utils.Asable;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/7 18:45
 * @version: 0.0.1
 */
public abstract class AnisEscapeAction<T> implements Asable {

    public abstract void action(T target, IEscapeMode escapeMode, String escapeSequence);

}
