package com.toocol.termio.utilities.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/15 11:01
 */
public abstract class ReadableOutputStream<T> extends OutputStream {

    public abstract T read() throws IOException;

    public abstract int available() throws IOException;

}
