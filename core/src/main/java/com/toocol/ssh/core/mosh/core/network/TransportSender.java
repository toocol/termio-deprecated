package com.toocol.ssh.core.mosh.core.network;

import com.google.protobuf.ByteString;
import com.toocol.ssh.utilities.utils.Timestamp;
import com.toocol.ssh.core.mosh.core.crypto.Crypto;
import com.toocol.ssh.core.mosh.core.proto.InstructionPB;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import static com.toocol.ssh.core.mosh.core.network.NetworkConstants.*;

/**
 * transportsender.h/transportsender-impl.h
 * <p>
 * TODO: See: transportsender-impl.h :: 153
 * TODO: See: transportsender-impl.h :: 239
 * TODO: See: transportsender-impl.h :: 320
 *
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/5/8 21:33
 * @version: 0.0.1
 */
public final class TransportSender {

    private long ackNum;
    private int shutdownTries;

    private final List<TimestampedState> sentStatesType = new ArrayList<>();
    private final TransportFragment.Fragmenter fragmenter;

    private TimestampedState assumedReceiverState;

    public TransportSender() {
        this.fragmenter = new TransportFragment.Fragmenter();
    }

    private void sendEmptyAck() {
        long now = Timestamp.timestamp();
    }

    private void sendInFragments(String diff, long newNum) {
        InstructionPB.Instruction.Builder builder = InstructionPB.Instruction.newBuilder();
        builder.setProtocolVersion(MOSH_PROTOCOL_VERSION);
        builder.setOldNum(assumedReceiverState.num);
        builder.setNewNum(newNum);
        builder.setAckNum(ackNum);
        builder.setThrowawayNum(sentStatesType.get(0).num);
        builder.setDiff(ByteString.copyFromUtf8(diff));
        builder.setChaff(ByteString.copyFromUtf8(makeChaff()));
        InstructionPB.Instruction inst = builder.build();

        if (newNum == -1) {
            shutdownTries++;
        }

        Queue<TransportFragment.Fragment> fragments = fragmenter.makeFragments(inst,
                DEFAULT_SEND_MTU - MoshPacket.ADDED_BYTES - Crypto.Session.ADDED_BYTES);
        while (!fragments.isEmpty()) {
            TransportFragment.Fragment fragment = fragments.poll();
        }

    }

    private void calculateTimers() {
        long now = Timestamp.timestamp();

        updateAssumedReceiverState();
    }

    private void updateAssumedReceiverState() {
        long now = Timestamp.timestamp();

        assumedReceiverState = sentStatesType.get(0);

        TimestampedState state = sentStatesType.get(1);
        if (state == null) {
            return;
        }
        if (now - state.timestamp < timeout() + ACK_DELAY) {
            assumedReceiverState = state;
        }
    }

    private void rationalizeStates() {

    }

    private String makeChaff() {
        int chaffMax = 16;
        int chaffLen = PRNG.uint8() % (chaffMax + 1);

        byte[] chaff = new byte[chaffMax];
        PRNG.fill(chaff, chaffLen);
        return new String(chaff, 0, chaffLen);
    }

    private int sendInterval() {
        int sendInterval = (int) Math.ceil(SRIT / 2.0);
        if (sendInterval < SEND_INTERVAL_MIN) {
            sendInterval = SEND_INTERVAL_MIN;
        } else if (sendInterval > SEND_INTERVAL_MAX) {
            sendInterval = SEND_INTERVAL_MAX;
        }
        return sendInterval;
    }

    private long timeout() {
        long rto = (long) Math.ceil(SRIT + 4 * RTTVAR);
        if (rto < MIN_RTO) {
            rto = MIN_RTO;
        } else if (rto > MAX_RTO) {
            rto = MAX_RTO;
        }
        return rto;
    }
}
