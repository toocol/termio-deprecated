package com.toocol.ssh.core.mosh.core;

import com.google.common.primitives.Longs;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/5/2 22:08
 * @version:
 */
class CryptoTest {

    @Test
    public void testByteOrder() {
        long time = System.currentTimeMillis();
        long beTime = Longs.fromByteArray(Longs.toByteArray(time));
        long myBeTime = Longs.fromByteArray(ByteOrder.htoBe64(time));
        assertEquals(time, beTime);
        assertEquals(time, myBeTime);
    }

    @Test
    public void testCipher() {
        String key = "zr0jtuYVKJnfJHP/XOOsbQ";

        Crypto.Session session =  new Crypto.Session(new Crypto.Base64Key(key));
        AeOcb.Block blk = AeOcb.Block.zeroBlock();
        try {
            byte[] encrypt = session.ctx.encrypt(blk.getBytes());
            AeOcb.Block encryptBlk = AeOcb.Block.zeroBlock();
            encryptBlk.fromBytes(encrypt);
            assertNotEquals(blk.l, encryptBlk.l);
            assertNotEquals(blk.r, encryptBlk.r);

            System.out.println("encrypt: " + encryptBlk.l + ":" + encryptBlk.r);

            encryptBlk = AeOcb.Block.swapIfLe(encryptBlk);
            System.out.println("swap: " + encryptBlk.l + ":" + encryptBlk.r);

            encryptBlk.doubleBlock();
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
            byte[] encryptBlks = session.ctx.encrypt(AeOcb.getBytesFromBlockArrays(blks));
            assertEquals(encryptBlks.length, 4 * 16);

            encryptBlk.fromBytes(encrypt);
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
        String key = "zr0jtuYVKJnfJHP/XOOsbQ";
        Crypto.Session session = new Crypto.Session(new Crypto.Base64Key(key));
    }

}