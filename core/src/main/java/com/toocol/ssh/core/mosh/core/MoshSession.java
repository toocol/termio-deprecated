package com.toocol.ssh.core.mosh.core;

import com.toocol.ssh.utilities.utils.ICastable;
import io.vertx.core.Vertx;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.datagram.DatagramSocketOptions;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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

    private boolean connected = false;

    public MoshSession(Vertx vertx, String host, int port, String key) {
        this.vertx = vertx;
        this.io = new IO();
        this.transport = new MoshOutputStream.Transport(host, port, key);
    }

    public MoshSession connect() {
        try {
            DatagramSocket socket = vertx.createDatagramSocket(new DatagramSocketOptions());
            this.io.inputStream = new MoshInputStream();
            this.io.outputStream = new MoshOutputStream(cast(this.io.inputStream), socket, transport);
            connected = true;
        } catch (IOException e) {
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

}
