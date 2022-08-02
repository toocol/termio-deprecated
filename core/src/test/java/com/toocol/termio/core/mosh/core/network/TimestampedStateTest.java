package com.toocol.termio.core.mosh.core.network;

import com.toocol.termio.core.mosh.core.statesnyc.UserEvent;
import com.toocol.termio.core.mosh.core.statesnyc.UserStream;
import com.toocol.termio.utilities.utils.Timestamp;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import static com.toocol.termio.core.mosh.core.network.NetworkConstants.ACK_DELAY;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/6/30 14:20
 */
class TimestampedStateTest {
    private final UserStream currentState = new UserStream();
    private final List<TimestampedState<UserStream>> sentStates = new ArrayList<>();
    private TimestampedState<UserStream> assumedReceiverState;

    @Test
    void testSubtract() {
        this.sentStates.add(new TimestampedState<>(Timestamp.timestamp(), 0, currentState.copy()));
        this.assumedReceiverState = this.sentStates.get(0);

        int ackNum = 1;
        for (int i = 0; i < 10000; i++) {
            currentState.pushBack(new UserEvent.Resize(100, 100));
            updateAssumedReceiverState();
            rationalizeStates();

            byte[] bytes = currentState.diffFrom(assumedReceiverState.state);
            assert bytes != null && bytes.length > 0;

            long newNum;
            TimestampedState<UserStream> back = sentStates.get(sentStates.size() - 1);
            if (currentState.equals(back.state)) {
                newNum = back.num;
            } else {
                newNum = back.num + 1;
            }
            addSentState(Timestamp.timestamp(), newNum, currentState);
            assert newNum == back.num + 1;

            /* suppose we have received an ack packet from mosh-server */
            processAcknowledgmentThrough(ackNum++);

        }
    }

    private void addSentState(long theTimestamp, long num, UserStream state) {
        sentStates.add(new TimestampedState<>(theTimestamp, num, state.copy()));
        if (sentStates.size() > 32) {
            ListIterator<TimestampedState<UserStream>> iterator = sentStates.listIterator(sentStates.size() - 1);
            for (int i = 0; i < 15; i++) {
                if (iterator.hasPrevious()) {
                    iterator.previous();
                }
            }
            iterator.remove();
        }
    }

    private void updateAssumedReceiverState() {
        long now = Timestamp.timestamp();

        assumedReceiverState = sentStates.get(sentStates.size() - 1);

        for (int i = 1; i < sentStates.size(); i++) {
            TimestampedState<UserStream> state = sentStates.get(i);
            if (state == null) {
                return;
            }
            assert now >= state.timestamp;
            if (now - state.timestamp < 1000 + ACK_DELAY) {
                assumedReceiverState = state;
            } else {
                return;
            }
        }
    }

    private void rationalizeStates() {
        UserStream knownReceiverState = sentStates.get(0).state;
        currentState.subtract(knownReceiverState);

        ListIterator<TimestampedState<UserStream>> iterator = sentStates.listIterator(sentStates.size());
        while (iterator.hasPrevious()) {
            iterator.previous().state.subtract(knownReceiverState);
        }
    }

    public void processAcknowledgmentThrough(long ackNum) {
        Iterator<TimestampedState<UserStream>> iterator = sentStates.iterator();
        TimestampedState<UserStream> i;
        boolean find = false;
        while (iterator.hasNext()) {
            i = iterator.next();
            if (i.num == ackNum) {
                find = true;
                break;
            }
        }

        if (find) {
            iterator = sentStates.iterator();
            while (iterator.hasNext()) {
                i = iterator.next();
                if (i.num < ackNum) {
                    iterator.remove();
                }
            }
        }
    }
}