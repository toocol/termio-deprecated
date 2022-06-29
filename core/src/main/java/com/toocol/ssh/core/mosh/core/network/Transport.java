package com.toocol.ssh.core.mosh.core.network;

import com.toocol.ssh.core.mosh.core.proto.InstructionPB;
import com.toocol.ssh.core.mosh.core.statesnyc.RemoteState;
import com.toocol.ssh.core.mosh.core.statesnyc.UserEvent;
import com.toocol.ssh.core.mosh.core.statesnyc.UserStream;
import com.toocol.ssh.core.term.core.Term;
import com.toocol.ssh.utilities.execeptions.NetworkException;
import com.toocol.ssh.utilities.utils.Timestamp;
import io.vertx.core.datagram.DatagramSocket;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

public final class Transport {

    public record Addr(String serverHost, int port, String key) {
        public String serverHost() {
            return serverHost;
        }

        public int port() {
            return port;
        }

        public String key() {
            return key;
        }
    }

    public final Addr addr;
    private final TransportFragment.FragmentAssembly fragments = new TransportFragment.FragmentAssembly();

    private TransportSender<UserStream> sender;
    private final List<TimestampedState<RemoteState>> receiveStates = new ArrayList<>();
    private final Queue<InstructionPB.Instruction> instQueue = new ConcurrentLinkedDeque<>();

    public Transport(String serverHost, int port, String key) {
        this.addr = new Addr(serverHost, port, key);
        receiveStates.add(new TimestampedState<>(Timestamp.timestamp(), 0, new RemoteState()));
    }

    public void connect(DatagramSocket socket) {
        this.sender = new TransportSender<>(new UserStream(), this.addr, socket);
        this.sender.setSendDelay(1);
        // tell the server the size of the terminal
        pushBackEvent(new UserEvent.Resize(Term.WIDTH, Term.HEIGHT));
    }

    public byte[] recvAndStash(byte[] recv) {
        byte[] bytes = sender.getConnection().recvOne(recv);
        TransportFragment.Fragment fragment = new TransportFragment.Fragment(bytes);
        if (fragments.addFragment(fragment)) {
            InstructionPB.Instruction inst = fragments.getAssembly();
            /* 1. make sure we don't already have the new state */
            for (TimestampedState<RemoteState> state : receiveStates) {
                if (inst.getNewNum() == state.num) {
                    return null;
                }
            }
            /* 2. make sure we do have the old state */
            boolean found = false;
            for (TimestampedState<RemoteState> state : receiveStates) {
                if (inst.getOldNum() == state.num) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return null;
            }

            TimestampedState<RemoteState> newState = new TimestampedState<>();
            newState.timestamp = Timestamp.timestamp();
            newState.num = inst.getNewNum();

            for (int i = 0; i < receiveStates.size(); i++) {
                TimestampedState<RemoteState> state = receiveStates.get(i);
                if (state.num > newState.num) {
                    receiveStates.add(i, newState);
                }
            }

            instQueue.offer(inst);
            return inst.getDiff().toByteArray();
        }

        return null;
    }

    public void tick() {
        recv();
        sender.tick();
    }

    @SuppressWarnings("all")
    public void pushBackEvent(UserEvent event) {
        // Ensure that there is only one UserEvent to be sent at a time
        UserStream currentState = sender.getCurrentState();
        while (!sender.getLastSentStates().equals(currentState)) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                // do nothing
            }
        }
        currentState.pushBack(event);
    }

    private void recv() {
        // Ensure that there is only one packet to be process at a time
        if (!instQueue.isEmpty()) {
            InstructionPB.Instruction inst = instQueue.poll();
            if (inst.getProtocolVersion() != NetworkConstants.MOSH_PROTOCOL_VERSION) {
                throw new NetworkException("mosh protocol version mismatch");
            }
            sender.processAcknowledgmentThrough(inst.getAckNum());

            processThrowawayUntil(inst.getThrowawayNum());

            sender.setAckNum(receiveStates.get(receiveStates.size() - 1).num);
            if (StringUtils.isNotEmpty(inst.getDiff().toString())) {
                sender.setDataAck();
            }
        }
    }

    private void processThrowawayUntil(long throwawayNum) {
        receiveStates.removeIf(next -> next.num < throwawayNum);
        assert receiveStates.size() > 0;
    }

}