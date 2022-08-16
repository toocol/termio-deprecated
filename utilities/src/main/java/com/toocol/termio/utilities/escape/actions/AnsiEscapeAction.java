package com.toocol.termio.utilities.escape.actions;


import com.toocol.termio.utilities.escape.IEscapeMode;
import com.toocol.termio.utilities.utils.Asable;

import java.util.List;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/7 18:45
 * @version: 0.0.1
 */
public abstract class AnsiEscapeAction<T> implements Asable {

    public abstract Class<? extends IEscapeMode> focusMode();

    public abstract void action(T executeTarget, IEscapeMode escapeMode, List<Object> params);

}
