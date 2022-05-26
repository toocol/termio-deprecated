package com.toocol.ssh.core.mosh.core.crypto;

import com.google.protobuf.ByteString;
import com.toocol.ssh.core.mosh.core.network.Compressor;
import com.toocol.ssh.core.mosh.core.network.ICompressorAcquirer;
import com.toocol.ssh.core.mosh.core.network.MoshPacket;
import com.toocol.ssh.core.mosh.core.network.TransportFragment;
import com.toocol.ssh.core.mosh.core.proto.InstructionPB;
import com.toocol.ssh.utilities.utils.Timestamp;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

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
            blks = AeOcb.transferBlockArrays(encryptBlks, 0);
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
        Compressor.debugMode();
        String key = "zr0jtuYVKJnfJHP/XOOsbQ";
        Crypto.Session encryptSession = new Crypto.Session(new Crypto.Base64Key(key));
        Crypto.Session decryptSession = new Crypto.Session(new Crypto.Base64Key(key));
        TransportFragment.Fragmenter fragmenter = new TransportFragment.Fragmenter();
        TransportFragment.FragmentAssembly fragments = new TransportFragment.FragmentAssembly();

        int oldNum = 0;
        int newNum = 0;
        int ackNum = 0;
        int tNum = 0;
        for (int i = 0; i < 1000; i++) {
            InstructionPB.Instruction.Builder builder = InstructionPB.Instruction.newBuilder();
            builder.setProtocolVersion(MOSH_PROTOCOL_VERSION);
            builder.setOldNum(oldNum++);
            builder.setNewNum(newNum++);
            builder.setAckNum(ackNum++);
            builder.setThrowawayNum(tNum++);
            builder.setDiff(ByteString.copyFromUtf8(randomString()));
            builder.setChaff(ByteString.copyFrom(makeChaff()));
            InstructionPB.Instruction inst = builder.build();

            AtomicBoolean assembleFlag = new AtomicBoolean();
            fragmenter.makeFragments(inst, 500).forEach(fragment -> {
                byte[] bytes = fragment.toBytes();

                MoshPacket sendPacket = newPacket(bytes);
                Crypto.Message origin = sendPacket.toMessage();

                byte[] encrypt = encryptSession.encrypt(origin);
                Crypto.Message decrypt = decryptSession.decrypt(encrypt, encrypt.length);
                MoshPacket recvPacket = new MoshPacket(decrypt);
                TransportFragment.Fragment frag = new TransportFragment.Fragment(recvPacket.getPayload());

                if (fragments.addFragment(frag)) {
                    InstructionPB.Instruction recvInst = fragments.getAssembly();
                    assembleFlag.set(true);
                    assertEquals(inst, recvInst);
                }

                assertArrayEquals(origin.nonce.ccBytes(), decrypt.nonce.ccBytes());
                assertArrayEquals(origin.text, decrypt.text);
                assertEquals(sendPacket, recvPacket);
            });

            assertTrue(assembleFlag.get());
        }
    }

    @Test
    public void testDecrypt() throws DecoderException {
        String correctHex = "80 00 00 00 00 00 00 00 a7 0e a8 61 4f cc f6 5d 57 e5 f2 06 b0 1e 5e 4d 14 6d e8 a5 39 83 c4 4b f1 2c 4f 76 a9 e8 2c 5c 94 05 25 3f db 4c 74 81 6f 41 a2 1b 10 cd 3b ad f6 a5 c8 bb bd 6f 9b 67 b8 32 d7 ed b9 d1 67 12 b2 5d 9d 04 2e fd c2 bb fe 2b 4d 36 72 09 d8 f8 ea 6d 2e a3 a7 33 b3 d4 01 43 70 1e 63 a9 94 24 7f 1d 9f 03 d3 38 2b 20 86 0f 58 ff 14 15 5a 85 87 08 b8 72 ef 7f ad 83 03 65 b3 0c c1 9b 95 50 92 91 8d 1b 87 39 fc 7a 09 6f 1c c3 d8 18 d9";
        String wrongHex = "80 00 00 00 00 00 00 05 06 80 7f 5d be 69 ad fa 30 69 e9 6c 0d 0b 1d 96 6f 9d 92 f9 9a 67 4c 52 9d 92 66 44 51 da d1 3d b7 a2 fd ea 2c 9a 66 94 17 db a8 ad 9b 9f 88 39 1e f1 ed b1 d4 9f dd d4 b6 ac e8 f7 35 f3 cb 6f 9d 2d 67 c8 4a 64 bc 19 1b 83 b2 43 17 2c 41 c9 5d f9 e4 ba 8b d6 84 70 c2 94 27 eb 87 a5 86 38 6a b5 29 39 e7 d0 08 d3 aa 8c 7b f5 7c bc ce 96 7e b6 c4 08 0c 27 b9 1e 42 39 41 7b 74 65 7a 52 cd 51 a0 83 80 af 85 7f f8 90 48 7e 81 6b a3 32 67 12";

        String key = "4UVV8YWRGg1kBxAlhQ09ZA";
        byte[] correctBytes = Hex.decodeHex(correctHex.replaceAll(" ", ""));
        byte[] wrongBytes = Hex.decodeHex(wrongHex.replaceAll(" ", ""));
        Crypto.Session session = new Crypto.Session(new Crypto.Base64Key(key));

        Crypto.Message correctMessage = session.decrypt(correctBytes, correctBytes.length);
        Crypto.Message wrongMessage = session.decrypt(wrongBytes, wrongBytes.length);
    }

    private String randomString() {
        int low = 0, high = 5000;
        int len = RandomUtils.nextInt(low, high + 1);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < len; i++) {
            int r = RandomUtils.nextInt();
            char ch = (char) (r % (127 - 32) + 32);
            builder.append(ch);
        }
        return builder.toString();
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

    private byte[] makeChaff() {
        int chaffMax = 16;
        int chaffLen = Prng.uint8() % (chaffMax + 1);

        byte[] chaff = new byte[chaffLen];
        Prng.fill(chaff, chaffLen);
        return chaff;
    }
}