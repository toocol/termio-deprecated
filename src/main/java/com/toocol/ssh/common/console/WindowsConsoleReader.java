package com.toocol.ssh.common.console;

import com.toocol.ssh.common.jni.TermioJNI;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/14 16:57
 */
public class WindowsConsoleReader extends ConsoleReader{
    /**
     * On windows terminals, this character indicates that a 'special' key has
     * been pressed. This means that a key such as an arrow key, or delete, or
     * home, etc. will be indicated by the next character.
     */
    public static final int SPECIAL_KEY_INDICATOR = 224;

    /**
     * On windows terminals, this character indicates that a special key on the
     * number pad has been pressed.
     */
    public static final int NUMPAD_KEY_INDICATOR = 0;

    /**
     * When following the SPECIAL_KEY_INDICATOR or NUMPAD_KEY_INDICATOR,
     * this character indicates an left arrow key press.
     */
    public static final int LEFT_ARROW_KEY = 75;

    /**
     * When following the SPECIAL_KEY_INDICATOR or NUMPAD_KEY_INDICATOR
     * this character indicates an
     * right arrow key press.
     */
    public static final int RIGHT_ARROW_KEY = 77;

    /**
     * When following the SPECIAL_KEY_INDICATOR or NUMPAD_KEY_INDICATOR
     * this character indicates an up
     * arrow key press.
     */
    public static final int UP_ARROW_KEY = 72;

    /**
     * When following the SPECIAL_KEY_INDICATOR or NUMPAD_KEY_INDICATOR
     * this character indicates an
     * down arrow key press.
     */
    public static final int DOWN_ARROW_KEY = 80;

    /**
     * When following the SPECIAL_KEY_INDICATOR or NUMPAD_KEY_INDICATOR
     * this character indicates that
     * the delete key was pressed.
     */
    public static final int DELETE_KEY = 83;

    /**
     * When following the SPECIAL_KEY_INDICATOR or NUMPAD_KEY_INDICATOR
     * this character indicates that
     * the home key was pressed.
     */
    public static final int HOME_KEY = 71;

    /**
     * When following the SPECIAL_KEY_INDICATOR or NUMPAD_KEY_INDICATOR
     * this character indicates that
     * the end key was pressed.
     */
    public static final char END_KEY = 79;

    /**
     * When following the SPECIAL_KEY_INDICATOR or NUMPAD_KEY_INDICATOR
     * this character indicates that
     * the page up key was pressed.
     */
    public static final char PAGE_UP_KEY = 73;

    /**
     * When following the SPECIAL_KEY_INDICATOR or NUMPAD_KEY_INDICATOR
     * this character indicates that
     * the page down key was pressed.
     */
    public static final char PAGE_DOWN_KEY = 81;

    /**
     * When following the SPECIAL_KEY_INDICATOR or NUMPAD_KEY_INDICATOR
     * this character indicates that
     * the insert key was pressed.
     */
    public static final char INSERT_KEY = 82;

    /**
     * When following the SPECIAL_KEY_INDICATOR or NUMPAD_KEY_INDICATOR,
     * this character indicates that the escape key was pressed.
     */
    public static final char ESCAPE_KEY = 0;

    ReplayPrefixOneCharInputStream replayStream = new ReplayPrefixOneCharInputStream("UTF-8");
    InputStreamReader replayReader;

    protected WindowsConsoleReader(InputStream in) {
        super(in);

        try {
            replayReader = new InputStreamReader(replayStream, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int readVirtualKey() {
        TermioJNI jni = TermioJNI.getInstance();
        int indicator = jni.getCh();

        // in Windows terminals, arrow keys are represented by
        // a sequence of 2 characters. E.g., the up arrow
        // key yields 224, 72
        if (indicator == SPECIAL_KEY_INDICATOR
                || indicator == NUMPAD_KEY_INDICATOR) {
            int key = jni.getCh();

            return switch (key) {
                case UP_ARROW_KEY -> CTRL_P; // translate UP -> CTRL-P
                case LEFT_ARROW_KEY -> CTRL_B; // translate LEFT -> CTRL-B
                case RIGHT_ARROW_KEY -> CTRL_F; // translate RIGHT -> CTRL-F
                case DOWN_ARROW_KEY -> CTRL_N; // translate DOWN -> CTRL-N
                case DELETE_KEY -> CTRL_QM; // translate DELETE -> CTRL-?
                case HOME_KEY -> CTRL_A;
                case END_KEY -> CTRL_E;
                case PAGE_UP_KEY -> CTRL_K;
                case PAGE_DOWN_KEY -> CTRL_L;
                case ESCAPE_KEY -> CTRL_OB; // translate ESCAPE -> CTRL-[
                case INSERT_KEY -> CTRL_C;
                default -> 0;
            };
        } else if (indicator > 128) {
            try {
                // handle unicode characters longer than 2 bytes,
                // thanks to Marc.Herbert@continuent.com
                replayStream.setInput(indicator, in);
                // replayReader = new InputStreamReader(replayStream, encoding);
                indicator = replayReader.read();
            } catch (IOException e) {
                indicator = -1;
            }

        }

        return indicator;
    }

    static class ReplayPrefixOneCharInputStream extends InputStream {
        byte firstByte;
        int byteLength;
        InputStream wrappedStream;
        int byteRead;

        final String encoding;

        public ReplayPrefixOneCharInputStream(String encoding) {
            this.encoding = encoding;
        }

        public void setInput(int recorded, InputStream wrapped) throws IOException {
            this.byteRead = 0;
            this.firstByte = (byte) recorded;
            this.wrappedStream = wrapped;

            byteLength = 1;
            if (encoding.equalsIgnoreCase("UTF-8"))
                setInputUTF8(recorded, wrapped);
            else if (encoding.equalsIgnoreCase("UTF-16"))
                byteLength = 2;
            else if (encoding.equalsIgnoreCase("UTF-32"))
                byteLength = 4;
        }


        public void setInputUTF8(int recorded, InputStream wrapped) throws IOException {
            // 110yyyyy 10zzzzzz
            if ((firstByte & (byte) 0xE0) == (byte) 0xC0)
                this.byteLength = 2;
                // 1110xxxx 10yyyyyy 10zzzzzz
            else if ((firstByte & (byte) 0xF0) == (byte) 0xE0)
                this.byteLength = 3;
                // 11110www 10xxxxxx 10yyyyyy 10zzzzzz
            else if ((firstByte & (byte) 0xF8) == (byte) 0xF0)
                this.byteLength = 4;
            else
                throw new IOException("invalid UTF-8 first byte: " + firstByte);
        }

        public int read() throws IOException {
            if (available() == 0)
                return -1;

            byteRead++;

            if (byteRead == 1)
                return firstByte;

            return wrappedStream.read();
        }

        /**
         * InputStreamReader is greedy and will try to read bytes in advance. We
         * do NOT want this to happen since we use a temporary/"losing bytes"
         * InputStreamReader above, that's why we hide the real
         * wrappedStream.available() here.
         */
        public int available() {
            return byteLength - byteRead;
        }
    }
}
