package com.toocol.ssh.core.mosh.core.crypto;

import com.google.common.collect.Lists;
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

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

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
        String wiresharkHex = "80 00 00 00 00 00 00 00 35 56 20 3d 78 72 42 34 26 e7 34 31 4f 41 d4 57 6b e2 f7 d2 bf 39 09 95 f2 af 56 b1 bf 1f 46 c4 d7 18 0f 4e 9d c9 4f 07 51 39 02 c8 04 95 78 09 6d 8b 34 2f 36 aa 46 50 1e 3a 0c f2 12 a6 32 73 18 a4 f1 c7 36 f4 32 9c ab b9 2c d9 cd d4 34 22 52 c0 e5 48 08 31 0f d9 c6 24 c3 fd a2 06 da fd ce 24 b9 8c 0d 8f b2 34 fa e0 23 a6 ad 6d 8e 4b 9b de 66 29 28 67 45 20 b0 a4 ac f3 19 95 72 14 5c ad 14 45 b5 b9 0f 33 20 1e";
        System.out.println("Wireshark hex: " + wiresharkHex.replaceAll(" ", ""));

        String bytesStr = "-128, 0, 0, 0, 0, 0, 0, 0, 53, 86, 32, 61, 120, 114, 66, 52, 38, -25, 52, 49, 79, 65, -44, 87, 107, -30, -9, -46, -65, 57, 9, -107, -14, -81, 86, -79, -65, 31, 70, -60, -41, 24, 15, 78, -99, -55, 79, 7, 81, 57, 2, -56, 4, -107, 120, 9, 109, -117, 52, 47, 54, -86, 70, 80, 30, 58, 12, -14, 18, -90, 50, 115, 24, -92, -15, -57, 54, -12, 50, -100, -85, -71, 44, -39, -51, -44, 52, 34, 82, -64, -27, 72, 8, 49, 15, -39, -58, 36, -61, -3, -94, 6, -38, -3, -50, 36, -71, -116, 13, -113, -78, 52, -6, -32, 35, -90, -83, 109, -114, 75, -101, -34, 102, 41, 40, 103, 69, 32, -80, -92, -84, -13, 25, -107, 114, 20, 92, -83, 20, 69, -75, -71, 15, 51, 32, 30";
        String[] split = bytesStr.split(", ");
        byte[] wiresharkBytes = new byte[split.length];
        for (int idx = 0; idx < split.length; idx++) {
            wiresharkBytes[idx] = Byte.parseByte(split[idx]);
        }

        String aeStr = "d997054438be3ebbd6001544e77a211fcba6cc34d5839a53202e92186749360786bd2f618b60477400f62c9c3dd638f9751173fc5084ddbe40058a60ad73d36363db78715b76025887d07aadc8dfbe104d5ca4f7afd45eed3cef4d1c1da6a32de1a6c10d347b18ba0baaea1f5146a1d224f222010a932c3f07676d282b6ffb1c21b6e04f434b00c9a459befa8fbf406e929e495ff2ee3e9f9ef60133cb9554381a364b72a207d3f3007ba3c542a434d724aacf485c085f8f05f9b153263a156fa2f43737e38eeda6380db90317458ce2bc23478d421c5aed692fa9f59f227a12e6db421e87a3014ed7cfa6078b415c8fb930199e48e2afe4965998b3b2dad5332cdd0baae9e502c82dbe3ae45db826b9dfbe9667ba9859250874c9bf68286313d03b0acf64afcc4a3af5593056bff83318149422b70d897b3ddbdb599a39da0cf45a50608a1273c4e879f8e306a01d8af4997f0e8acc86ec4367eef16bba3a85d123412aa0ed7ac27f59cca6c3258f5793a2f5214f068baa714ec0cd502996fc237e68a7eb499ccaa7018f4c7aa0e1c3a454c44dd4f1cb9f09a995bfdc6fb45748a2656658e51f8402e94e0c515fdee99cd5c7ba616b91f8784a579dcfd5f105d7096f704c225f66b0bc8acd5b064349974912637d0e768e29de8c1dcd3be8538abc184301354aac4652256cc187961bc769d17f339f5ddaeb6584ab1b13ec8d69fbed27fb16d06e223644988c0ca565ce218d4931573c665e26fdafc27887d2457c102f70105e57faa4af9d3c3c901f128d6c035ad78cab848f223045a8fa07361436344a04bd6210d7c6290e23e2da6301612fce86143f9cfc375ad462202c7146aac78971fe1a97cb6fce7ac06161eff2e1cb2fd1d856108b8c9de6a71e6b6dafd011178c45b720666ca9cad3dd29d8e6b3b9206a426c38d2775a90c831a60d0d0a405e29e3d68b544faac7abcf4118204984499d03c8fa31eae6e48ba68c84728aae29da8b9e6d975252eaf19c35e9751669c22fac93717f5a9f30d44ec11164f5c9ec7c428aabb71e78dd8e64cd83e8f423db20a7aa80eaa76aba1469bb735cd87c7e315aab0b936fe5559dd02e40723530150f22e64ce3d9f5fdae463b29c831e9e3228f78fa9a41ad1a1f79f25fc1b52080307eb32a5012942a64cb869093f332e7df2d0da2d41af5e1ce67d21252fe11f117cf1c75fe59bb8dcb3da1f958735d5fea956e407db033940618bfef46bd88b4d9cfc8850e0d119dca3ce545509e4d5ea11d42ba89176c53227ac9ce33a61f940477fbc012b7ca506d18dd5e0fc9307475acecec974886ebf9d0f76f956522aecbb15b34e9edad3f959f6ea14a26bbf96b8c70648d45190ed8113a4947fcaa743a3e5ef36a5d55bddeae8b909a696b4c1076fe07c95170cfdc519d7530a875d96611418a2ce6683fc6df93f5ef790df5fd5a4417171c6d093ec17aaef5975990fc9e9bdc9f8e1479cfa3ea99c44a484ee53de3fcc6e6ef788906199d832d103b085457526ad7d11b73b1d26e5729";
        byte[] aeBytes = Hex.decodeHex(aeStr);

        System.out.println("AE bytes: " + Arrays.toString(aeBytes));
        System.out.println("AE bytes length: " + aeBytes.length);
        System.out.println("Wireshark bytes: " + Arrays.toString(wiresharkBytes));
        System.out.println("Wireshark bytes length: " + wiresharkBytes.length);

        String key = "fDSpx96gmdY+CPPiINZKNQ";
        Crypto.Session session = new Crypto.Session(new Crypto.Base64Key(key));

        Crypto.Message message = session.decrypt(wiresharkBytes, wiresharkBytes.length);
        Crypto.Message aeMessage = session.decrypt(aeBytes, aeBytes.length);
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