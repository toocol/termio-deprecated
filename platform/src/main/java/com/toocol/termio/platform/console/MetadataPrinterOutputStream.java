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

    private final AtomicInteger readIndicator = new AtomicInteger(0);
    private final AtomicInteger writeIndicator = new AtomicInteger(0);

    @Override
    public void write(int b) throws IOException {
        if (buffer == null) {
            throw new IOException("MetadataPrinterOutputStream has been closed.");
        }
        writeToRingBuffer((byte) b);
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
                if (readIndicator.get() == writeIndicator.get()) {
                    this.wait();
                }
            } catch (Exception e) {
                throw new IOException("MetadataReaderOutputStream interrupt.");
            }
        }
        return readFromRingBuffer();
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
    public void close() throws IOException {
        buffer = null;
    }

    private String readFromRingBuffer() throws IOException {
        byte[] rec = new byte[available()];
        int idx = 0;
        while (readIndicator.get() != writeIndicator.get()) {
            rec[idx++] = buffer[readIndicator.get()];
            buffer[readIndicator.get()] = -1;
            readIndicator.set(readIndicator.get() + 1 >= BUFFER_SIZE ? 0 : readIndicator.get() + 1);
        }
        return new String(rec, StandardCharsets.UTF_8);
    }

    private void writeToRingBuffer(byte b) {
        buffer[writeIndicator.get()] = b;
        writeIndicator.set(writeIndicator.get() + 1 >= BUFFER_SIZE ? 0 : writeIndicator.get() + 1);
    }
}
