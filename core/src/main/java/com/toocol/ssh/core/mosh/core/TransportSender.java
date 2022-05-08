package com.toocol.ssh.core.mosh.core;

import com.google.protobuf.ByteString;
import com.toocol.ssh.core.mosh.core.proto.InstructionPB;

import java.util.ArrayList;
import java.util.List;

/**
 * transportsender.h/transportsender-impl.h
 *
 * TODO: See: transportsender-impl.h :: 153
 * TODO: See: transportsender-impl.h :: 239
 * TODO: See: transportsender-impl.h :: 320
 *
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/5/8 21:33
 * @version: 0.0.1
 */
public class TransportSender {
    private static final int MOSH_PROTOCOL_VERSION = 2;

    private static final double SRIT = 1000;
    private static final double RTTVAR = 500;

    private static final long MIN_RTO = 50; /* ms */
    private static final long MAX_RTO = 1000; /* ms */

    private static final int SEND_INTERVAL_MIN = 20; /* ms between frames */
    private static final int SEND_INTERVAL_MAX = 250; /* ms between frames */
    private static final int ACK_INTERVAL = 3000; /* ms between empty acks */
    private static final int ACK_DELAY = 100; /* ms before delayed ack */
    private static final int SHUTDOWN_RETRIES = 16; /* number of shutdown packets to send before giving up */
    private static final int ACTIVE_RETRY_TIMEOUT = 10000; /* attempt to resend at frame rate */

    private long ackNum;

    private final List<TimestampedState> sentStatesType = new ArrayList<>();
    private TimestampedState assumedReceiverState;


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
