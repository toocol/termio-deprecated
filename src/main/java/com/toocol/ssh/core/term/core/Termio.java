package com.toocol.ssh.core.term.core;

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
public class Termio {

    private Termio() {
    }

    public static final String PROMPT = "[terminatio] > ";
    public static final int WIDTH = 180;
    public static final int HEIGHT = 50;

    private static final Termio INSTANCE = new Termio();

    public static Termio getInstance() {
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

    public Terminal getTerminal() {
        return terminal;
    }

    public PrintWriter printer() {
        return printer;
    }

    public LineReader getReader() {
        return reader;
    }

}
