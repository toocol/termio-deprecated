package com.toocol.ssh.core.mosh.core;

import com.toocol.ssh.core.term.core.Printer;
import com.toocol.ssh.core.term.core.Term;
import com.toocol.ssh.utilities.utils.ICastable;
import io.vertx.core.Vertx;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.datagram.DatagramSocketOptions;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/25 19:58
 */
public class MoshSession implements ICastable {

    private static class IO {
        private OutputStream outputStream;
        private InputStream inputStream;
    }


    private final IO io;
    private final Vertx vertx;
    private final MoshOutputStream.Transport transport;
    private final long sessionId;

    private boolean connected = false;
    private boolean listened = false;

    public MoshSession(Vertx vertx, long sessionId, String host, int port, String key) {
        this.vertx = vertx;
        this.io = new IO();
        this.transport = new MoshOutputStream.Transport(host, port, key);
        this.sessionId = sessionId;
    }

    public MoshSession connect() {
        try {
            DatagramSocket socket = vertx.createDatagramSocket(new DatagramSocketOptions());
            this.io.inputStream = new MoshInputStream();
            this.io.outputStream = new MoshOutputStream(cast(this.io.inputStream), socket, transport);

            CountDownLatch bindLatch = new CountDownLatch(1);
            Term term = Term.getInstance();
            term.printDisplay("mosh start to listened local port: " + transport.port);
            socket.listen(transport.port, "127.0.0.1", result -> {
                if (result.succeeded()) {
                    socket.handler(packet -> {
                        try {
                            byte[] bytes = packet.data().getBytes();
                            this.io.outputStream.write(bytes, 0, bytes.length);
                        } catch (IOException e) {
                            Printer.printErr(e.getMessage());
                        }
                    });
                    term.printDisplay("Mosh success to listened local port: " + transport.port);
                    listened = true;
                } else {
                    term.printDisplay("Mosh fail to listened local port: " + transport.port);
                    listened = false;
                }
                bindLatch.countDown();
            });
            boolean suc = bindLatch.await(10, TimeUnit.SECONDS);
            if (!suc || !listened) {
                close();
                return this;
            }
            connected = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public InputStream getInputStream() throws IOException {
        if (!connected) {
            throw new IOException("Mosh session is not connected.");
        }
        return this.io.inputStream;
    }

    public OutputStream getOutputStream() throws IOException {
        if (!connected) {
            throw new IOException("Mosh session is not connected.");
        }
        return this.io.outputStream;
    }

    public void close() {
        try {
            this.connected = false;
            this.io.outputStream.close();
            this.io.inputStream.close();
        } catch (Exception e) {
            // do nothing
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public long getSessionId() {
        return sessionId;
    }
}
