package com.toocol.ssh.core.mosh.core.crypto;

import com.google.protobuf.ByteString;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.toocol.ssh.core.mosh.core.network.ICompressorAcquirer;
import com.toocol.ssh.core.mosh.core.network.MoshPacket;
import com.toocol.ssh.core.mosh.core.network.TransportFragment;
import com.toocol.ssh.core.mosh.core.proto.InstructionPB;
import com.toocol.ssh.core.mosh.core.proto.UserInputPB;
import com.toocol.ssh.utilities.utils.Timestamp;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
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
        String key = "zr0jtuYVKJnfJHP/XOOsbQ";
        Crypto.Session encryptSession = new Crypto.Session(new Crypto.Base64Key(key));
        Crypto.Session decryptSession = new Crypto.Session(new Crypto.Base64Key(key));
        TransportFragment.Pool sendPool = new TransportFragment.Pool();
        TransportFragment.Fragmenter fragmenter = new TransportFragment.Fragmenter(sendPool);
        TransportFragment.Pool receivePool = new TransportFragment.Pool();
        sendPool.init();
        receivePool.init();
        TransportFragment.FragmentAssembly fragments = new TransportFragment.FragmentAssembly(receivePool);

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
                Crypto.Message origin = sendPacket.fillMessage(new Crypto.Message());

                byte[] encrypt = encryptSession.encrypt(origin);
                sendPool.recycle();
                Crypto.Message decrypt = decryptSession.decrypt(encrypt, encrypt.length, new Crypto.Message());
                MoshPacket recvPacket = new MoshPacket(decrypt);
                TransportFragment.Fragment frag = receivePool.getObject().setData(recvPacket.getPayload());

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

    @Test
    public void wiresharkDump2() throws DecoderException {
        String key = "onKHXfA3uWhXCSYb0aePOQ";

        /*
         * Those hex dump were coming from actual mosh connection.
         * */
        String req1 = "00 00 00 00 00 00 00 00 11 e4 3e c6 f5 ab 10 4e 35 0e 32 e7 41 4e c7 90 65 89 05 2a bb 6c f6 aa 8e 70 96 fe 64 b6 f5 f5 06 0b 7f 49 2e 67 b4 77 cc 8a 2e 5c 7c a1 9a e5 be 72 19 d7 20 69 1d 5c 9b c0 f9 dc 89 2e f4 f8 7c ea 7a b3";
        String req2 = "00 00 00 00 00 00 00 01 dc ac c0 af bb 2d 87 35 45 64 89 62 d2 37 dc 08 e0 6f f5 6e 1d 72 1e 7c 5d 55 b4 3a 06 51 2f af 0c 18 19 cb b8 c2 4c 35 bd a7 46 85 2e 55 5f 52 78 31 26 db f9 32 64 72 ba cf 34 91 97 2d 02 1e 97 03 e4 10 6a 14 99 4f 3b 4d a2 66 af ae c1 34 5f b7 3c d3 79";
        String req3 = "00 00 00 00 00 00 00 02 e9 f3 39 51 2d 37 88 ae dc 1c 51 2e 49 9b 34 0d 32 8f 81 12 6b 69 f6 d0 ae a8 04 44 78 eb f0 11 29 1f 01 3d 56 29 c3 77 37 a4 10 d6 5d 26 d7 9b 33 1f b6 f7 f8 c6 6c 81 09 5b ed 0e 5b 1f d6 99 60 d1 5c a9 c5";
        String req4 = "00 00 00 00 00 00 00 03 4e 5c 20 0b c3 9b 4d e8 81 ec 70 d7 ae 3a 42 42 aa 5e c0 15 2e 62 ce 04 01 18 05 c8 1e a5 fc 99 ca 10 72 65 2f 28 39 ec 98 65 bb 2b c2 80 e8 cd d3 f5 46 b9 79 5e 3c 44 87 1f 6e 05 ec 29 0d 99 29 f9 6a 71 d8 94 7e 7e 03 14 e1 32 cd b6 2c 79";


        Crypto.Session session = new Crypto.Session(new Crypto.Base64Key(key));

        printParseMessage(req1, null, 1, session, UserInputPB.resize);
        printParseMessage(req2, null, 2, session, UserInputPB.resize);
        printParseMessage(req3, null, 3, session, UserInputPB.resize);
        printParseMessage(req4, null, 4, session, UserInputPB.resize);
    }

    @Test
    public void wiresharkDump3() throws DecoderException {
        String key = "DLEV/svKCu8IJKYkRMby1A";

        /*
         * Those hex dump were coming from actual mosh connection.
         * */
        String req1 = "00 00 00 00 00 00 00 00 0d a3 70 a7 4b 7f 19 5f e4 18 98 f4 2b a7 89 d5 6f 3f 8a 2f c8 96 e6 4e 63 2a b5 02 9b a8 9d ae 66 e3 8a 01 02 1d ee 5c e7 c1 06 bc 94 05 c4 14 a0 f3 31 9c 6c c9 c1 12 19 cb f1 84 89 71";
        String req2 = "00 00 00 00 00 00 00 01 8a 18 6e 6c d6 61 fb 83 f3 25 39 35 2d 24 4c fb 81 64 7b 3d a2 75 ef 53 5f da 30 31 f3 e4 f0 14 4b c4 89 5e 56 bf 12 54 d5 ca ef 6d 0d 89 3f a1 dc 53 7b a9 0e 9e cd 36 be 61 9f 8a f8 47";
        String req3 = "00 00 00 00 00 00 00 02 91 a9 38 56 12 75 7a e3 f1 37 41 08 b1 af d3 6b df a9 6f ac fe 12 08 69 83 4b c1 50 8f 49 17 02 b5 af 42 7a d6 02 df 8c 57 8c 26 24 0b 22 62 7b 7e f0 40 58 3f 41 7f a9 b3 c2 b0 61 07 51 a1 52 63 7a df 4d";
        String req4 = "00 00 00 00 00 00 00 03 cb de cb f8 14 a8 dd 5a 30 ec 2a 8e dd b3 5d 94 a5 43 f7 b8 58 9b b1 8a 3b 6e c9 f6 00 68 ed 4e df 22 67 72 94 1c f5 2e 46 85 79 b7 10 b4 34 c9 4d 89 d2 f5 91 74 85 58 b7 56 e3 82 a4";
        String req5 = "00 00 00 00 00 00 00 04 f2 8c bf 84 18 f0 33 81 eb 33 64 b0 e8 41 8d 5f d2 77 8f bb d2 1c de 1a c8 05 93 15 20 1d c4 36 b9 55 50 e4 c7 74 a1 71 6b 3f 3d ac 2b a3 25 54 76 85 d2 3b fb 48 92 78 15 6f";

        String resp1 = "80 00 00 00 00 00 00 00 79 b4 c4 90 13 75 ea f8 a0 91 97 b3 61 85 1e 30 b2 81 45 6a f7 cb fb 33 9c a7 8d 5b 74 04 18 07 9a c3 ed f5 f2 6e c4 45 73 33 db ca 23 9b bc bb bb 32 11 14 ed e7 c5 fa 0c c9 15 66 8f a3 63 1f b8 95 73 fd 6f a8 8b 5d 46 e7 b0 d2 9a 00 69 46 a3 84 bd 60 4f 27 8b c7 56 fd 45 66 46 90 c2 87 f7 a3 1d 7b 4d 1e f8 d7 70 32 ff 43 15 32 6d 5a df 4e 4e 43 c8 00 35 fb 11 af eb 9c b3 6f 51 52 e6 9a 92 95 7e 24 8e 6e d1 10 b3 b9 6f a4 b4 a4 7b c6 5d 1d 37 04 73 df 90 19 ce 60 75";
        String resp2 = "80 00 00 00 00 00 00 01 e4 a2 cd 09 03 f6 c0 b2 c5 f6 3d 6a 88 a0 2e f6 37 e8 6d ac f5 c8 37 07 65 2d d8 b2 dd d4 b3 8f c1 63 66 d8 22 12 ac 20 77 f5 2f f2 dc ad 4d 6c 54 35 6e 6f f3 95 a7 86 81 30 56 da eb 58 23 89 ca 63 15 8e 96 b8 33 b9 ba 09 3b 1e 9b 15 ee 36 62 72 76 ac 17 1e 8f 51 43 7d 61 0a a1 a0 a1 63 92 ea b0 61 ee fc 27 1d 78 05 62 2d e3 f4 bd e9 9d db fe 03 93 63 55 3a c8 17 76 59 ee 37 22 27 86 fb d2 a7 f9 e0 e2 d9 41 52 c8 53 44 7a c5 6d a5 b3 62 57 d7 e4 6b be d9 24 73 37 a7 e9 7f 75 20 80 e8 0c 37 b5 f8 9d ff fa 8c 2a e5 34 8a d4 a2 94 99 24 6b 81 8d 0c 4f da 0a 28 60 7e 11 aa";
        String resp3 = "80 00 00 00 00 00 00 02 97 bd a3 b9 9c a8 01 c6 4c 08 07 d7 c4 7c db 07 41 24 be 2f 8f e4 e6 8d 2a 51 6f b5 a9 fc 44 0f ce 9b 8f ba ea 08 8b d7 8b 19 71 9f 5b b3 66 be 41 26 7e 00 e8 b8 d8 7a 91 3d c5 ac 31 0c 9d f4 f5 c2 bc f5";
        String resp4 = "80 00 00 00 00 00 00 04 c4 cc 88 cf dc 9d 66 9c 9a ba 5a 94 9a 38 d6 55 77 d8 06 95 68 84 d9 ca 80 66 e7 4d 2a 62 b8 bd f5 c9 09 00 5f fe 72 a1 a7 8e ec e7 5c 0f d6 bf e2 3f 41 22 94 5d a3 58 71 5d 6c d6 b4 aa 13 e6 84";
        String resp5 = "80 00 00 00 00 00 00 06 8e 92 9b 5b 2a 33 10 e5 6f dc fa 3e ad 97 7e 6f 77 7a d7 b8 51 07 de 4d 45 73 80 96 3a 69 3d f7 e5 78 b2 d6 3a 70 b4 b3 d2 e5 24 ee f0 1f 32 48 38 ed 9d 24 9c 2c 88 fc 28";

        Crypto.Session session = new Crypto.Session(new Crypto.Base64Key(key));

        printParseMessage(req1, resp1, 1, session, UserInputPB.resize);
        printParseMessage(req2, resp2, 2, session, UserInputPB.resize);
        printParseMessage(req3, resp3, 3, session, null);
        printParseMessage(req4, resp4, 4, session, null);
        printParseMessage(req5, resp5, 5, session, null);
    }

    @Test
    public void selfDump() throws DecoderException {
        String key = "WkQ1ElbK11SJywYUggbl7g";

        /*
         * Those hex dump were coming from actual mosh connection.
         * */
        String req1 = "00 00 00 00 00 00 00 00 6d 39 80 21 7c 77 78 53 01 07 51 5f 07 24 43 87 ea f0 d7 91 1f b8 ab ee 8a c8 88 46 ef 3f 9c 39 e5 3a 52 6c 1d 28 77 78 ee e2 81 91 b4 97 91 6e 4a ae 85 a4 a3 95 de 19 cc 92 37 a7 5c 1f c8 c0 0a 36 da 55 cd 10 c9 99 b4 c6 c8";
        String req2 = "00 00 00 00 00 00 00 01 a7 77 ba 59 55 a3 9a c5 b7 68 31 88 de c9 2e cd b8 2c d4 b0 fa ab 37 6f 89 a9 95 8e 79 c1 bf 66 5d 3a 8e b9 9a e6 28 d3 b4 cc ba f2 7e 17 ff 65 b9 15 4a 72 bf 7c 27 36 71 11 f5 d9 05 19 b7 24 14 ba f3 91 2e 8b 15 14 28 a4 fb 97 d0 2e f2 25 30 b9 f9 a2 57 f3 d2 d9 da 72 9d 81 f1 a1 39 2b 54 89 8a 03 6f 3f 8e dc eb";
        String req3 = "00 00 00 00 00 00 00 02 8d c8 d1 cd 9d 9b 01 b8 28 43 b0 16 72 a7 7d 4f 5d d2 7d 61 f2 a1 25 62 42 15 3f b6 11 89 cd 6e 7a 59 16 c8 fb 7a 2c 30 da 44 27 97 8c ea 95 7f 7a 04 9f 0a 4c 3d ae b7 3b 74 ae b4 84 c6 24 ce e8 8c ba 02 0e db 04 75 61 88 c5 dc 70 be 16 c4 8e 2d c8 eb 45 b1 62 16 0c fd e7 00 65 56 88 89 7c 65 38 71 21 cb 1c 19 94 ab 43 00 39 ff 02 ba 88 d6 2e";
        String req4 = "00 00 00 00 00 00 00 03 11 ab 59 41 c9 77 22 4b 26 c5 78 6b 9a 74 eb 64 34 43 4b cb 4e 5a 22 b8 71 a0 0e 4d 6b 90 a9 cd 91 48 ca a2 73 b8 9c 7c ef 17 b9 89 4a 43 2f 83 6f 67 e3 6b d7 b9 44 02 10 e2 2b 91 e1 60 fc a7 b4 98 e1 9b 43 db 87 6b 4d 6b b9 87 ca fa f9 e3 13 c4 be 54 a9 6f d9 4f 54 68 ec 31 4f 49 9a 11 e4 8c 71 6b 52 2b 66 85 4b bf af 4c 7c c0 bd 74 6b f0";

        String resp1 = "80 00 00 00 00 00 00 01 74 e9 4e 9e c3 40 0b 38 df aa 7e 57 aa 92 e8 15 6a 7a a3 ff 77 34 12 8e 37 8e 77 db 78 b1 e6 00 7d 5a 9e 81 d3 7e 68 19 71 44 76 30 4e fd 89 95 a3 94 e8 ac 33 9c 25 1e 40 31 36 5b 5d bf 22 dc 88 d4 55 04 af a2 78 b5 b6 84 9d 1a fc 22 e4 c7 70 c3 0f b9 af 63 38 84 18 8e 22 75 fe 0d 71 f5 31 0c 72 10 4d 52 31 53 7d e5 93 e9 87 18";
        String resp2 = "80 00 00 00 00 00 00 02 bd a4 96 71 2b 11 50 b4 4f 65 c2 65 8a e4 c4 c8 e2 0f 26 e5 74 12 29 23 c6 ea 07 ac f6 d8 d5 b0 3e 41 e7 dd 46 5f 21 f3 c2 57 ab 09 b4 e4 d6 ff 8a 37 bc 9d 1b 9f 9e 9a d0 73 f1 9d 84 27 b5 58 49 0d 76 92";
        String resp3 = "80 00 00 00 00 00 00 03 db bc c9 8c b3 73 7d f9 06 77 b2 c5 ce 34 a5 ed 2e a0 5a c4 9d ff a4 58 31 13 d0 f4 80 12 c8 71 a2 5a 52 f2 e2 a9 32 50 96 96 f2 e8 31 5e 16 5f 2e e4 c4 bc f7 8a 77 cc 8d ce 19";
        String resp4 = "80 00 00 00 00 00 00 06 4e 97 3f 9c f9 fa 65 a1 fc a4 e8 bb 49 a0 a4 57 c3 fd c9 70 eb 66 ab 69 a8 c6 9b 86 78 a4 4b be 5d a0 77 ab ef a8 71 9a f5 30 d9 4c b0 11 a0 a5 4e bf ce 6b 5c c7 ad 7e 8a 12 43 cc";


        Crypto.Session session = new Crypto.Session(new Crypto.Base64Key(key));

        printParseMessage(req1, resp1, 1, session, UserInputPB.resize);
        printParseMessage(req2, resp2, 2, session, UserInputPB.keystroke);
        printParseMessage(req3, resp3, 3, session, null);
        printParseMessage(req4, resp4, 4, session, null);
    }

    @Test
    public void moshClientDump() throws DecoderException {
        String key = "T9LSlLfIS+KPQsMwXXqSFQ";
        String[] payloads = new String[]{
                "00:00:00:00:00:00:00:8c:dd:f2:51:61:bb:b5:5a:0f:ad:27:87:f1:0d:8e:b7:18:1d:c4:26:0d:e9:a9:3f:91:0b:8e:5f:0e:8d:22:dc:50:2e:a8:72:9d:06:96:a2:bf:c4:0d:0f:94:15:61:61:9d:15:15:f2:f2:fd:f1:02:4f:9c:87:6b:c0:98:da:96:73:2a:1d:d3:c3:67:b0:4d:32",
                "00:00:00:00:00:00:00:8d:9c:be:f1:32:85:9d:c0:9b:8d:a0:55:70:e0:8b:fe:5e:31:d3:e1:7d:94:79:49:6a:6a:52:0f:f8:c2:25:de:bf:31:96:5e:09:f7:65:ea:47:d4:7a:ba:21:00:07:96:8e:da:ca:e0:b3:2c:c2:5d:40:ce:c6:28:c4:ea:f4:0d:24:41:fc:b5:ed:14:c0:a0:99:26:44:a4:d0:15:30",
        };
        Crypto.Session session = new Crypto.Session(new Crypto.Base64Key(key));
        for (int i = 0; i < payloads.length; i++) {
            printParseMessage(payloads[i], null, i, session, UserInputPB.keystroke);
        }
    }

    @Test
    public void moshClientDump2() throws DecoderException {
        String key = "+vjwtzPJDsiJ8NNGrOlDSA";
        String[] payloads = new String[]{
                "00:00:00:00:00:00:00:1e:b4:63:b9:dc:3d:d8:ce:58:d1:bc:27:53:fb:4a:ab:e7:47:17:84:0a:5e:12:b8:5a:6d:2d:94:5e:40:1e:61:62:e9:79:0a:fb:6c:42:99:a9:10:46:e2:28:d3:e0:2b:fd:12:9b:72:bc:2f:fb:2d:c2:8f:8c:f0:e7:51:aa:6a:dd:c7:bc:50:f2:ac:05:1f:2f:ba:3d:9c:28:6d:5c:0d:fb:6d:ea:60:a6:4a:da:0b:8d:d2:b1:2c:ed:a4:48:9c:dc:24:ae:96:3e:f7:77:de:b1:45:31:44:2e:83:2e:3b:e6:cb:02:8d:b5:f4:47:e8:47:9a:9b:43:d5:ce:1e:bc:e0:32:6f:03:c8:6b:7e:dc:26:67:30:8b:49:25:0a:ed:58:3f:cc:b9:7a:5a:91:61:ae:cb:19:26:8b:ce:31:11:c4:bf:a0:d2:de:cd:d3:e3:8c:bb:47:c4:23:04:c4:bd:4a:80:2e:a7:d4:d8:0c:31:da:33:39:f7:e7:d9:66:70:7f:2c:5a:b2:69:2c:68:78:55:bc:b4:62:78:62:0b:e7:37:79:a8:c9:d3:75:89:61:73:da:fd:4c:cd:df:83:b8:7f",
                "00:00:00:00:00:00:00:1f:60:02:dc:74:c4:33:16:c5:0c:e4:84:89:5c:19:aa:ac:f5:8d:70:67:41:57:eb:b0:ee:71:72:f0:f6:60:23:8a:12:f5:de:c1:83:5c:85:df:42:7d:4b:9b:b8:9c:5a:45:40:06:a7:2e:47:fa:e5:63:a9:66:70:d0:31:93:db:66:31:52:dd",
                "00:00:00:00:00:00:00:20:04:f0:7e:87:40:6d:ae:c0:a5:92:0d:5f:8e:d4:6e:3e:9c:9b:84:39:8f:59:63:c7:5a:a7:01:7f:f2:ad:9c:93:e3:fa:08:17:72:79:da:b4:2c:ae:e3:a7:52:cb:44:b8:f5:08:10:98:97:46"
        };
        Crypto.Session session = new Crypto.Session(new Crypto.Base64Key(key));
        for (int i = 0; i < payloads.length; i++) {
            printParseMessage(payloads[i], null, i, session, UserInputPB.keystroke);
        }
    }

    @Test
    public void moshDump() throws DecoderException {
        String key = "4w+88XxBDYgoSwFvfDRj8A";
        String[] req = new String[]{
                "00:00:00:00:00:00:00:00:ec:f9:43:d7:e6:e0:33:64:87:54:c1:98:5a:8c:57:8a:8c:b2:8b:3e:7a:90:bb:4c:f8:c2:e8:75:76:4e:92:9f:4e:a4:1e:33:64:fb:52:28:f0:4e:9d:09:d0:80:2a:b4:20:fb:19:2f:63:86:14:42:fb:7c:1b:9c:8d:cd:56:57:2d:cb:56:e9:70:64:3b:3b:07",
                "00:00:00:00:00:00:00:01:77:fe:c1:e0:66:ed:98:a8:82:84:8b:09:59:07:7c:43:8c:77:72:20:55:a7:83:93:aa:bf:b3:2b:b7:69:7b:27:e5:4c:57:09:3e:ca:d5:0a:60:71:c6:db:e6:a9:ac:21:73:de:13:56:df:36:b5:2a:cd:22:12:0a:22:09:63:60:94:33:b7:f7:07:20",
                "00:00:00:00:00:00:00:02:79:22:8a:40:ce:bf:72:91:7b:85:56:a9:25:08:3e:48:35:d6:e3:12:c9:d0:31:24:53:6b:76:f8:e3:8b:db:fb:6a:3b:dd:08:3e:59:c3:c1:65:e4:1b:77:e2:fd:62:f0:10:e4:7d:a8:bc:e5:b3:e2:8b:3f:ba:32:ec",
                "00:00:00:00:00:00:00:03:d6:ec:24:73:af:87:37:4a:64:6d:c5:71:3b:72:6d:ef:92:19:cd:ee:f1:93:70:92:4c:55:af:cd:3e:72:7b:e5:8e:21:83:fd:8f:3f:b3:e5:2c:9e:d3:8b:b4:10:38:2f:bf:67:bb:9d:46:18:f8:9b:57:29:b8",
                "00:00:00:00:00:00:00:04:9a:8a:89:2f:f9:8a:46:e4:40:2a:76:3d:76:99:00:80:7c:58:39:15:74:23:76:50:06:3e:57:51:3a:97:54:d8:4c:dc:ac:f9:b3:e5:db:ae:47:30:11:6b:6b:f6:03:d3:37:14:2c:0f:23:d2:2f:be:20:01:9e:1c:d3:0c",
                "00:00:00:00:00:00:00:05:f9:cb:41:f2:05:21:a8:23:94:9c:9d:25:1b:f7:9d:da:d9:40:9a:f1:9a:70:76:a4:f1:5a:2d:80:d5:36:db:3a:4c:80:a7:97:cc:3b:eb:26:2b:39:15:7b:53:78:b1:b5:d3:ca:a9:30:97:6d:bc:df:9c:1a:a3:7f:64:dd:ba:1d:44:6e:dd:84",
                "00:00:00:00:00:00:00:06:c4:44:b5:9f:d4:0c:51:94:c6:97:2b:de:70:5c:85:bc:3a:80:0b:6b:5b:b9:d6:0c:c6:6f:65:17:75:fa:79:7a:6b:ed:f3:28:9e:a9:f7:a7:94:2c:e1:d8:98:a9:43:be:fc:07:d6:78:a8:ff:f7:02:bf:ed:19:6a:16:dd:c1:28:85",
                "00:00:00:00:00:00:00:07:d4:03:a6:b9:61:7d:1b:66:bb:16:63:07:b8:4e:00:ce:2d:6e:17:a3:10:e8:bb:f3:3c:5f:4e:3e:2c:1e:a6:29:e2:21:b0:c7:dd:55:c1:12:8e:e6:9b:d6:ed:74:3a:62:a7:66:41:62:0b:5c:a3:4c:ee:c1:2f:11:74:14:d8:a8",
                "00:00:00:00:00:00:00:08:d5:39:7d:e6:ef:5d:bd:40:83:eb:11:99:37:b1:0a:9a:41:16:6f:6e:3c:c9:f3:29:91:a3:1b:af:c6:51:f0:69:e9:34:af:99:61:47:b3:70:72:90:95:87:ef:f7:6c:25:d3:be:72:89:a5:e4:0d:ef:03:7d:b1:e3",
                "00:00:00:00:00:00:00:09:29:f3:46:79:9b:aa:2f:79:dd:73:27:56:34:57:57:06:a1:80:56:1b:c3:a3:f3:1b:2c:37:a9:57:5c:a9:20:a0:33:7e:fb:e8:1b:c6:91:a9:7b:97:02:0e:bf:a8:5e:be:88:2a:9a:7a:04:34:4e:3f:3b:aa:fe:38:fa:57:9c:34",
                "00:00:00:00:00:00:00:0a:f3:89:dd:64:a9:b6:fc:8a:c1:7d:e2:62:6c:33:13:ed:40:f5:b8:7c:c2:67:aa:0b:62:b5:77:23:ca:e9:d3:1d:d4:58:93:3d:f1:78:6c:64:66:05:a6:50:78:c8:cd:cd:22:2d:52:d1:21:29:94:5e:6b:d1:82:b6:84:71:6e:2f",
                "00:00:00:00:00:00:00:0b:a2:d2:ac:c0:d1:0c:08:72:55:f8:8b:38:42:d5:a0:e5:79:14:b0:b3:82:db:5c:b6:ae:54:b0:50:6c:14:55:a9:a7:47:38:27:83:d1:14:27:da:38:4a:e2:37:f2:f5:31:01:6d:b0:e4:43:0e:33:db:0c:83:00:48:b0:49:c0:b6:29:03",
                "00:00:00:00:00:00:00:0c:7f:80:8f:ef:4d:6a:19:92:87:46:bd:dd:63:91:91:e1:19:58:d1:a1:a4:3b:ee:73:28:7c:2c:cb:39:8a:f6:e5:fb:c3:1c:3c:af:75:e3:93:08:aa:08:9d:28:6d:15:05:78:52:15:57:c2:3e:6d:6a:93:06:fd:72:02:c5:ff:29:fe:d6:f8:0a:0b",
                "00:00:00:00:00:00:00:0d:34:16:51:69:ca:e9:07:1a:d3:a4:1d:d4:f7:2a:b2:cf:27:05:99:43:1f:dc:dc:41:62:c3:a1:dd:cd:51:a7:25:bf:c1:35:90:5d:45:10:5a:78:62:2f:d2:27:b5:3f:3b:61:09:ef:6e:af:8b:f2:5b:ef:74:54:9d:90:31:80:01:80:c7:22",
                "00:00:00:00:00:00:00:0e:17:55:8d:58:28:e2:83:db:cb:d7:46:27:e0:97:98:fd:06:9f:58:d8:1d:12:13:c0:83:1f:67:a2:3d:7e:9a:12:9b:04:db:f8:5c:1f:55:2a:29:0d:4c:2f:9d:fd:7e:f4:a2:80:9c:a7:9b:75:d6:f6:cf:10:ab:59:d7:58:32:86:8f:a7:79",
                "00:00:00:00:00:00:00:0f:df:85:a9:8f:49:d8:10:a5:4f:02:67:81:91:89:00:43:7a:53:07:a3:cc:fd:9f:b5:1c:ef:59:ce:e6:57:d2:c9:2c:9c:f6:41:b7:4c:3e:fc:60:08:6c:d1:07:85:a5:a7:fd:4c:ba:f2:a3:16:7e:64:a0:44:89:f3:a3:2f:9b:f9:21:3e:75",
                "00:00:00:00:00:00:00:10:f0:c8:ef:26:25:34:9a:a5:b0:0c:20:99:aa:75:d2:c4:92:47:10:b8:dc:b7:af:a9:1b:dc:17:0b:d1:15:98:9a:5a:24:fb:79:ce:13:e3:03:f4:76:ee:c9:5c:d2:05:3b:eb:fd:c2:10:f8:dc:9f:c6:21:da:f7:24",
                "00:00:00:00:00:00:00:11:09:eb:3d:ee:f6:72:16:ea:82:3e:ec:e9:15:9f:aa:2d:bf:ca:fa:69:46:be:67:7a:e5:22:37:10:6e:bb:82:b7:66:0f:b2:6d:12:5a:e2:30:0c:ab:fc:30:89:e7:ca:19:31:7e:74:2b:64:6e:b5:94:19:8c:86:82:46",
                "00:00:00:00:00:00:00:12:59:27:3d:70:ef:6a:70:44:4e:69:dc:ff:84:a6:d2:fc:f4:70:5b:6d:6e:60:37:f8:92:31:27:90:14:f4:6d:53:1f:f1:84:46:6b:07:66:bc:95:af:96:08:53:c4:fd:24:18:4a:dd:2d:33",
                "00:00:00:00:00:00:00:13:db:6d:59:f6:de:a8:94:24:5c:da:6f:56:a0:c8:a2:d1:4c:97:4c:2b:1c:a4:f8:c1:ce:17:c3:98:11:98:db:18:81:96:da:97:79:23:aa:8c:6d:af:1a:14:2f:97:02:70:2c:8d:bd:9e:8f:a1:c6:c8:04:f1:92:4d:55:f2:f5:43:5d:d2:a4:23:6e",
                "00:00:00:00:00:00:00:14:b3:d2:84:b8:78:93:0d:62:85:93:04:9f:40:1c:b6:2d:30:67:5f:f0:70:de:fc:ac:1c:19:1f:e4:9f:54:d2:67:ff:13:a2:a6:a2:a0:07:37:77:92:0a:c6:a9:3f:bb:94:5d:8f:e9:cf:bc:7d:f0:b6:3a:c6"
        };
        String[] resp = new String[]{
                "80:00:00:00:00:00:00:00:37:d6:6f:99:ea:df:f0:1b:03:ed:3e:7d:31:8d:cf:e2:c2:3c:7a:c9:a9:67:67:33:c8:a1:ef:1b:35:7f:9f:a4:81:7f:2b:ee:c1:d7:5d:b4:46:e9:d2:64:00:29:f4:01:ef:55:33:7b:cf:94:50:fc:f4:4e:65:8b:0e:95:b0:79:71:0d:eb:9e:0b:0b:70:3f:52:22:f0:da:f4:3c:dc:25:be:79:89:ac:f3:16:65:5b:0f:4d:19:d7:37:e0:35:58:b5:a1:b1:92:1a:c2:cb:5f:10:51:05:e5:df:b4:9f:47:09:5f:37:b9:1d:f8:5e:53:70:d3:cd:25:57:ee:9e:f8:e4:94:13:21:8b:e5:35:d5:ff:f9:94:a7:77:08:97:40:65:87:f7:db:a1",
                "80:00:00:00:00:00:00:01:64:0f:36:96:8d:44:cc:ba:db:51:15:f7:89:c4:e0:1b:82:39:f2:83:d9:ef:a5:fa:0e:b1:c2:a8:37:79:b2:91:c4:b0:07:ba:2b:e6:7a:63:4d:90:11:14:a7:31:f2:58:9f:e4:2b:c0:21:1a:83:72:51:58:ec:fa:e9:1c:4e:96:a0:c5:44:1c:34:50:76:db:16:8d:7e:ac:84:4e:74:fd:86:0d:30:4e:3f:55:d1:60:40:ec:23:70:2d:28:62:fd:aa:07:76:f7:1f:b6:5c:5a:c7:73:66:2c:97:ed:63:61:e0:e5:eb:d3:7c:4c:16:5e:61:1e:ae:9d:9a:6d:c7:22:54:6d:3a:d6:44:ff:2e:9b:ff:96:3d:a4:ad:a5:ec:31:32:21:bd:34:8f:9c:ce:fd:71:ff:3d:7b:74:f7:06:24:d6:f9:7a:58:dc:b8:7c:94:ae:7d:98:7a:eb:8b:6f:cd:64:0e:0d:fe:4a:21:f9",
                "80:00:00:00:00:00:00:02:26:1a:45:1e:bf:3d:93:51:07:36:ea:99:5d:92:d0:74:a5:a5:c1:fd:bb:f3:55:d3:b3:8d:ef:5b:f9:7a:8a:c9:fa:1f:40:bf:62:93:8d:3e:e9:68:63:59:e3:88:f6:6d:5d:67:6f:75",
                "80:00:00:00:00:00:00:03:03:36:e0:24:ab:9f:6d:93:c8:d3:68:42:bf:24:d8:c0:16:63:f5:0f:ab:c4:52:fa:42:e3:f2:11:0a:49:cd:83:4e:f7:26:e0:d5:a6:81:b2:df:9f:36:a0:ff:34:83:0b:53:34:0e:6a:54:4b:9b:ba:f2:ed:d1:e1:0b:1d:b1:ef:db:34:59",
                "80:00:00:00:00:00:00:04:9b:46:e0:d9:7a:96:1a:8c:b4:ec:56:4d:63:70:da:22:ec:9c:da:44:53:49:1c:29:85:bd:2a:99:9a:c8:e4:d7:72:a4:53:06:a1:7b:7c:8b:93:39:5c:72:79:28:f8:dd:9e:bd:35:79:69:3c:13:0b:e0:6f:ff:7c:45",
                "80:00:00:00:00:00:00:05:a0:29:f9:d5:65:69:39:3d:d7:23:ee:b5:0f:c1:f1:16:5b:9f:45:0e:fa:c4:23:01:94:85:09:13:9d:5c:a4:dd:b6:0a:56:8c:b1:07:e5:f2:3d:d8:87:6b:09:69:1a:83:bf:ac:5f:81",
                "80:00:00:00:00:00:00:06:2b:69:15:58:43:cd:b3:7e:fe:7a:26:10:43:16:50:8c:1d:3e:88:63:31:6f:57:88:36:d3:80:b6:1a:d2:53:3b:49:8a:0e:37:08:ce:6c:2c:e6:1f:37:8b:4b:96:1c:cc:ce:2a:29:20:ca:67:e8",
                "80:00:00:00:00:00:00:07:76:65:15:e8:66:e6:fd:f1:f3:51:f2:6d:aa:ff:9b:8d:ac:e8:79:f2:91:f7:6d:a1:d4:d3:d1:2b:03:aa:1e:23:c0:cb:ad:9d:fc:2d:a1:d7:86:6b:27:87:a5:64:16:c6:91:eb:52:bb:9e:75:af:82:98:7e:51:97:d2:6b:60",
                "80:00:00:00:00:00:00:08:b0:8c:10:b4:dd:05:e5:63:a0:e7:bd:06:e8:8b:bb:e3:57:5a:82:94:03:61:9f:46:00:87:eb:3f:10:36:2a:22:88:c7:d7:50:eb:ee:94:5b:4c:12:69:75:3a:fb:e1:14:62:38:d9:d8:99:c3:ce:2c:07:83:ab",
                "80:00:00:00:00:00:00:09:12:e2:c4:fb:72:f2:1f:6b:6c:9f:e1:bf:14:bd:d5:48:72:14:46:cd:7e:ac:42:0e:65:b4:2f:fb:e7:ac:85:4d:7b:d2:e0:35:35:68:a4:74:f9:57:71:d3:ca:47:6e:ac:82:ff:ba:74:c3:3d:dc:ce:69:8c",
                "80:00:00:00:00:00:00:0a:9d:d9:30:1c:89:59:a0:1f:1e:ff:37:02:af:3f:0c:30:d7:0c:1c:52:bb:80:87:0d:58:05:e4:cc:6a:29:f3:96:8d:0c:1a:21:fb:de:d8:7a:23:f9:18:35:c9:28:03:a8:a8:55:f1:1b:29:1f:89:b2:30:5d:fc:44:67:6e:4d:08:de:81:6e:ba:ca:80:5f",
                "80:00:00:00:00:00:00:0b:71:f9:b1:9d:b6:1a:31:e1:2a:f5:c6:a1:21:7e:01:1c:24:0e:4d:52:85:6d:2b:e0:49:90:c2:47:6d:6f:04:d6:78:9e:e6:0b:b6:eb:8b:70:a8:d0:78:11:d1:a4:2e:4a:11:23:1a:43:34:90:31:f5:4f:3a:65:90:2a:42:39:db:02:ec:36:83:27:d8",
                "80:00:00:00:00:00:00:0c:62:46:c2:85:d5:36:a2:55:43:9b:fd:c1:e1:79:6b:77:7a:91:95:4b:cd:e8:42:46:2a:61:17:ee:86:f8:85:0b:27:a4:56:92:0a:cd:6d:31:94:1a:bc:4d:6e:75:f3:6a:3e:8e:26:19:29:e5:0f:dd:ac:72:3b:46:02:85:b8:1b:33:26:d7:44:20:e3",
                "80:00:00:00:00:00:00:0d:99:85:29:4a:5b:f7:fb:7c:e0:cb:76:2e:87:da:79:af:55:f7:d0:82:2f:b5:0a:7b:c8:9d:12:4b:2f:58:d7:98:bf:cc:36:aa:26:d7:65:f5:48:da:5b:c7:85:ea:34:58:b6:d5:11:b5:19:45:64:82:e8:9f:10:68:be:12:93:b1:7e:3d:fd:0e:af:90:4d:59:5d:19:1a",
                "80:00:00:00:00:00:00:0e:38:69:92:f3:e0:5c:78:d4:97:36:fd:63:25:cb:f4:f2:f0:71:6a:0c:14:ab:29:70:a6:af:b9:90:9b:f6:dc:e1:b7:5a:69:c4:1b:8b:49:8a:5c:73:71:8c:84:4e:63:b2:4c:40:ae:25:41:52:8e:68:59:19:11:85:96:a9:11:9c:27:07:00:e7:62:9f:a5:ed:f5:04:ac:a3:30:12:86:d4:aa:91:7c:db:c6:57:6d:5b:5e:2c:67:42:11:e3:10:dd:3c:5e:93:39:50:4e:84:22:8c:9b:71:46:3f:65:09:61:b0:14:5a:e4:ac:d7:58:ae:ab",
                "80:00:00:00:00:00:00:0f:e9:c7:70:69:30:c6:45:57:63:b0:3c:4e:b9:fe:86:c9:1a:87:43:7d:87:84:28:56:a3:00:40:ce:86:18:62:f2:5e:6f:ed:33:a7:50:dc:93:ba:2f:9c:b7:14:50:18:ce:76:61:d2:bf:67:6e:ac:da:66:60:ef:22:03:d4:20:2d:bc:44:3a:8c:20:90:2c:cf:3d:26:79:1e:44:70:da:36:a8:b6:b9:25:69:6d:9a:a1:09:7c:42:f3:3c:33:84:ab:94:e3:0a:03:aa:9e:b6:48:16:b3:63:17:57:9d:f5:94:e0:f9:5a:c5:67:c6:f5:2c:97:90:2d:7d:16:27:70:dc:a4:61",
                "80:00:00:00:00:00:00:10:55:08:ad:8c:4d:09:b6:44:9a:6c:5d:c8:6c:34:86:9d:20:94:7a:6a:fe:81:0c:c8:63:8a:39:38:b2:fe:a2:f7:af:44:46:f0:3f:e5:df:b9:77:7c:01:a4:cd:ef:67:a9:fc:db:78:d9:64:22:62:e3:e6:01:0f:0a:ae",
                "80:00:00:00:00:00:00:11:f1:eb:a2:54:8a:fa:25:16:bb:46:54:7b:bd:fd:1f:7f:48:41:7b:6b:0a:af:93:36:1f:87:2f:5d:df:ae:46:1f:99:2b:c4:95:7c:7c:6f:73:1e:fb:d8:a7:86:77:34:f7:b4:89:10:b8:5f:26:3f:ce:5d:0d:ed:51:8c:1c",
                "80:00:00:00:00:00:00:12:a8:80:e5:4e:e1:4d:5f:ec:6d:bb:4d:f3:2c:f3:35:0f:17:17:af:a8:41:3b:25:5e:a4:3d:31:74:c7:ca:1c:05:38:a3:91:90:d1:57:42:0a:3f:3b:ad:12:aa:a0:54:54:03:43:52:19:4e:89:f2:02:ab:a1:80:66:6c:a7:d0:5b:53:06"
        };
        Crypto.Session session = new Crypto.Session(new Crypto.Base64Key(key));
        for (int i = 0, j = 0; i < req.length && j < resp.length; i++, j++) {
            printParseMessage(req[i], resp[j], i, session, UserInputPB.keystroke);
        }
    }

    private void printParseMessage(String reqHex, String respHex, int idx, Crypto.Session session, GeneratedMessage.GeneratedExtension<?, ?> extension) throws DecoderException {
        byte[] reqBytes = null;
        byte[] respBytes = null;
        if (StringUtils.isNotEmpty(reqHex)) {
            if (reqHex.contains(" ")) {
                reqBytes = Hex.decodeHex(reqHex.replaceAll(" ", ""));
            } else if (reqHex.contains(":")) {
                reqBytes = Hex.decodeHex(reqHex.replaceAll(":", ""));
            }
        }
        if (StringUtils.isNotEmpty(respHex)) {
            if (respHex.contains(" ")) {
                respBytes = Hex.decodeHex(respHex.replaceAll(" ", ""));
            } else if (respHex.contains(":")) {
                respBytes = Hex.decodeHex(respHex.replaceAll(":", ""));
            }
        }

        TransportFragment.Pool pool = new TransportFragment.Pool();
        pool.init();
        TransportFragment.FragmentAssembly fragments = new TransportFragment.FragmentAssembly(pool);
        MoshPacket packet;
        Crypto.Message message = new Crypto.Message();
        TransportFragment.Fragment frag;

        if (reqBytes != null) {
            message = session.decrypt(reqBytes, reqBytes.length, message);
            packet = new MoshPacket(message);
            frag = pool.getObject().setData(packet.getPayload());
            if (fragments.addFragment(frag)) {
                InstructionPB.Instruction reqInst = fragments.getAssembly();
                assert reqInst != null;
                System.out.println("req " + idx + " : ");
                System.out.println("---");
                System.out.println(reqInst.toString());

                if (extension != null) {
                    try {
                        ExtensionRegistry registry = ExtensionRegistry.newInstance();
                        registry.add(extension);
                        System.out.println(UserInputPB.UserMessage.parseFrom(reqInst.getDiff().toByteArray(), registry).toString());
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (respBytes != null) {
            message = session.decrypt(respBytes, respBytes.length, message);
            packet = new MoshPacket(message);
            frag = new TransportFragment.Fragment(packet.getPayload());
            if (fragments.addFragment(frag)) {
                InstructionPB.Instruction recvInst = fragments.getAssembly();
                assert recvInst != null;
                System.out.println("resp " + idx + " : ");
                System.out.println("fragmentId : " + frag.getId());
                System.out.println("---");
                System.out.println(recvInst.toString());
            }
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