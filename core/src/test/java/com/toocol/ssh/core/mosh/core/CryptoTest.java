package com.toocol.ssh.core.mosh.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/5/2 22:08
 * @version:
 */
class CryptoTest {

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