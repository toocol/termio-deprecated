package com.toocol.termio.utilities.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/15 11:00
 */
public abstract class WritableInputStream<T> extends InputStream {

    public abstract void write(T data) throws IOException;

    public abstract void flush() throws IOException;

}
