package com.toocol.ssh.core.mosh.core.network;

import com.toocol.ssh.core.term.core.Printer;
import io.vertx.core.datagram.DatagramPacket;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/28 22:17
 * @version: 0.0.1
 */
public final class MoshOutputStream extends PipedOutputStream {

    private static final int DEFAULT_BUFF_SIZE = 1024 * 10;

    final byte[] buff = new byte[DEFAULT_BUFF_SIZE];

    private int curlen = 0;
    private final Transport transport;

    public MoshOutputStream(PipedInputStream in, Transport transport) throws IOException {
        super(in);
        this.transport = transport;
    }

    public void sendPacket() {
        if (curlen == 0) {
            return;
        }
        byte[] cutOff = new byte[curlen];
        System.arraycopy(buff, 0, cutOff, 0, curlen);
        curlen = 0;

        transport.send(new String(cutOff, StandardCharsets.UTF_8));
    }

    public void receivePacket(DatagramPacket datagramPacket) {
        try {
            byte[] bytes = datagramPacket.data().getBytes();
            this.write(bytes, 0, bytes.length);
            super.flush();
        } catch (IOException e) {
            Printer.printErr(e.getMessage());
        }
    }

    @Override
    public void write(@Nonnull byte[] bytes) throws IOException {
        System.arraycopy(bytes, 0, buff, curlen, bytes.length);
        curlen += bytes.length;
    }

    @Override
    public void write(int i) throws IOException {
        this.buff[curlen++] = (byte) i;
    }

    @Override
    public synchronized void flush() throws IOException {
        try {
            sendPacket();
        } catch (Exception e) {
            throw new IOException("Send packet failed");
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
    }

}
