package com.toocol.ssh.core.mosh.core.crypto;

import com.google.common.primitives.Bytes;
import com.google.protobuf.ByteString;
import com.toocol.ssh.core.mosh.core.network.Compressor;
import com.toocol.ssh.core.mosh.core.network.ICompressorAcquirer;
import com.toocol.ssh.core.mosh.core.network.MoshPacket;
import com.toocol.ssh.core.mosh.core.proto.InstructionPB;
import com.toocol.ssh.utilities.utils.ByteUtil;
import com.toocol.ssh.utilities.utils.Timestamp;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static com.toocol.ssh.core.mosh.core.network.NetworkConstants.MOSH_PROTOCOL_VERSION;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/5/2 22:08
 * @version:
 */
class CryptoTest implements ICompressorAcquirer {
    private short savedTimestamp;
    private long savedTimestampReceivedAt;

    @Test
    public void testCipher() {
        String key = "zr0jtuYVKJnfJHP/XOOsbQ";

        Crypto.Session session =  new Crypto.Session(new Crypto.Base64Key(key));
        System.out.println(session.ctx.encryptCipher.getBlockSize());
        AeOcb.Block blk = AeOcb.Block.zeroBlock();
        try {
            byte[] encrypt = session.ctx.encrypt(blk.getBytes());
            AeOcb.Block encryptBlk = AeOcb.Block.fromBytes(encrypt);
            assertNotNull(encryptBlk);
            assertNotEquals(blk.l, encryptBlk.l);
            assertNotEquals(blk.r, encryptBlk.r);

            System.out.println("encrypt: " + encryptBlk.l + ":" + encryptBlk.r);

            encryptBlk = AeOcb.Block.swapIfLe(encryptBlk);
            System.out.println("swap: " + encryptBlk.l + ":" + encryptBlk.r);

            encryptBlk = AeOcb.Block.doubleBlock(encryptBlk);
            System.out.println("double: " + encryptBlk.l + ":" + encryptBlk.r);

            encryptBlk = AeOcb.Block.swapIfLe(encryptBlk);
            System.out.println("swap: " + encryptBlk.l + ":" + encryptBlk.r);

            encryptBlk = AeOcb.Block.swapIfLe(encryptBlk);
            System.out.println("swap: " + encryptBlk.l + ":" + encryptBlk.r);

            AeOcb.Block[] blks = new AeOcb.Block[] {
                    AeOcb.Block.zeroBlock(),
                    AeOcb.Block.zeroBlock(),
                    AeOcb.Block.zeroBlock(),
                    AeOcb.Block.zeroBlock()
            };
            byte[] encryptBlks = session.ctx.encrypt(AeOcb.getBytesFromBlockArrays(blks, 0, blks.length));
            assertEquals(encryptBlks.length, 4 * 16);

            encryptBlk = AeOcb.Block.fromBytes(encrypt);
            assertNotNull(encryptBlk);
            blks = AeOcb.transferBlockArrays(encryptBlks, 1);
            for (AeOcb.Block b : blks) {
                assertEquals(b.l, encryptBlk.l);
                assertEquals(b.r, encryptBlk.r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testBase64() {
        String key = "zr0jtuYVKJnfJHP/XOOsbQ";

        Crypto.Base64Key base64Key = new Crypto.Base64Key(key);

        assertEquals(key, base64Key.printableKey());
    }

    @Test
    public void testEncrypt() {
        Compressor compressor = getCompressor();

        InstructionPB.Instruction.Builder builder = InstructionPB.Instruction.newBuilder();
        builder.setProtocolVersion(MOSH_PROTOCOL_VERSION);
        builder.setOldNum(0);
        builder.setNewNum(1);
        builder.setAckNum(0);
        builder.setThrowawayNum(0);
        builder.setDiff(ByteString.copyFromUtf8(UUID.randomUUID().toString() + UUID.randomUUID()));
        builder.setChaff(ByteString.copyFromUtf8("CHAFF"));
        InstructionPB.Instruction inst = builder.build();

        byte[] bytes = inst.toByteArray();
        byte[] compress = compressor.compress(bytes);

        String data = "Hello World~d";
        String key = "zr0jtuYVKJnfJHP/XOOsbQ";
        Crypto.Session encryptSession = new Crypto.Session(new Crypto.Base64Key(key));
        Crypto.Session decryptSession = new Crypto.Session(new Crypto.Base64Key(key));

        MoshPacket moshPacket = newPacket(data.getBytes(StandardCharsets.UTF_8));
        Crypto.Message origin = moshPacket.toMessage();

        byte[] encrypt = encryptSession.encrypt(origin);
        Crypto.Message decrypt = decryptSession.decrypt(encrypt, encrypt.length);

        assertTrue(ByteUtil.equals(origin.text, decrypt.text));
        assertTrue(ByteUtil.equals(origin.nonce.ccBytes(), decrypt.nonce.ccBytes()));
    }

    private MoshPacket newPacket(byte[] bytes) {
        short outgoingTimestampReply = -1;

        long now = Timestamp.timestamp();

        if (now - savedTimestampReceivedAt < 1000) {
            outgoingTimestampReply = (short) (savedTimestamp + (short) (now - savedTimestampReceivedAt));
            savedTimestamp = -1;
            savedTimestampReceivedAt = -1;
        }

        return new MoshPacket(
                bytes,
                MoshPacket.Direction.TO_SERVER,
                Timestamp.timestamp16(),
                outgoingTimestampReply
        );
    }

}