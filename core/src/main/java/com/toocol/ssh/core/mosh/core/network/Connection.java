package com.toocol.ssh.core.mosh.core.network;

import com.toocol.ssh.core.mosh.core.crypto.Crypto;
import com.toocol.ssh.utilities.utils.Timestamp;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.datagram.DatagramSocket;

import static com.toocol.ssh.core.mosh.core.network.NetworkConstants.*;

/**
 * Equivalent to network.h/network.cc/
 *
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/5/11 22:54
 * @version: 0.0.1
 */
public final class Connection {

    private short savedTimestamp;
    private long savedTimestampReceivedAt;
    private long expectedReceiverSeq;

    private long lastHeard;
    private long lastPortChoice;
    private long lastRoundtripSuccess;

    final Transport.Addr addr;

    final DatagramSocket socket;

    final Crypto.Session encryptSession;
    final Crypto.Session decryptSession;

    public Connection(Transport.Addr addr, DatagramSocket socket) {
        this.addr = addr;
        this.socket = socket;
        Crypto.Base64Key key = new Crypto.Base64Key(addr.key());
        this.encryptSession = new Crypto.Session(key);
        this.decryptSession = new Crypto.Session(key);
    }

    public void send(byte[] msg) {
        MoshPacket packet = newPacket(msg);
        socket.send(Buffer.buffer(encryptSession.encrypt(packet.toMessage())), addr.port(), addr.serverHost(), result -> {

        });
    }

    public byte[] recvOne(byte[] recv) {
        Crypto.Message decryptMessage = decryptSession.decrypt(recv, recv.length);
        MoshPacket packet = new MoshPacket(decryptMessage);
        expectedReceiverSeq = packet.getSeq() + 1;
        if (packet.getTimestamp() != -1) {
            savedTimestamp = packet.getTimestamp();
            savedTimestampReceivedAt = Timestamp.timestamp();
        }
        return packet.getPayload();
    }

    public long timeout() {
        long rto = (long) Math.ceil(SRIT + 4 * RTTVAR);
        if (rto < MIN_RTO) {
            rto = MIN_RTO;
        } else if (rto > MAX_RTO) {
            rto = MAX_RTO;
        }
        return rto;
    }

    private MoshPacket newPacket(byte[] msg) {
        short outgoingTimestampReply = -1;

        long now = Timestamp.timestamp();

        if (now - savedTimestampReceivedAt < 1000) {
            outgoingTimestampReply = (short) (savedTimestamp + (short) (now - savedTimestampReceivedAt));
            savedTimestamp = -1;
            savedTimestampReceivedAt = -1;
        }

        return new MoshPacket(
                msg,
                MoshPacket.Direction.TO_SERVER,
                Timestamp.timestamp16(),
                outgoingTimestampReply
        );
    }
}
