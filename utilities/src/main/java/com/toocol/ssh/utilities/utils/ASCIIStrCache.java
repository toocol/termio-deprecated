package com.toocol.ssh.utilities.utils;

/**
 * ASCII char correspond string cache;
 *
 * @author Joezeo
 */
public class ASCIIStrCache {

    private static final int ASCII_LENGTH = 128;
    private static final String[] CACHE = new String[ASCII_LENGTH];

    static {
        for (char c = 0; c < ASCII_LENGTH; c++) {
            CACHE[c] = String.valueOf(c);
        }
    }

    public static String toString(char c) {
        return c < ASCII_LENGTH ? CACHE[c] : String.valueOf(c);
    }
}
