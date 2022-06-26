package com.toocol.ssh.core.mosh.core.crypto;

import com.google.protobuf.ByteString;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.toocol.ssh.core.mosh.core.network.Compressor;
import com.toocol.ssh.core.mosh.core.network.ICompressorAcquirer;
import com.toocol.ssh.core.mosh.core.network.MoshPacket;
import com.toocol.ssh.core.mosh.core.network.TransportFragment;
import com.toocol.ssh.core.mosh.core.proto.InstructionPB;
import com.toocol.ssh.core.mosh.core.proto.UserInputPB;
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

        Crypto.Session session = new Crypto.Session(new Crypto.Base64Key(key));
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

            AeOcb.Block[] blks = new AeOcb.Block[]{
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
    public void wiresharkDump1() throws DecoderException {
        String key = "H8czB7uE1l1oy6/Nn+elkw";

        /*
        * Those hex dump were coming from actual mosh connection.
        * */
        String req1 = "00 00 00 00 00 00 00 00 72 f9 6a 80 d9 03 c3 e6 81 63 30 6b cd 28 c3 e4 2d 28 79 01 18 8f 53 56 a5 e8 58 0f 0f 2c 05 a9 cb f1 f3 dc ad 78 a3 67 5f b7 d8 ca aa a9 0a cc f6 72 4e aa 3d c5 de c8 77 8a 9c e2 ea 18 ea d1 4b 84 e0";
        String req2 = "00 00 00 00 00 00 00 01 9d 3f b3 e3 d3 b6 39 6b 18 4e 24 c3 81 84 ef 44 10 46 d6 f1 7a 51 8d cc 71 20 e3 d6 c9 b9 ce 03 2f 67 a8 fc 91 d8 67 82 98 f0 e1 15 cb 36 0f 2a 50 78 ea df 8c 3b dd ea 88 e3 57 2a c5";
        String req3 = "00 00 00 00 00 00 00 02 c6 17 c4 94 7c fd a1 fb a9 3d da 6c 74 22 94 86 fe 45 76 4b f2 02 29 2f cf ab 04 c6 21 63 9a 21 b5 d1 ae 0c 27 38 af ea 4e 60 39 0c f7 9c 6d 43 7f 55 2a 29 12 64 d5 31 61 c8 f5 24 f4 7d 85 64 ce e1 f3 d3 b7";

        String resp1 = "80 00 00 00 00 00 00 00 45 f5 ad 51 7f 66 06 ff d2 fa cf 94 a4 4d 9d 4c 2a 97 7c 34 72 44 ea 98 2d c1 5a b1 16 e8 a1 31 68 27 ec fc 17 7a c1 79 da 0e 28 02 04 2f b3 7a db f8 d2 15 59 0a 79 15 16 63 7a 6e 85 c7 76 af bc 6e 51 a6 d5 2e b5 a3 44 d1 cc c0 3d 7e b7 e6 bd 2e 3d 1e ac b0 9d f7 9f 2e 8f 53 54 e2 8c 5f e9 2c 16 eb dd 73 dd 99 14 7a e8 c1 ec b6 fc 3e 57 ea cc e7 52 13 e3 d2 4b 56 49 cf 25 f2 e1 85 b7 63 ab 6e 55 29 67 9f d3 fb 2d f4 21 8b 06 25 a7 33 1f 70 06 98 82 f5 e0 11 a1 11";
        String resp2 = "80 00 00 00 00 00 00 01 4a c5 dc e8 e4 ba e3 4f 55 26 7b 47 a4 d4 81 31 9e a0 62 78 5d d8 a6 44 35 0d 19 15 59 b3 69 1f 77 35 21 8f c5 8f 1a 7f b9 64 d3 65 a1 1f 88 b1 0d 2f fd db 5a 7e 25 7f 00 45 5d 7b 51 50 20 3f 22 d5 cf c1 d5 53 41 8c 96 5c e2 d6 de 6a e0 2a f2 f2 db 09 fb 40 12 1f f6 99 9e 19 af 26 f1 c8 9f f9 1d 9d 5a b8 71 04 19 24 4b c7 60 99 63 5a 48 e6 52 6c f2 30 67 c7 46 14 39 74 96 6b 2a 14 84 81 08 e5 79 3e d7 a1 c7 5f 88 c4 bf a1 dd 53 b4 6c 9d da 30 99 b3 ae 1c a4 e8 61 ff 63 49 86 ac 06 8d 19 06 de f1 f5 8f 3d b8 50 61 0a 24 fb 08 60 f2 db 7e 82 8b 6e 9a f0 32 c7 e6 b5 dd 1c 29 cf 74 2a 71 de 72 ff 77";
        String resp3 = "80 00 00 00 00 00 00 03 7f f3 27 eb c5 1f ea ed a3 07 2e 35 9c 96 c2 56 df 0a 22 05 d6 5c 78 cd 23 44 30 d8 69 06 17 bd 78 af 05 a9 cc 82 38 1d 79 a9 d3 2e b2 a9 fd d9 e6 84 f7 99 d6 f8 80 2b 31 c3 d3";

        String input1 = "00 00 00 00 00 00 02 09 63 50 2b 87 4d 46 23 af 4a 3b 83 2c e2 91 74 30 80 0c 8e 71 4b d5 ea 43 de a8 63 5c 5a 8c f0 b9 71 97 b1 d0 74 2c 5b 7a a9 47 e1 4b 9d 57 4e f9 14 04 89 ab d8 c4 22 49 ae c5 77 c5 6d 80 33 8f 66 ba ec f2 64 f3 ee fa 97 7b 69";
        String input2 = "00 00 00 00 00 00 02 0a 19 9c 18 ab c0 d9 eb 43 18 16 bc ae f9 64 8c 31 86 65 d1 86 6f 2f 2c c3 30 61 8a 16 06 49 9a b2 4a 6c f2 5d 0f 58 9f 9b b8 14 0e ba 84 fd 8e bf b4 e8 76 51 c1 e3 8e ce d1 59 91 58 dd ba bb 36 3f 89 94 2d 98 d1 37 da";
        String input3 = "00 00 00 00 00 00 02 0b a8 8f 4c 3c e7 38 6d 62 9d 43 0f 4b 4b d4 81 3b 1d aa bd 55 ef b1 49 47 19 da 54 ff 11 63 e3 36 13 8c 63 45 97 86 c1 34 b1 48 7a 22 60 e3 3c d6 32 af b6 ee 89 ce 68 cc 1c 98 ac 66 09 80 46 a6 cf eb c3 41";

        String respIn1 = "80 00 00 00 00 00 01 fb 65 d8 6a ce 15 80 82 79 8a 6d 20 00 5d a5 78 e0 30 3a 3a 1f 4a 12 93 77 05 00 89 e4 3b e0 3c d3 bb 00 cb fc 97 e9 4b 2d 66 4e f8 4b b3 7a 06 7c 11 87 8d 14 1e 9d 24 1d 25 87 9b e5 eb 73 a1 3a 48 f5 f9 88";
        String respIn2 = "80 00 00 00 00 00 01 fc c8 5a f5 c4 ba cd d5 8a f7 03 72 99 e5 d5 e3 71 32 96 48 c7 25 7f 01 49 14 be 0f 03 ca 67 1d 0d b8 94 0d 22 b8 1f f0 08 c5 ee 27 1f 93 49 35 ac a9 7e ec 54 7b 25 61 32 55 4a 98 c0 05 a9 b2 dc 16 0a 75 b0 50 98 67 ff 50 11 a9 0c 0d 55 42 38 35 b6 9c 83 a1 2d 24";
        String respIn3 = "80 00 00 00 00 00 01 fd d4 f9 37 4e 4a 3d 15 3f 8f ae 09 1b 01 bb 32 61 a0 36 92 d2 14 56 2b 9a 03 7f 94 46 f4 cb de 7c 0a 4f 58 86 98 4d eb a7 1c cf 3b b8 50 f0 b2 87 bf 7f 40 d2 ff 3a e3 66 87 aa 96 d6 28 c6 80 67 82 39 09 38 11 59 f7 7c 26 a7 55 50 e6 e8 7d a4 08 b8 55 09 49 f5 97 da be e8 d3 7e 4b a7 16 e6 69 cd 64 bf 6c 61 fb ad b6 7e f6 14 77 35 09 79 44 9a 25 36 97 5e d4 0a fd f3 47 5f 9f a7 25 09 5e f7 ca c4 1c 37 55 b1 63 21 a8 02 76 97 d5 2c 1d 58 e0 7b a6 bc e2 b1 e7 58 fd 6b ed 3e c9 08 28 0d 2f 63 b0 04 ce 79 13 e8 0a 67 1a a8 ed fe 84 3e eb 6e 84 45 22 ba fc 8f e1 a7 3f 11 c3 a1 0d 66 3a 24 70 f2 e8 b7 21 6b 18 ed 15 20 45 ce 3d f7 42 45 6d eb 1a d0 aa 61 65 a3 28 4c a9 4f e1 a4 38 b9 0b 40 6c c5 f8 60 94 f2 52 71 6e 6e d5 1a ba 5c 87 0d bd";

        Crypto.Session session = new Crypto.Session(new Crypto.Base64Key(key));

        printParseMessage(req1, resp1, 1, session, UserInputPB.resize);
        printParseMessage(req2, resp2, 2, session, UserInputPB.resize);
        printParseMessage(req3, resp3, 3, session, null);

        printParseMessage(input1, respIn1, 4, session, UserInputPB.keystroke);
        printParseMessage(input2, respIn2, 5, session, UserInputPB.keystroke);
        printParseMessage(input3, respIn3, 6, session, UserInputPB.keystroke);
    }

    private void printParseMessage(String reqHex, String respHex, int idx, Crypto.Session session, GeneratedMessage.GeneratedExtension<?, ?> extension) throws DecoderException {
        byte[] reqBytes = Hex.decodeHex(reqHex.replaceAll(" ", ""));
        byte[] respBytes = Hex.decodeHex(respHex.replaceAll(" ", ""));

        TransportFragment.FragmentAssembly fragments = new TransportFragment.FragmentAssembly();
        MoshPacket packet;
        Crypto.Message message;
        TransportFragment.Fragment frag;

        message = session.decrypt(reqBytes, reqBytes.length);
        packet = new MoshPacket(message);
        frag = new TransportFragment.Fragment(packet.getPayload());
        if (fragments.addFragment(frag)) {
            InstructionPB.Instruction recvInst = fragments.getAssembly();
            System.out.println("req " + idx + " : ");
            System.out.println("---");
            System.out.println(recvInst.toString());

            if (extension != null) {
                try {
                    ExtensionRegistry registry = ExtensionRegistry.newInstance();
                    registry.add(extension);
                    System.out.println(UserInputPB.UserMessage.parseFrom(recvInst.getDiff().toByteArray(), registry).toString());
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }
        }

        message = session.decrypt(respBytes, respBytes.length);
        packet = new MoshPacket(message);
        frag = new TransportFragment.Fragment(packet.getPayload());
        if (fragments.addFragment(frag)) {
            InstructionPB.Instruction recvInst = fragments.getAssembly();
            System.out.println("resp " + idx + " : ");
            System.out.println("---");
            System.out.println(recvInst.toString());
        }
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