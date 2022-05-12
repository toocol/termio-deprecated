package com.toocol.ssh.core.mosh.core.crypto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/5/13 2:20
 * @version:
 */
class AeOcbTest {

    @Test
    void testEndian() {
        AeOcb.Block block = AeOcb.Block.zeroBlock();
        block.l = 123123;
        block.r = 123123;

        for (byte b : ByteOrder.longBytes(block.l)) {
            System.out.print(b + " ");
        }

        System.out.println();

        block = AeOcb.Block.swapIfLe(block);

        for (byte b : ByteOrder.longBytes(block.l)) {
            System.out.print(b + " ");
        }
    }
}