package com.toocol.termio.platform.console;

import com.toocol.termio.utilities.io.ReadableOutputStream;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/3 0:52
 * @version: 0.0.1
 */
public class MetadataPrinterOutputStream extends ReadableOutputStream<String> {
    private static final int BUFFER_SIZE = 2 << 16;
    private byte[] buffer = new byte[BUFFER_SIZE];
    {
        Arrays.fill(buffer, (byte) -1);
    }

    private volatile int readIndicator = 0;
    private volatile int writeIndicator = 0;

    @Override
    public void write(int b) throws IOException {
        if (buffer == null) {
            throw new IOException("MetadataPrinterOutputStream has been closed.");
        }
        synchronized (this) {
            writeToRingBuffer((byte) b);
        }
    }

    @Override
    public void flush() throws IOException {
        if (buffer == null) {
            throw new IOException("MetadataPrinterOutputStream has been closed.");
        }
        synchronized (this) {
            this.notify();
        }
    }

    @Override
    public String read() throws IOException {
        if (buffer == null) {
            throw new IOException("MetadataPrinterOutputStream has been closed.");
        }
        synchronized (this) {
            try {
                if (readIndicator == writeIndicator) {
                    this.wait();
                }
            } catch (Exception e) {
                throw new IOException("MetadataReaderOutputStream interrupt.");
            }
            return readFromRingBuffer();
        }
    }

    @Override
    public int available() throws IOException {
        if (buffer == null) {
            throw new IOException("MetadataPrinterOutputStream has been closed.");
        }
        synchronized (this) {
            return writeIndicator < readIndicator ?
                    BUFFER_SIZE - readIndicator + writeIndicator : writeIndicator - readIndicator;
        }
    }

    @Override
    public void close() throws IOException {
        buffer = null;
    }

    private synchronized String readFromRingBuffer() throws IOException {
        byte[] rec = new byte[available()];
        int idx = 0;
        while (readIndicator != writeIndicator) {
            rec[idx++] = buffer[readIndicator];
            buffer[readIndicator] = -1;
            readIndicator = readIndicator + 1 >= BUFFER_SIZE ? 0 : readIndicator + 1;
        }
        return new String(rec, StandardCharsets.UTF_8);
    }

    private synchronized void writeToRingBuffer(byte b) {
        buffer[writeIndicator] = b;
        writeIndicator = writeIndicator + 1 >= BUFFER_SIZE ? 0 : writeIndicator + 1;
    }
}
