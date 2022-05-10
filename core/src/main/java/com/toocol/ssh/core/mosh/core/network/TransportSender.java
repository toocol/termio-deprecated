package com.toocol.ssh.core.mosh.core.network;

import com.google.protobuf.ByteString;
import com.toocol.ssh.core.mosh.core.crypto.Crypto;
import com.toocol.ssh.core.mosh.core.proto.InstructionPB;
import com.toocol.ssh.core.mosh.core.statesnyc.State;
import com.toocol.ssh.utilities.utils.Timestamp;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
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
public final class TransportSender<MyState extends State> {

    private final MyState currentState;
    private final List<TimestampedState<MyState>> sentStates = new ArrayList<>();
    private final TransportFragment.Fragmenter fragmenter = new TransportFragment.Fragmenter();

    private TimestampedState<MyState> assumedReceiverState;

    /* timing state */
    private long nextAckTime;
    private long nextSendTime;

    private int verbose;
    private int shutdownTries;
    private long shutdownStart;

    /* information about receiver state */
    private long ackNum;
    private boolean pendingDataAck;

    private int sendMinDelay;
    private long lastHeard;

    private long minDelayClock;

    public TransportSender(MyState initialState) {
        this.currentState = initialState;
        this.sentStates.add(new TimestampedState<>(Timestamp.timestamp(), 0, initialState));
        this.assumedReceiverState = sentStates.get(0);
        this.nextAckTime = Timestamp.timestamp();
        this.nextSendTime = Timestamp.timestamp();
        this.verbose = 0;
        this.shutdownTries = 0;
        this.shutdownStart = -1;
        this.ackNum = 0;
        this.pendingDataAck = false;
        this.sendMinDelay = 8;
        this.lastHeard = 0;
        this.minDelayClock = -1;
    }

    public void sendToReceiver(String diff) {
        long newNum;
    }

    public void sendEmptyAck() {
        long now = Timestamp.timestamp();
    }

    private void addSentState(long theTimestamp, long num, MyState state) {
        sentStates.add(new TimestampedState<>(theTimestamp, num, state));
        if (sentStates.size() > 32) {
            ListIterator<TimestampedState<MyState>> iterator = sentStates.listIterator(sentStates.size() - 1);
            for (int i = 0; i < 16; i++) {
                if (iterator.hasPrevious()) {
                    iterator.previous();
                }
            }
            iterator.remove();
        }
    }

    private void sendInFragments(String diff, long newNum) {
        InstructionPB.Instruction.Builder builder = InstructionPB.Instruction.newBuilder();
        builder.setProtocolVersion(MOSH_PROTOCOL_VERSION);
        builder.setOldNum(assumedReceiverState.num);
        builder.setNewNum(newNum);
        builder.setAckNum(ackNum);
        builder.setThrowawayNum(sentStates.get(0).num);
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

        assumedReceiverState = sentStates.get(0);

        for (int i = 1; i < sentStates.size(); i++) {
            TimestampedState<MyState> state = sentStates.get(i);
            if (state == null) {
                return;
            }
            if (now - state.timestamp < timeout() + ACK_DELAY) {
                assumedReceiverState = state;
            } else {
                return;
            }
        }

    }

    private void rationalizeStates() {
        MyState knownReceiverState = sentStates.get(0).state;
        currentState.subtract(knownReceiverState);

        for (TimestampedState<MyState> sentState : sentStates) {
            sentState.state.subtract(knownReceiverState);
        }
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
