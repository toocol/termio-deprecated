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

    private final AtomicInteger readIndicator = new AtomicInteger(0);
    private final AtomicInteger writeIndicator = new AtomicInteger(0);

    @Override
    public int read() throws IOException {
        if (buffer == null) {
            throw new IOException("MetadataPrinterOutputStream has been closed.");
        }
        synchronized (this) {
            try {
                if (readIndicator.get() == writeIndicator.get()) {
                    this.wait();
                }
            } catch (Exception e) {
                throw new IOException("MetadataReaderInputStream interrupt.");
            }
        }
        return readFromRingBuffer();
    }

    @Override
    public byte[] readAllBytes() throws IOException {
        byte[] bytes = new byte[available()];
        int idx = 0;
        while (writeIndicator.get() != readIndicator.get()) {
            bytes[idx++] = (byte) readFromRingBuffer();
        }
        return bytes;
    }

    @Override
    public int available() throws IOException {
        if (buffer == null) {
            throw new IOException("MetadataPrinterOutputStream has been closed.");
        }
        return writeIndicator.get() < readIndicator.get() ?
                BUFFER_SIZE - readIndicator.get() + writeIndicator.get() : writeIndicator.get() - readIndicator.get();
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        if (buffer == null) {
            throw new IOException("MetadataPrinterOutputStream has been closed.");
        }
        for (byte b : bytes) {
            writeToRingBuffer(b);
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

    private int readFromRingBuffer() {
        int ch = buffer[readIndicator.get()];
        buffer[readIndicator.get()] = -1;
        readIndicator.set(readIndicator.get() + 1 >= BUFFER_SIZE ? 0 : readIndicator.get() + 1);
        return ch;
    }

    private void writeToRingBuffer(byte b) {
        buffer[writeIndicator.get()] = b;
        writeIndicator.set(writeIndicator.get() + 1 >= BUFFER_SIZE ? 0 : writeIndicator.get() + 1);
    }
}
