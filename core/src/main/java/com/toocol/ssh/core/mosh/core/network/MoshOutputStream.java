package com.toocol.ssh.core.mosh.core.network;

import com.toocol.ssh.core.mosh.core.statesnyc.UserEvent;
import com.toocol.ssh.core.term.core.Printer;
import io.vertx.core.datagram.DatagramPacket;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Queue;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/28 22:17
 * @version: 0.0.1
 */
@SuppressWarnings("all")
public final class MoshOutputStream extends PipedOutputStream {

    private static final int DEFAULT_BUFF_SIZE = 1024 * 10;

    final byte[] buff = new byte[DEFAULT_BUFF_SIZE];

    private int curlen = 0;
    private final Transport transport;

    public MoshOutputStream(PipedInputStream in, Transport transport) throws IOException {
        super(in);
        this.transport = transport;
    }

    public void waitReading() {
        Queue<byte[]> queue = this.transport.getOutputQueue();
        new Thread(() -> {
            while (true) {
                try {
                    while (!queue.isEmpty()) {
                        byte[] bytes = queue.poll();
                        if (bytes != null) {
                            this.write(bytes, 0, bytes.length);
                            super.flush();
                        }
                    }
                    Thread.sleep(1);
                } catch (Exception e) {
                    Printer.printErr(e.getMessage());
                }
            }
        }).start();
    }

    public void pushBackUserBytesEvent() {
        if (curlen == 0) {
            return;
        }
        byte[] cutOff = new byte[curlen];
        System.arraycopy(buff, 0, cutOff, 0, curlen);
        curlen = 0;

        transport.pushBackEvent(new UserEvent.UserBytes(cutOff));
    }

    public synchronized void receivePacket(DatagramPacket datagramPacket) {
        byte[] bytes = datagramPacket.data().getBytes();
        transport.recvAndStash(bytes);
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
            pushBackUserBytesEvent();
        } catch (Exception e) {
            throw new IOException("Send packet failed");
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
    }

}
