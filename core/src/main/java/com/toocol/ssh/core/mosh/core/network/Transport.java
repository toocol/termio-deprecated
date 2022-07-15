package com.toocol.ssh.core.mosh.core.network;

import com.toocol.ssh.core.mosh.core.proto.InstructionPB;
import com.toocol.ssh.core.mosh.core.statesnyc.CompleteTerminal;
import com.toocol.ssh.core.mosh.core.statesnyc.State;
import com.toocol.ssh.core.mosh.core.statesnyc.UserEvent;
import com.toocol.ssh.core.mosh.core.statesnyc.UserStream;
import com.toocol.ssh.core.term.core.Term;
import com.toocol.ssh.utilities.functional.DiffThread;
import com.toocol.ssh.utilities.execeptions.NetworkException;
import com.toocol.ssh.utilities.log.Loggable;
import com.toocol.ssh.utilities.utils.Timestamp;
import io.vertx.core.datagram.DatagramPacket;
import io.vertx.core.datagram.DatagramSocket;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

@SuppressWarnings("all")
public final class Transport<RemoteState extends State> implements Loggable {
    public final Addr addr;
    private final RemoteState state;
    private final TransportFragment.Pool receivePool = new TransportFragment.Pool();
    private final TransportFragment.FragmentAssembly fragments = new TransportFragment.FragmentAssembly(receivePool);
    private final List<TimestampedState<RemoteState>> receiveStates = new ArrayList<>();
    private final Queue<InstructionPB.Instruction> instQueue = new ConcurrentLinkedDeque<>();
    private TransportSender<UserStream> sender;
    private Connection connection;

    public Transport(String serverHost, int port, String key, RemoteState initalRemoteState) {
        this.addr = new Addr(serverHost, port, key);
        this.state = initalRemoteState;
        receiveStates.add(new TimestampedState<>(Timestamp.timestamp(), 0, initalRemoteState));
    }

    public void connect(DatagramSocket socket) {
        this.connection = new Connection(this.addr, socket);
        this.sender = new TransportSender<>(new UserStream(), this.connection);
        this.sender.setSendDelay(1);
        this.receivePool.init();
        // tell the server the size of the terminal
        pushBackEvent(new UserEvent.Resize(Term.WIDTH, Term.HEIGHT));
    }

    @DiffThread
    public void receivePacket(DatagramPacket datagramPacket) {
        byte[] bytes = this.connection.recvOne(datagramPacket.data().getBytes());
        TransportFragment.Fragment fragment = receivePool.getObject().setData(bytes);
        if (fragments.addFragment(fragment)) {
            InstructionPB.Instruction inst = fragments.getAssembly();
            if (inst.getProtocolVersion() != NetworkConstants.MOSH_PROTOCOL_VERSION) {
                throw new NetworkException("mosh protocol version mismatch");
            }

            instQueue.offer(inst);
        }
    }

    @DiffThread
    public void pushBackEvent(UserEvent event) {
        sender.pushBackEvent(event);
    }

    @DiffThread
    public void tick() {
        recv();
        sender.tick();
    }

    public Queue<byte[]> getOutputQueue() {
        return ((CompleteTerminal) state).getOutputQueue();
    }

    private void recv() {
        while (!instQueue.isEmpty()) {
            InstructionPB.Instruction inst = instQueue.poll();

            sender.processAcknowledgmentThrough(inst.getAckNum());

            /* 1. make sure we don't already have the new state */
            for (TimestampedState<RemoteState> state : receiveStates) {
                if (inst.getNewNum() == state.num) {
                    return;
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
                return;
            }

            processThrowawayUntil(inst.getThrowawayNum());

            TimestampedState<RemoteState> newState = new TimestampedState<>();
            newState.state = state;
            newState.timestamp = Timestamp.timestamp();
            newState.num = inst.getNewNum();

            byte[] diff = inst.getDiff().toByteArray();
            boolean dataAcked = false;
            if (diff != null && diff.length > 0) {
                info("Receive packet oldNum = {}, newNum = {}, ackNum = {}, throwawayNum = {}, diff = {}",
                        inst.getOldNum(), inst.getNewNum(), inst.getAckNum(), inst.getThrowawayNum(), inst.getDiff().toStringUtf8());
                if (inst.getAckNum() == 2) {
                    return;
                }
                newState.state.applyString(diff, inst.getAckNum());

                dataAcked = true;
            }

            for (int i = 0; i < receiveStates.size(); i++) {
                TimestampedState<RemoteState> state = receiveStates.get(i);
                if (state.num > newState.num) {
                    receiveStates.add(i, newState);
                    warn("Received OUT-OF-ORDER state {} [ack {}]", newState.num, inst.getAckNum());
                    return;
                }
            }

            receiveStates.add(newState);
            sender.setAckNum(newState.num);

            sender.remoteHeard(newState.timestamp);
            if (dataAcked) {
                sender.setDataAck();
            }
        }
    }

    private void processThrowawayUntil(long throwawayNum) {
        // when sender's throwaway num equals receiver's ackNum there were problems
        receiveStates.removeIf(next -> next.num < throwawayNum);
        assert receiveStates.size() > 0;
    }

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

}