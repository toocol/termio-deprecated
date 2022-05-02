package com.toocol.ssh.core.mosh.core;

import com.toocol.ssh.core.term.core.Printer;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.datagram.DatagramPacket;
import io.vertx.core.datagram.DatagramSocket;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Equivalent to network.h/network.cc
 *
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/28 22:17
 * @version: 0.0.1
 */
public class MoshOutputStream extends PipedOutputStream {
    @SuppressWarnings("all")
    public static class Transport {
        final String serverHost;
        final int port;
        final String key;

        public Transport(String serverHost, int port, String key) {
            this.serverHost = serverHost;
            this.port = port;
            this.key = key;
        }
    }

    final DatagramSocket socket;
    final Transport transport;
    final byte[] buff = new byte[1];

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
    }

    public void sendPacket(byte[] bytes) {
        MoshPacket packet = newPacket(bytes);
        socket.send(Buffer.buffer(packet.getBytes(transport.key)), transport.port, transport.serverHost);
    }

    public void receivePacket(DatagramPacket datagramPacket) {
        try {
            byte[] bytes = datagramPacket.data().getBytes();
            this.write(bytes, 0, bytes.length);
        } catch (IOException e) {
            Printer.printErr(e.getMessage());
        }
    }

    @Override
    public void write(@Nonnull byte[] bytes) throws IOException {
        sendPacket(bytes);
    }

    @Override
    public void write(int i) throws IOException {
        this.buff[0] = (byte) i;
        sendPacket(this.buff);
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
