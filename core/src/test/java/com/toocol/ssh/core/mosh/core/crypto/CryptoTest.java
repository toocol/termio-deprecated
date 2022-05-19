package com.toocol.ssh.core.mosh.core.crypto;

import com.google.protobuf.ByteString;
import com.toocol.ssh.core.mosh.core.network.ICompressorAcquirer;
import com.toocol.ssh.core.mosh.core.network.MoshPacket;
import com.toocol.ssh.core.mosh.core.network.TransportFragment;
import com.toocol.ssh.core.mosh.core.proto.InstructionPB;
import com.toocol.ssh.utilities.utils.Timestamp;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;

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
        String key = "zr0jtuYVKJnfJHP/XOOsbQ";
        Crypto.Session encryptSession = new Crypto.Session(new Crypto.Base64Key(key));
        Crypto.Session decryptSession = new Crypto.Session(new Crypto.Base64Key(key));
        TransportFragment.Fragmenter fragmenter = new TransportFragment.Fragmenter();
        TransportFragment.FragmentAssembly fragments = new TransportFragment.FragmentAssembly();

        int oldNum = 0;
        int newNum = 0;
        for (int i = 0; i < 1000; i++) {
            InstructionPB.Instruction.Builder builder = InstructionPB.Instruction.newBuilder();
            builder.setProtocolVersion(MOSH_PROTOCOL_VERSION);
            builder.setOldNum(oldNum++);
            builder.setNewNum(newNum++);
            builder.setAckNum(0);
            builder.setThrowawayNum(0);
            builder.setDiff(ByteString.copyFromUtf8(randomString()));
            builder.setChaff(ByteString.copyFrom(makeChaff()));
            InstructionPB.Instruction inst = builder.build();

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
                    assertEquals(inst, recvInst);
                }

                assertArrayEquals(origin.nonce.ccBytes(), decrypt.nonce.ccBytes());
                assertArrayEquals(origin.text, decrypt.text);
                assertEquals(sendPacket, recvPacket);
            });

        }
    }

    private String randomString() {
        int low = 0, high = 500;
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