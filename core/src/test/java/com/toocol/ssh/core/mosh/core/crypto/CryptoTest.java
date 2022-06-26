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

    private void printParseMessage(String reqHex, String respHex, int idx, Crypto.Session session, GeneratedMessage.GeneratedExtension<?, ?> extension) throws DecoderException {
        byte[] reqBytes = null;
        byte[] respBytes = null;
        if (StringUtils.isNotEmpty(reqHex))
            reqBytes = Hex.decodeHex(reqHex.replaceAll(" ", ""));
        if (StringUtils.isNotEmpty(respHex))
            respBytes = Hex.decodeHex(respHex.replaceAll(" ", ""));

        TransportFragment.FragmentAssembly fragments = new TransportFragment.FragmentAssembly();
        MoshPacket packet;
        Crypto.Message message;
        TransportFragment.Fragment frag;

        if (reqBytes != null) {
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
        }

        if (respBytes != null) {
            message = session.decrypt(respBytes, respBytes.length);
            packet = new MoshPacket(message);
            frag = new TransportFragment.Fragment(packet.getPayload());
            if (fragments.addFragment(frag)) {
                InstructionPB.Instruction recvInst = fragments.getAssembly();
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