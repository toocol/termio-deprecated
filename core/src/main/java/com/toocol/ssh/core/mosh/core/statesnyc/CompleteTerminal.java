package com.toocol.ssh.core.mosh.core.statesnyc;

import com.google.protobuf.ByteString;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.InvalidProtocolBufferException;
import com.toocol.ssh.core.mosh.core.proto.HostInputPB;
import com.toocol.ssh.utilities.console.Console;
import com.toocol.ssh.utilities.log.Loggable;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/7/13 23:07
 * @version: 0.0.1
 */
public class CompleteTerminal extends State<CompleteTerminal> implements Loggable {
    private static final int ACK_BUFFER = 32;
    private static final Console CONSOLE = Console.get();

    private final Queue<byte[]> outputQueue = new ConcurrentLinkedDeque<>();
    private final Map<Long, byte[]> acked = new ConcurrentHashMap<>();

    private long echoAck = -1;

    @Override
    public void applyString(byte[] diff, long ackNum) {
        if (diff == null || diff.length == 0) {
            return;
        }
        try {
            ExtensionRegistry registry = ExtensionRegistry.newInstance();
            registry.add(HostInputPB.hostbytes);
            registry.add(HostInputPB.resize);
            registry.add(HostInputPB.echoack);
            HostInputPB.HostMessage input = HostInputPB.HostMessage.parseFrom(diff, registry);
            for (int i = 0; i < input.getInstructionCount(); i++) {
                HostInputPB.Instruction instruction = input.getInstruction(i);
                if (instruction.hasExtension(HostInputPB.hostbytes)) {
                    ByteString hostString = instruction.getExtension(HostInputPB.hostbytes).getHoststring();
                    info("Host String: " + hostString.toStringUtf8());
                    act(hostString.toByteArray(), ackNum);
                } else if (instruction.hasExtension(HostInputPB.resize)) {
                    HostInputPB.ResizeMessage resize = instruction.getExtension(HostInputPB.resize);
                } else if (instruction.hasExtension(HostInputPB.echoack)) {
                    long echoAckNum = instruction.getExtension(HostInputPB.echoack).getEchoAckNum();
                    if (echoAckNum == echoAck) {
                        return;
                    }
                    echoAck = echoAckNum;
                }
            }
        } catch (InvalidProtocolBufferException e) {
            // do nothing
        }
    }

    private void act(byte[] bytes, long ackNum) {
        bytes = CONSOLE.cleanUnsupportedCharacter(bytes);

        if (acked.containsKey(ackNum) && acked.get(ackNum).length >= bytes.length) {
            return;
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

        acked.put(ackNum, bytes);

        outputQueue.offer(bytes);
    }

    public Queue<byte[]> getOutputQueue() {
        return outputQueue;
    }
}
