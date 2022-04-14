package com.toocol.ssh.core.term.core;

import com.toocol.ssh.common.jni.TerminatioJNI;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/14 11:09
 */
public class Term {

    private Term() {
    }

    public static final String PROMPT = "[terminatio] > ";

    private static final Term INSTANCE = new Term();

    public static Term getInstance() {
        return INSTANCE;
    }

    public void initialize() {
        try {
            terminal = TerminalBuilder.builder()
                    .encoding(StandardCharsets.UTF_8)
                    .system(true)
                    .exec(true)
                    .jna(true)
                    .jansi(true)
                    .color(true)
                    .streams(System.in, System.out)
                    .build();
            terminal.echo(false);
        } catch (IOException e) {
            System.exit(-1);
        }

        reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .appName("terminatio")
                .build();

        printer = terminal.writer();
    }

    private Terminal terminal;
    private LineReader reader;
    private PrintWriter printer;

    public int getWidth() {
        return TerminatioJNI.getInstance().getWindowWidth();
    }

    public int getHeight() {
        return TerminatioJNI.getInstance().getWindowHeight();
    }

    public PrintWriter printer() {
        return printer;
    }

    public LineReader getReader() {
        return reader;
    }

}
