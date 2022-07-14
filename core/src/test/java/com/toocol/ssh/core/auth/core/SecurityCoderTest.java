package com.toocol.ssh.core.auth.core;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/25 20:09
 */
class SecurityCoderTest {

    static final SecurityCoder coder = new SecurityCoder("Fq3Jf5Li3-Lf2Tz3Jn0");

    @Test
    void get() {
        assertThrows(RuntimeException.class, SecurityCoder::get);

        new Thread(() -> assertThrows(RuntimeException.class, SecurityCoder::get)).start();

        assertThrows(RuntimeException.class, () -> new SecurityCoder(UUID.randomUUID().toString()));

        try {
            Constructor<SecurityCoder> declaredConstructor = SecurityCoder.class.getDeclaredConstructor(String.class);
            declaredConstructor.setAccessible(true);
            assertThrows(InvocationTargetException.class, () -> declaredConstructor.newInstance(UUID.randomUUID().toString()));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Test
    void encodeThenDecode() {
        SecurityCoder coderGet = SecurityCoder.get();
        for (int idx = 0; idx < 1000; idx++) {
            String origin = UUID.randomUUID().toString();
            String decode = coderGet.decode(coder.encode(origin));
            assertEquals(origin, decode);
        }
    }

}