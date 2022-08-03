package com.toocol.termio.utilities.console;

import jline.Terminal;
import jline.console.ConsoleReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/4 0:37
 * @version: 0.0.1
 */
public class TerminalConsoleReader extends ConsoleReader implements IConsoleReader {

    public TerminalConsoleReader() throws IOException {
    }

    public TerminalConsoleReader(InputStream in, OutputStream out, Terminal term) throws IOException {
        super(in, out, term);
    }

    @Override
    public int readChar() throws IOException {
        return readCharacter();
    }
}
