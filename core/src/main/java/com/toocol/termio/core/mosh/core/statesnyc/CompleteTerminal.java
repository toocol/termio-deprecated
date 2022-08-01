package com.toocol.termio.core.mosh.core.statesnyc;

import com.google.protobuf.ByteString;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.InvalidProtocolBufferException;
import com.toocol.termio.core.mosh.core.proto.HostInputPB;
import com.toocol.termio.utilities.console.Console;
import com.toocol.termio.utilities.log.Loggable;

import java.nio.charset.StandardCharsets;
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

        // Special judgment is required when ackNum is equal to 1
        if (acked.containsKey(ackNum) && acked.get(ackNum).length >= bytes.length && ackNum != 1) {
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

        info("Host String: " + new String(bytes, StandardCharsets.UTF_8));

        acked.put(ackNum, bytes);

        outputQueue.offer(bytes);
    }

    public Queue<byte[]> getOutputQueue() {
        return outputQueue;
    }
}
