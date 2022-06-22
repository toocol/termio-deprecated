package com.toocol.ssh.core.mosh.core.network;

import com.google.protobuf.ByteString;
import com.toocol.ssh.core.mosh.core.crypto.Crypto;
import com.toocol.ssh.core.mosh.core.crypto.Prng;
import com.toocol.ssh.core.mosh.core.proto.InstructionPB;
import com.toocol.ssh.core.mosh.core.statesnyc.State;
import com.toocol.ssh.utilities.utils.Timestamp;
import io.vertx.core.datagram.DatagramSocket;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Queue;

import static com.toocol.ssh.core.mosh.core.network.NetworkConstants.*;

/**
 * transportsender.h/transportsender-impl.h
 *
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/5/8 21:33
 * @version: 0.0.1
 */
public final class TransportSender<MyState extends State<MyState>> {

    private final MyState currentState;
    private final List<TimestampedState<MyState>> sentStates = new ArrayList<>();
    private final TransportFragment.Fragmenter fragmenter = new TransportFragment.Fragmenter();
    private final Connection connection;

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

    public TransportSender(MyState initialState, Transport.Addr addr, DatagramSocket socket) {
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

        this.connection = new Connection(addr, socket);
    }

    public void tick() {
        calculateTimers();

        long now = Timestamp.timestamp();

        if (now < nextAckTime && now < nextSendTime) {
            return;
        }

        byte[] diff = currentState.diffFrom(assumedReceiverState.state);

        if (diff == null || diff.length == 0) {
            if (now >= nextAckTime) {
                sendEmptyAck();
                minDelayClock = -1;
            }
            if (now >= nextSendTime) {
                nextSendTime = -1;
                minDelayClock = -1;
            }
        } else {
            sendToReceiver(diff);
            minDelayClock = -1;
        }
    }

    public void sendToReceiver(byte[] diff) {
        minDelayClock = -1;
        long newNum;
        TimestampedState<MyState> back = sentStates.get(sentStates.size() - 1);
        if (currentState.equals(back.state)) {
            newNum = back.num;
        } else {
            newNum = back.num + 1;
        }

        if (newNum == back.num) {
            back.timestamp = Timestamp.timestamp();
        } else {
            addSentState(Timestamp.timestamp(), newNum, currentState);
        }

        sendInFragments(diff, newNum);

        assumedReceiverState = sentStates.get(sentStates.size() - 1);
        nextAckTime = Timestamp.timestamp() + ACK_INTERVAL;
        nextSendTime = -1;
    }

    public void sendEmptyAck() {
        long now = Timestamp.timestamp();

        assert now >= nextAckTime;

        long new_num = sentStates.get(sentStates.size() - 1).num + 1;

        addSentState(now, new_num, currentState);
        sendInFragments(new byte[0], new_num);

        nextAckTime = now + ACK_INTERVAL;
        nextSendTime = -1;
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

    private void sendInFragments(byte[] diff, long newNum) {
        InstructionPB.Instruction.Builder builder = InstructionPB.Instruction.newBuilder();
        builder.setProtocolVersion(MOSH_PROTOCOL_VERSION);
        builder.setOldNum(assumedReceiverState.num);
        builder.setNewNum(newNum);
        builder.setAckNum(ackNum);
        builder.setThrowawayNum(sentStates.get(0).num);
        builder.setDiff(ByteString.copyFrom(diff));
        builder.setChaff(ByteString.copyFrom(makeChaff()));
        InstructionPB.Instruction inst = builder.build();

        if (newNum == -1) {
            shutdownTries++;
        }

        Queue<TransportFragment.Fragment> fragments = fragmenter.makeFragments(inst,
                DEFAULT_SEND_MTU - MoshPacket.ADDED_BYTES - Crypto.Session.ADDED_BYTES);
        while (!fragments.isEmpty()) {

            TransportFragment.Fragment fragment = fragments.poll();
            connection.send(fragment.toBytes());

        }

        pendingDataAck = false;
    }

    private byte[] makeChaff() {
        int chaffMax = 16;
        int chaffLen = Prng.uint8() % (chaffMax + 1);

        byte[] chaff = new byte[chaffLen];
        Prng.fill(chaff, chaffLen);
        return chaff;
    }

    private void calculateTimers() {
        long now = Timestamp.timestamp();

        updateAssumedReceiverState();

        rationalizeStates();

        if (pendingDataAck && (nextAckTime > now + ACK_DELAY)) {
            nextAckTime = now + ACK_DELAY;
        }

        if (!currentState.equals(sentStates.get(sentStates.size() - 1).state)) {
            if (minDelayClock == -1) {
                minDelayClock = now;
            }

            nextSendTime = Math.max(minDelayClock + sendMinDelay,
                    sentStates.get(sentStates.size()- 1).timestamp + sendInterval());
        } else if (!currentState.equals(assumedReceiverState.state)
                && lastHeard + ACTIVE_RETRY_TIMEOUT > now) {
            nextSendTime = sentStates.get(sentStates.size()- 1).timestamp + sendInterval();
            if (minDelayClock != -1) {
                nextSendTime = Math.max(nextSendTime, minDelayClock + sendMinDelay);
            }
        } else if (!currentState.equals(sentStates.get(0).state)
                && lastHeard + ACTIVE_RETRY_TIMEOUT > now) {
            nextSendTime = sentStates.get(sentStates.size()- 1).timestamp + connection.timeout() + ACK_DELAY;
        } else {
            nextSendTime = -1;
        }
    }

    private void updateAssumedReceiverState() {
        long now = Timestamp.timestamp();

        assumedReceiverState = sentStates.get(0);

        for (int i = 1; i < sentStates.size(); i++) {
            TimestampedState<MyState> state = sentStates.get(i);
            if (state == null) {
                return;
            }
            if (now - state.timestamp < connection.timeout() + ACK_DELAY) {
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

    private int sendInterval() {
        int sendInterval = (int) Math.ceil(SRIT / 2.0);
        if (sendInterval < SEND_INTERVAL_MIN) {
            sendInterval = SEND_INTERVAL_MIN;
        } else if (sendInterval > SEND_INTERVAL_MAX) {
            sendInterval = SEND_INTERVAL_MAX;
        }
        return sendInterval;
    }

    public MyState getCurrentState() {
        return currentState;
    }

    public Connection getConnection() {
        return connection;
    }
}
