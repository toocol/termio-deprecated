package com.toocol.ssh.core.term.core;

import com.toocol.ssh.common.jni.TermioJNI;
import com.toocol.ssh.common.utils.Tuple2;
import io.vertx.core.eventbus.EventBus;
import jline.console.ConsoleReader;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/14 11:09
 */
public final class Term {

    public static final String PROMPT = " [termio] > ";

    private static final TermioJNI JNI = TermioJNI.getInstance();

    public Term(EventBus eventBus) {
        this.eventBus = eventBus;
        escapeHelper = new EscapeHelper();
        historyHelper = new TermHistoryHelper();
        termReader  = new TermReader(this);
        termPrinter = new TermPrinter(this);
    }
    {
        try {
            reader = new ConsoleReader();
        } catch (Exception e) {
            Printer.println("\nCreate console reader failed.");
            System.exit(-1);
        }
    }

    private static Term INSTANCE;
    public static void set(Term term) {
        INSTANCE = term;
    }

    public static volatile TermStatus status = TermStatus.TERMIO;
    public static TermTheme theme = TermTheme.DARK_THEME;
    public static int executeLine = 0;

    volatile AtomicInteger executeCursorOldX = new AtomicInteger(0);
    int displayZoneBottom = 0;

    ConsoleReader reader;
    final EventBus eventBus;
    final EscapeHelper escapeHelper;
    final TermHistoryHelper historyHelper;
    final TermReader termReader;
    final TermPrinter termPrinter;

    public void cleanDisplayZone() {
        termPrinter.cleanDisplayZone();
    }

    public void printExecution(String msg) {
        termPrinter.printExecution(msg);
    }

    public void printDisplay(String msg) {
        termPrinter.printDisplay(msg);
    }

    public void printDisplayBuffer() {
        termPrinter.printDisplayBuffer();
    }

    public void printCommandBuffer() {
        termPrinter.printCommandBuffer();
    }

    public String readLine() {
        return termReader.readLine();
    }

    public static Term getInstance() {
        return INSTANCE;
    }

    public int getWidth() {
        return JNI.getWindowWidth();
    }

    public int getHeight() {
        return JNI.getWindowHeight();
    }

    public Tuple2<Integer, Integer> getCursorPosition() {
        String[] coord = JNI.getCursorPosition().split(",");
        return new Tuple2<>(Integer.parseInt(coord[0]), Integer.parseInt(coord[1]));
    }

    public void setCursorPosition(int x, int y) {
        JNI.setCursorPosition(x, y);
    }

    public void showCursor() {
        JNI.showCursor();
    }

    public void hideCursor() {
        JNI.hideCursor();
    }

    public void cursorLeft() {
        JNI.cursorLeft();
    }

    public void cursorRight() {
        JNI.cursorRight();
    }

    public void cursorBackLine(int lines) {
        JNI.cursorBackLine(lines);
    }
}
