package com.toocol.ssh.core.mosh.core.network;

import com.toocol.ssh.core.mosh.core.proto.InstructionPB;
import com.toocol.ssh.core.mosh.core.statesnyc.UserEvent;
import com.toocol.ssh.core.mosh.core.statesnyc.UserStream;
import com.toocol.ssh.core.term.core.Term;
import com.toocol.ssh.utilities.execeptions.NetworkException;
import io.vertx.core.datagram.DatagramSocket;
import org.apache.commons.lang3.StringUtils;

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

    public Transport(String serverHost, int port, String key) {
        this.addr = new Addr(serverHost, port, key);
    }

    public void connect(DatagramSocket socket) {
        this.sender = new TransportSender<>(new UserStream(), this.addr, socket);
        this.sender.setSendDelay(1);
        // tell the server the size of the terminal
        pushBackEvent(new UserEvent.Resize(Term.WIDTH, Term.HEIGHT));
    }

    public byte[] recv(byte[] recv) {
        byte[] bytes = sender.getConnection().recvOne(recv);
        TransportFragment.Fragment fragment = new TransportFragment.Fragment(bytes);
        if (fragments.addFragment(fragment)) {
            InstructionPB.Instruction inst = fragments.getAssembly();
            if (inst.getProtocolVersion() != NetworkConstants.MOSH_PROTOCOL_VERSION) {
                throw new NetworkException("mosh protocol version mismatch");
            }
            sender.processAcknowledgmentThrough(inst.getAckNum());
            sender.setAckNum(inst.getNewNum());
            if (StringUtils.isNotEmpty(inst.getDiff().toString())) {
                sender.setDataAck();
            }
            return inst.getDiff().toByteArray();
        }

        return null;
    }

    public void tick() {
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

}