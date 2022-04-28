package com.toocol.ssh.core.mosh.core;

import com.toocol.ssh.core.term.core.Printer;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.datagram.DatagramSocket;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/28 22:17
 * @version: 0.0.1
 */
public class MoshOutputStream extends PipedOutputStream {
    public record Transport(String serverHost, int port, String key) {
    }

    private final DatagramSocket socket;
    private final Transport transport;
    private final byte[] buff = new byte[1];

    private boolean listened = false;

    public MoshOutputStream(PipedInputStream in, DatagramSocket socket, Transport transport) throws IOException {
        super(in);
        this.socket = socket;
        this.transport = transport;
        this.socket.listen(transport.port, "127.0.0.1", result -> {
            if (result.succeeded()) {
                socket.handler(packet -> {
                    try {
                        byte[] bytes = packet.data().getBytes();
                        this.write(bytes, 0, bytes.length);
                    } catch (IOException e) {
                        Printer.printErr(e.getMessage());
                    }
                });
                listened = true;
            } else {
                listened = false;
            }
        });
    }

    @Override
    public void write(@Nonnull byte[] bytes) throws IOException {
        if (!listened) {
            throw new IOException("Bind local mosh port failed.");
        }
        socket.send(Buffer.buffer(bytes), transport.port, transport.serverHost);
    }

    @Override
    public void write(int i) throws IOException {
        if (!listened) {
            throw new IOException("Bind local mosh port failed.");
        }
        this.buff[0] = (byte) i;
        socket.send(Buffer.buffer(this.buff), transport.port, transport.serverHost);
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.socket.close();
    }
}
