package com.toocol.ssh.core.mosh.core.network;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.toocol.ssh.core.mosh.core.proto.InstructionPB;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static com.toocol.ssh.core.mosh.core.network.NetworkConstants.MOSH_PROTOCOL_VERSION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/5/14 22:17
 * @version:
 */
class CompressorTest {

    @Test
    void compressorTest() {
        Compressor compressor = Compressor.get();

        InstructionPB.Instruction.Builder builder = InstructionPB.Instruction.newBuilder();
        builder.setProtocolVersion(MOSH_PROTOCOL_VERSION);
        builder.setOldNum(0);
        builder.setNewNum(1);
        builder.setAckNum(1);
        builder.setThrowawayNum(0);
        builder.setDiff(ByteString.copyFromUtf8(UUID.randomUUID().toString() + UUID.randomUUID()));
        builder.setChaff(ByteString.copyFromUtf8("CHAFF"));
        InstructionPB.Instruction inst = builder.build();

        byte[] bytes = inst.toByteArray();
        byte[] compress = compressor.compress(bytes);
        assertTrue(bytes.length > compress.length);

        byte[] origin = compressor.decompress(compress);

        assertEquals(new String(origin, StandardCharsets.UTF_8), new String(bytes, StandardCharsets.UTF_8));
        try {
            InstructionPB.Instruction instruction = InstructionPB.Instruction.parseFrom(origin);
            assertEquals(instruction.toString(), inst.toString());
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }
}