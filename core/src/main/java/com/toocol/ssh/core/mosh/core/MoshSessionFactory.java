package com.toocol.ssh.core.mosh.core;

import com.toocol.ssh.core.cache.SshSessionCache;
import com.toocol.ssh.core.ssh.core.SshSessionFactory;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

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
     * @param host host address
     * @return mosh key
     */
    private String sshTouch(String host) {
        return null;
    }

    /**
     * creating udp connection to mosh-server and starting data transport;
     */
    private void tryMosh(String host, int port, String key) {
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
