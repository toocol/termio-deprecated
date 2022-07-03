package com.toocol.ssh.core.mosh.core.network;

import com.toocol.ssh.core.mosh.core.proto.InstructionPB;
import com.toocol.ssh.core.mosh.core.statesnyc.RemoteState;
import com.toocol.ssh.core.mosh.core.statesnyc.UserEvent;
import com.toocol.ssh.core.mosh.core.statesnyc.UserStream;
import com.toocol.ssh.core.term.core.Term;
import com.toocol.ssh.utilities.annotation.DiffThread;
import com.toocol.ssh.utilities.console.Console;
import com.toocol.ssh.utilities.execeptions.NetworkException;
import com.toocol.ssh.utilities.log.Loggable;
import com.toocol.ssh.utilities.utils.Timestamp;
import io.vertx.core.datagram.DatagramPacket;
import io.vertx.core.datagram.DatagramSocket;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@SuppressWarnings("all")
public final class Transport implements Loggable {

    private static final int ACK_BUFFER = 32;

    private static final Console CONSOLE = Console.get();

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
    private final TransportFragment.Pool receivePool = new TransportFragment.Pool();
    private final TransportFragment.FragmentAssembly fragments = new TransportFragment.FragmentAssembly(receivePool);
    private final List<TimestampedState<RemoteState>> receiveStates = new ArrayList<>();
    private final Queue<InstructionPB.Instruction> instQueue = new ConcurrentLinkedDeque<>();
    private final Queue<byte[]> outputQueue = new ConcurrentLinkedDeque<>();
    private final Map<Long, byte[]> acked = new ConcurrentHashMap<>();

    private TransportSender<UserStream> sender;
    private Connection connection;

    public Transport(String serverHost, int port, String key) {
        this.addr = new Addr(serverHost, port, key);
        receiveStates.add(new TimestampedState<>(Timestamp.timestamp(), 0, new RemoteState()));
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
        // Ensure that there is only one UserEvent to be sent at a time
        while (!sender.getLastSentStates().equals(sender.getCurrentState())) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                // do nothing
            }
        }
        sender.pushBackEvent(event);
    }

    @DiffThread
    public void tick() {
        recv();
        sender.tick();
    }

    public Queue<byte[]> getOutputQueue() {
        return outputQueue;
    }

    private void recv() {
        // Ensure that there is only one packet to be process at a time
        if (!instQueue.isEmpty()) {
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

            TimestampedState<RemoteState> newState = new TimestampedState<>();
            newState.timestamp = Timestamp.timestamp();
            newState.num = inst.getNewNum();

            for (int i = 0; i < receiveStates.size(); i++) {
                TimestampedState<RemoteState> state = receiveStates.get(i);
                if (state.num > newState.num) {
                    receiveStates.add(i, newState);
                }
            }

            processThrowawayUntil(inst.getThrowawayNum());

            receiveStates.add(newState);
            sender.setAckNum(newState.num);

            byte[] diff = inst.getDiff().toByteArray();
            info("Receive packet newNum = {}, ackNum = {}, diff = {}",
                    inst.getNewNum(), inst.getAckNum(), inst.getDiff().toStringUtf8());
            if (diff != null && diff.length > 0) {
                sender.setDataAck();

                diff = CONSOLE.cleanUnsupportedCharacter(diff);

                if (inst.getAckNum() == 2 && acked.containsKey(inst.getAckNum())) {
                    return;
                }
                if (acked.containsKey(inst.getAckNum()) && acked.get(inst.getAckNum()).length >= diff.length) {
                    return;
                } else if (acked.containsKey(inst.getAckNum()) && acked.get(inst.getAckNum()).length < diff.length) {
                    diff = subtractDiff(acked.get(inst.getAckNum()), diff);
                }
                if (acked.size() >= ACK_BUFFER) {
                    int cnt = 0;
                    for (Long ack : acked.keySet()) {
                        if (cnt == ACK_BUFFER / 2) {
                            break;
                        }
                        acked.remove(ack);
                        cnt++;
                    }
                }
                acked.put(inst.getAckNum(), diff);
                outputQueue.offer(diff);
            }
        }
    }

    private byte[] subtractDiff(byte[] l, byte[] r) {
        byte[] diff = new byte[r.length - l.length];
        System.arraycopy(r, l.length, diff, 0, r.length - l.length);
        return diff;
    }

    private void processThrowawayUntil(long throwawayNum) {
        receiveStates.removeIf(next -> next.num < throwawayNum);
        assert receiveStates.size() > 0;
    }

}