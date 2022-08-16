package com.toocol.termio.platform.console;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/3 0:51
 * @version: 0.0.1
 */
public class MetadataWriterPrintStream extends PrintStream {

    public MetadataWriterPrintStream(OutputStream out) {
        super(out);
    }

}
