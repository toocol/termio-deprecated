package com.toocol.ssh.common.console;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/14 16:59
 */
public class UnixConsoleReader extends ConsoleReader{

    public static final short ARROW_START = 27;
    public static final short ARROW_PREFIX = 91;
    public static final short ARROW_LEFT = 68;
    public static final short ARROW_RIGHT = 67;
    public static final short ARROW_UP = 65;
    public static final short ARROW_DOWN = 66;
    public static final short O_PREFIX = 79;
    public static final short HOME_CODE = 72;
    public static final short END_CODE = 70;

    public static final short DEL_THIRD = 51;

    private String ttyConfig;
    private boolean backspaceDeleteSwitched = false;

    ReplayPrefixOneCharInputStream replayStream = new ReplayPrefixOneCharInputStream("UTF-8");
    InputStreamReader replayReader;


    protected UnixConsoleReader(InputStream in) {
        super(in);
        try {
            replayReader = new InputStreamReader(replayStream, StandardCharsets.UTF_8);
            initializeTerminal();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int readVirtualKey() {
        int c = readCharacter(in);

        if (backspaceDeleteSwitched)
            if (c == DELETE)
                c = '\b';
            else if (c == '\b')
                c = DELETE;

        // in Unix terminals, arrow keys are represented by
        // a sequence of 3 characters. E.g., the up arrow
        // key yields 27, 91, 68
        if (c == ARROW_START) {
            //also the escape key is 27
            //thats why we read until we
            //have something different than 27
            //this is a bugfix, because otherwise
            //pressing escape and than an arrow key
            //was an undefined state
            while (c == ARROW_START)
                c = readCharacter(in);
            if (c == ARROW_PREFIX || c == O_PREFIX) {
                c = readCharacter(in);
                if (c == ARROW_UP) {
                    return CTRL_P;
                } else if (c == ARROW_DOWN) {
                    return CTRL_N;
                } else if (c == ARROW_LEFT) {
                    return CTRL_B;
                } else if (c == ARROW_RIGHT) {
                    return CTRL_F;
                } else if (c == HOME_CODE) {
                    return CTRL_A;
                } else if (c == END_CODE) {
                    return CTRL_E;
                } else if (c == DEL_THIRD) {
                    c = readCharacter(in); // read 4th
                    return DELETE;
                }
            }
        }

        // handle unicode characters, thanks for a patch from amyi@inf.ed.ac.uk
        if (c > 128) {
            try {
                // handle unicode characters longer than 2 bytes,
                // thanks to Marc.Herbert@continuent.com
                replayStream.setInput(c, in);
                // replayReader = new InputStreamReader(replayStream, encoding);
                c = replayReader.read();
            } catch (IOException e) {
                return -1;
            }

        }

        return c;
    }

    public int readCharacter(final InputStream in) {
        try {
            return in.read();
        } catch (Exception e) {
            // do nothing
        }
        return -1;
    }

    /**
     *  Remove line-buffered input by invoking "stty -icanon min 1"
     *  against the current terminal.
     */
    public void initializeTerminal() throws IOException, InterruptedException {
        // save the initial tty configuration
        ttyConfig = stty("-g");

        // sanity check
        if ((ttyConfig.length() == 0)
                || ((!ttyConfig.contains("="))
                && (!ttyConfig.contains(":")))) {
            throw new IOException("Unrecognized stty code: " + ttyConfig);
        }

        checkBackspace();

        // set the console to be character-buffered instead of line-buffered
        stty("-icanon min 1");

        // disable character echoing
        stty("-echo");

        // at exit, restore the original tty configuration (for JDK 1.3+)
        try {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    restoreTerminal();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));
        } catch (AbstractMethodError ame) {
            // JDK 1.3+ only method. Bummer.
        }
    }

    /**
     * Restore the original terminal configuration, which can be used when
     * shutting down the console reader. The ConsoleReader cannot be
     * used after calling this method.
     */
    public void restoreTerminal() throws Exception {
        if (ttyConfig != null) {
            stty(ttyConfig);
            ttyConfig = null;
        }
    }


    protected void checkBackspace(){
        String[] ttyConfigSplit = ttyConfig.split("[:=]");

        if (ttyConfigSplit.length < 7)
            return;

        if (ttyConfigSplit[6] == null)
            return;

        backspaceDeleteSwitched = ttyConfigSplit[6].equals("7f");
    }

    private static String stty(final String args)
            throws IOException, InterruptedException {
        return exec("stty " + args + " < /dev/tty").trim();
    }

    /**
     *  Execute the specified command and return the output
     *  (both stdout and stderr).
     */
    private static String exec(final String cmd)
            throws IOException, InterruptedException {
        return exec(new String[] {
                "sh",
                "-c",
                cmd
        });
    }

    /**
     *  Execute the specified command and return the output
     *  (both stdout and stderr).
     */
    private static String exec(final String[] cmd)
            throws IOException, InterruptedException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();

        Process p = Runtime.getRuntime().exec(cmd);
        int c;
        InputStream in;

        in = p.getInputStream();

        while ((c = in.read()) != -1) {
            bout.write(c);
        }

        in = p.getErrorStream();

        while ((c = in.read()) != -1) {
            bout.write(c);
        }

        p.waitFor();

        return bout.toString();
    }

    /**
     * This is awkward and inefficient, but probably the minimal way to add
     * UTF-8 support to JLine
     *
     * @author <a href="mailto:Marc.Herbert@continuent.com">Marc Herbert</a>
     */
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
                setInputUTF8();
            else if (encoding.equalsIgnoreCase("UTF-16"))
                byteLength = 2;
            else if (encoding.equalsIgnoreCase("UTF-32"))
                byteLength = 4;
        }

        public void setInputUTF8() throws IOException {
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
