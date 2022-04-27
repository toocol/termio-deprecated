package com.toocol.ssh.core.mosh.core;

import com.jcraft.jsch.ChannelShell;
import com.toocol.ssh.core.auth.core.SshCredential;
import com.toocol.ssh.core.cache.CredentialCache;
import com.toocol.ssh.core.cache.SshSessionCache;
import com.toocol.ssh.core.shell.core.Shell;
import com.toocol.ssh.core.ssh.core.SshSessionFactory;
import com.toocol.ssh.utilities.utils.Tuple2;

import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/25 19:47
 */
public class MoshSessionFactory {
    public static final String LOCAL_IP_ADDRESS = "172.16.30.10";
    public static final int LOCAL_PORT = 60001;
    public static final int MAX_BYTES = 2048;

    private final SshSessionCache sshSessionCache = SshSessionCache.getInstance();
    private final SshSessionFactory sshSessionFactory = SshSessionFactory.factory();

    /**
     * touch mosh-server by ssh;
     *
     * @param index the index of ssh credential
     * @return mosh server port / key
     */
    public Tuple2<Integer, String> sshTouch(int index) {
        SshCredential credential = CredentialCache.getCredential(index);
        long sessionId = sshSessionCache.containSession(credential.getHost());
        if (sessionId != 0) {
            sessionId = sshSessionFactory.invokeSession(sessionId, credential, null);
        } else {
            sessionId = sshSessionFactory.createSession(credential, null);
        }

        ChannelShell channelShell = sshSessionCache.getChannelShell(sessionId);
        Shell shell = sshSessionCache.getShell(sessionId);

        Tuple2<Integer, String> portKey = new Tuple2<>();
        CountDownLatch latch = new CountDownLatch(1);
        try {
            InputStream inputStream = channelShell.getInputStream();

            new Thread(() -> {
                byte[] tmp = new byte[1024];
                while (true) {
                    try {
                        while (inputStream.available() > 0) {
                            int i = inputStream.read(tmp, 0, 1024);
                            if (i < 0) {
                                break;
                            }
                            String inputStr = new String(tmp, 0, i);

                            for (String line : inputStr.split("\r\n")) {
                                if (line.contains("MOSH CONNECT")) {
                                    String[] split = line.split(" ");
                                    portKey.first(Integer.parseInt(split[2])).second(split[3]);
                                    latch.countDown();
                                }
                            }
                        }
                    } catch (Exception e) {
                        latch.countDown();
                    }

                }
            }).start();

            new Thread(() -> shell.writeAndFlush("mosh-server\n".getBytes(StandardCharsets.UTF_8))).start();

            boolean suc = latch.await(20, TimeUnit.SECONDS);
            if (!suc) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }

        return portKey;
    }

    /**
     * creating udp connection to mosh-server and starting data transport;
     */
    public void tryMosh(String host, int port, String key) {
        DatagramSocket socket = null;
        try {
            InetAddress address = InetAddress.getByName(LOCAL_IP_ADDRESS);
            socket = new DatagramSocket(LOCAL_PORT, address);
            byte[] receiveBytes = new byte[MAX_BYTES];
            DatagramPacket receivePacket = new DatagramPacket(receiveBytes, receiveBytes.length);

            byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
            DatagramPacket responsePacket = new DatagramPacket(bytes, bytes.length, InetAddress.getByName(host), port);
            socket.send(responsePacket);

            while (true) {
                try {
                    socket.receive(receivePacket);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                String receiveMsg = new String(receivePacket.getData(), 0, receivePacket.getLength());
                InetAddress clientAddress = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();

                String response = "response： " + receiveMsg;
                byte[] responseBuf = response.getBytes();
                responsePacket = new DatagramPacket(responseBuf, responseBuf.length, clientAddress, clientPort);

                try {

                    socket.send(responsePacket);

                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }
}
