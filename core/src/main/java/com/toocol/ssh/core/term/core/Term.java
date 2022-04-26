package com.toocol.ssh.core.term.core;

import com.toocol.ssh.utilities.action.AbstractDevice;
import com.toocol.ssh.utilities.console.Console;
import com.toocol.ssh.utilities.utils.Tuple2;
import io.vertx.core.eventbus.EventBus;
import jline.console.ConsoleReader;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/14 11:09
 */
public final class Term extends AbstractDevice {

    public static final String PROMPT = " [termio] > ";
    private static final Console CONSOLE = Console.get();

    ConsoleReader reader;
    EventBus eventBus;
    final EscapeHelper escapeHelper;
    final TermHistoryHelper historyHelper;
    final TermReader termReader;
    final TermPrinter termPrinter;
    final TermCharEventDispatcher termCharEventDispatcher;

    public Term() {
        escapeHelper = new EscapeHelper();
        historyHelper = new TermHistoryHelper();
        termReader  = new TermReader(this);
        termPrinter = new TermPrinter(this);
        termCharEventDispatcher = new TermCharEventDispatcher();
    }
    {
        try {
            reader = new ConsoleReader();
        } catch (Exception e) {
            Printer.println("\nCreate console reader failed.");
            System.exit(-1);
        }
    }

    private static final Term INSTANCE = new Term();
    public static void setEventBus(EventBus eventBus) {
        INSTANCE.eventBus = eventBus;
    }

    public static volatile TermStatus status = TermStatus.TERMIO;
    public static TermTheme theme = TermTheme.DARK_THEME;
    public static int executeLine = 0;

    volatile StringBuilder lineBuilder = new StringBuilder();
    volatile AtomicInteger executeCursorOldX = new AtomicInteger(0);
    int displayZoneBottom = 0;


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

    public void printBackground() {
        termPrinter.printBackground();
    }

    public String readLine() {
        return termReader.readLine();
    }

    public static Term getInstance() {
        return INSTANCE;
    }

    public int getWidth() {
        return CONSOLE.getWindowWidth();
    }

    public int getHeight() {
        return CONSOLE.getWindowHeight();
    }

    public Tuple2<Integer, Integer> getCursorPosition() {
        String[] coord = CONSOLE.getCursorPosition().split(",");
        return new Tuple2<>(Integer.parseInt(coord[0]), Integer.parseInt(coord[1]));
    }

    public void setCursorPosition(int x, int y) {
        CONSOLE.setCursorPosition(x, y);
    }

    public void showCursor() {
        CONSOLE.showCursor();
    }

    public void hideCursor() {
        CONSOLE.hideCursor();
    }

    public void cursorLeft() {
        CONSOLE.cursorLeft();
    }

    public void cursorRight() {
        CONSOLE.cursorRight();
    }

    public void cursorBackLine(int lines) {
        CONSOLE.cursorBackLine(lines);
    }
}
