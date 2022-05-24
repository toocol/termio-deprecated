package com.toocol.ssh.core.mosh.core.network;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.toocol.ssh.core.mosh.core.proto.InstructionPB;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
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
        byte[] compress = compressor.compress(bytes, true);
        assertTrue(bytes.length > compress.length);

        byte[] origin = compressor.decompress(compress, true);

        assertEquals(new String(origin, StandardCharsets.UTF_8), new String(bytes, StandardCharsets.UTF_8));
        try {
            InstructionPB.Instruction instruction = InstructionPB.Instruction.parseFrom(origin);
            assertEquals(instruction.toString(), inst.toString());
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    @Test
    void decompressTest() {
        String src = "eJx1VcmO20YQvesrCnOJDcgCkmNyGgwMJEEMGLCNHAwfSs0i2VFv6IUa+evzqluaxUhuZLHW914V7wNJKXwhW/a0SJDMzl32xJSsGKE40znbasNCdeVKi92k4FGIW11j/qlQPAfivDQvoe7p2Gr/PMlsAwJjQGraeGmyp7hJdpySpjvbusIzll6EyUmtkntlTv1B01ZrnAyjTyt81D7hvaB4pVJjvhzovY5QaOVN6CgSqLTjO+O4FDtbgXuhOWbPrgfbMF5+vRllhHMWMitnNmjEfkfc8UJ3Bc+xFUotJzS7p8kuGAwYubhYg/CYFw72O+uwsEpY6rq/o/MqWXiAdSv5BPZ/FFI/DF5iUD8niie9KeLmd1k2cdf8Nkx2s1ODU+VSwYbOJI8ItRKMgEcTw2wnhFs4eQ5g9e2e1uYjYF1QVubmgN1Foc3sj04JKTU3U1uGrQWk2DSBNoNhMSMFsOf0cdaRvGBEqebw9edvu937FwhG72NwF2plII94qCpfyKiQjC1+Tyk6fR7t2VnAY1HoOAcNuqoJpngE/lsfvWjliS1SO4SgczHROTH949BFlvlmuPZ51emB7p2PpRLkTT5OksNL2lXjFbqxgVLuLKuOzzGfitogW8j0WV5TOx7R5zXBGzkshz0KyCN6kEwfYxIsxn0YwiTswMNt9t7myy8fOLw90N+rdUgOohXtVpqu4ViizvCotactuuZtUEGO7pw9Cf0Z10B/RXN6VfYhQg45KLe/N+BMX7S7UtGB2rSRz2v0oOgDu7q28kPPit7HbIOxyfUlxVxtyPDKdAugFlN7OJTDbvcHNi/66xfI8QkbTfUFWwPQPlW+ifaBA08Macqr/TWagqGMf6C6hBugtW/7MzXTOzh8/eUbfYJvmFRbpTYV/GCzclvW+izoK1MjB3xqJOvBM6qhMZufTlw5WefKb8STt7gd8ZVG4qwK6arGurZgVRWIky4R7KkKT3FNyUHaXb7j3PTxV6VguKu1RKPbWcxY2v8pBG6YztyXDtcMTuMS90vRxwnjSD/NP7WsLtCNogVqlJfPq3IC19SxhBTuer07QF6GpOURRSeUBDgRBTJ5mSzj4wUQ3xDCGiG18893DDzFzcr4O4zOoZiIW5k7z1M0fZeVJI30fBrHBsdnIDHDA5Mhj84+wBLVeRda/zf0e6M3CFeNtYe0xhpxydJqzbUXoz8X7afGBGP/veCvYsMJQ+kRl34SniPL6NkjtguPDYSXOFw6xMBDCxpO/Zocdv8Cge6byg==";
        byte[] decode = Base64.getDecoder().decode(src);

        Compressor compressor = Compressor.get();
        byte[] decompress = compressor.decompress(decode, false);
        System.out.println(new String(decompress));
    }
}