package com.toocol.ssh.core.mosh.core.network;

import com.toocol.ssh.core.mosh.core.crypto.Crypto;
import com.toocol.ssh.core.term.core.Printer;
import com.toocol.ssh.utilities.utils.Timestamp;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.datagram.DatagramPacket;
import io.vertx.core.datagram.DatagramSocket;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Equivalent to network.h/network.cc/
 *
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/28 22:17
 * @version: 0.0.1
 */
public final class MoshOutputStream extends PipedOutputStream {

    private static final int DEFAULT_BUFF_SIZE = 1024 * 10;

    final DatagramSocket socket;
    final Transport transport;
    final Crypto.Session session;
    final byte[] buff = new byte[DEFAULT_BUFF_SIZE];

    private int curlen = 0;

    private short savedTimestamp;
    private long savedTimestampReceivedAt;
    private long expectedReceiverSeq;

    private long lastHeard;
    private long lastPortChoice;
    private long lastRoundtripSuccess;

    public MoshOutputStream(PipedInputStream in, DatagramSocket socket, Transport transport) throws IOException {
        super(in);
        this.socket = socket;
        this.transport = transport;
        this.session = new Crypto.Session(new Crypto.Base64Key(transport.key));
    }

    public void sendPacket() {
        if (curlen == 0) {
            return;
        }
        byte[] cutOff = new byte[curlen];
        System.arraycopy(buff, 0, cutOff, 0, curlen);
        curlen = 0;

        MoshPacket packet = newPacket(cutOff);
        socket.send(Buffer.buffer(session.encrypt(packet.toMessage())), transport.port, transport.serverHost);
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
        this.socket.close();
    }


    private MoshPacket newPacket(byte[] bytes) {
        short outgoingTimestampReply = -1;

        long now = Timestamp.timestamp();

        if (now - savedTimestampReceivedAt < 1000) {
            outgoingTimestampReply = (short) (savedTimestamp + (short) (now - savedTimestampReceivedAt));
            savedTimestamp = -1;
            savedTimestampReceivedAt = -1;
        }

        return new MoshPacket(
                new String(bytes, StandardCharsets.UTF_8),
                MoshPacket.Direction.TO_SERVER,
                Timestamp.timestamp16(),
                outgoingTimestampReply
        );
    }

}
