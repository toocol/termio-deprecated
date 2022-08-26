package com.toocol.termio.platform.console;

import com.toocol.termio.utilities.io.WritableInputStream;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/4 0:34
 * @version: 0.0.1
 */
public class MetadataReaderInputStream extends WritableInputStream<byte[]> {
    private static final int BUFFER_SIZE = 2 << 16;
    private byte[] buffer = new byte[BUFFER_SIZE];
    {
        Arrays.fill(buffer, (byte) -1);
    }

    private volatile int readIndicator = 0;
    private volatile int writeIndicator = 0;

    @Override
    public int read() throws IOException {
        if (buffer == null) {
            throw new IOException("MetadataPrinterOutputStream has been closed.");
        }
        synchronized (this) {
            try {
                if (readIndicator == writeIndicator) {
                    this.wait();
                }
            } catch (Exception e) {
                throw new IOException("MetadataReaderInputStream interrupt.");
            }
            return readFromRingBuffer();
        }
    }

    @Override
    public byte[] readAllBytes() throws IOException {
        byte[] bytes = new byte[available()];
        int idx = 0;
        while (writeIndicator != readIndicator) {
            bytes[idx++] = (byte) readFromRingBuffer();
        }
        return bytes;
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
    public void write(byte[] bytes) throws IOException {
        if (buffer == null) {
            throw new IOException("MetadataPrinterOutputStream has been closed.");
        }
        synchronized (this) {
            for (byte b : bytes) {
                writeToRingBuffer(b);
            }
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
    public void close() throws IOException {
        buffer = null;
    }

    private synchronized int readFromRingBuffer() {
        int ch = buffer[readIndicator];
        buffer[readIndicator] = -1;
        readIndicator = readIndicator + 1 >= BUFFER_SIZE ? 0 : readIndicator + 1;
        return ch;
    }

    private synchronized void writeToRingBuffer(byte b) {
        buffer[writeIndicator] = b;
        writeIndicator = writeIndicator + 1 >= BUFFER_SIZE ? 0 : writeIndicator + 1;
    }
}
