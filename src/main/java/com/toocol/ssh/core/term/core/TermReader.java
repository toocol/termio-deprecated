package com.toocol.ssh.core.term.core;

import jline.console.ConsoleReader;

import java.io.IOException;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/16 15:23
 */
public class TermReader {

    private ConsoleReader reader;

    {
        try {
            reader = new ConsoleReader();
            reader.addTriggeredAction('\t', (event) -> {
            });
        } catch (Exception e) {
            Printer.println("\nCreate console reader failed.");
            System.exit(-1);
        }
    }

    public String readLine(String prompt) {
        try {
            return reader.readLine(prompt);
        } catch (IOException e) {
            Printer.println("\nIO error.");
            System.exit(-1);
        }
        return null;
    }

    public ConsoleReader getReader() {
        return reader;
    }
}
