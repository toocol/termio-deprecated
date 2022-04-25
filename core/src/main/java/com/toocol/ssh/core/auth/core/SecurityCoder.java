package com.toocol.ssh.core.auth.core;

import com.toocol.ssh.utilities.utils.StrUtil;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/25 0:27
 * @version: 0.0.1
 */
public record SecurityCoder(String key) {

    private static final String ALGORITHM = "AES";
    private static final String SECURE_RANDOM = "SHA1PRNG";
    private static final String SUPPORT_PACKAGE = "com.toocol.ssh.core.auth";

    private static SecurityCoder instance = null;

    public SecurityCoder(String key) {
        if (instance != null) {
            throw new RuntimeException("Illegal access.");
        }
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (!stackTrace[2].getClassName().startsWith(SUPPORT_PACKAGE)) {
            throw new RuntimeException("Illegal access.");
        }
        this.key = key;

        instance = this;
    }

    public static SecurityCoder get() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (!stackTrace[2].getClassName().startsWith(SUPPORT_PACKAGE)) {
            return null;
        }

        return instance;
    }

    public String decode(String origin) {
        if (StringUtils.isEmpty(origin)) {
            return StrUtil.EMPTY;
        }
        if (StringUtils.isEmpty(key)) {
            return origin;
        }
        try {
            return decryptData(key, origin);
        } catch (Exception e) {
            return null;
        }
    }

    public String encode(String origin) {
        if (StringUtils.isEmpty(origin)) {
            return StrUtil.EMPTY;
        }
        if (StringUtils.isEmpty(key)) {
            return origin;
        }
        try {
            return encryptData(key, origin);
        } catch (Exception e) {
            return null;
        }
    }

    private static String encryptData(String key, String message) throws Exception {
        KeyGenerator keygen = getKeyGenerator(key);
        SecretKey secretKey = new SecretKeySpec(keygen.generateKey().getEncoded(), ALGORITHM);

        return Base64.getEncoder().encodeToString(encrypt(secretKey, message.getBytes(StandardCharsets.UTF_8)));
    }

    private static String decryptData(String key, String ciphertext) throws Exception {
        KeyGenerator keygen = getKeyGenerator(key);
        SecretKey secretKey = new SecretKeySpec(keygen.generateKey().getEncoded(), ALGORITHM);
        return new String(decrypt(secretKey, Base64.getDecoder().decode(ciphertext)), StandardCharsets.UTF_8);
    }

    private static KeyGenerator getKeyGenerator(String key) throws NoSuchAlgorithmException {
        KeyGenerator keygen = KeyGenerator.getInstance(ALGORITHM);
        SecureRandom secureRandom = SecureRandom.getInstance(SECURE_RANDOM);
        secureRandom.setSeed(key.getBytes());
        keygen.init(128, secureRandom);
        return keygen;
    }

    private static byte[] encrypt(Key key, byte[] messBytes) {
        if (key != null) {
            try {
                // create a Cipher object base on AES.
                Cipher cipher = Cipher.getInstance(ALGORITHM);
                cipher.init(Cipher.ENCRYPT_MODE, key);
                return cipher.doFinal(messBytes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static byte[] decrypt(Key key, byte[] cipherBytes) {
        if (key != null) {
            try {
                Cipher cipher = Cipher.getInstance(ALGORITHM);
                cipher.init(Cipher.DECRYPT_MODE, key);
                return cipher.doFinal(cipherBytes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
