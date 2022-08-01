package com.toocol.termio.utilities.utils;

import java.io.ByteArrayOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.nio.charset.Charset;

/**
 * URL decode, the content is: application/x-www-form-urlencoded。
 *
 * <pre>
 * 1. trans %20 to blank space;
 * 2. trans "%xy" to text,xy is two bit Hexadecimal value;
 * 3. skip unformed %
 * </pre>
 *
 * @author looly
 */
public class URLDecoder implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private static final byte ESCAPE_CHAR = '%';

    public static String decode(String str, Charset charset) {
        return StrUtil.str(decode(StrUtil.bytes(str, charset)), charset);
    }

    public static byte[] decode(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(bytes.length);
        int b;
        for (int i = 0; i < bytes.length; i++) {
            b = bytes[i];
            if (b == '+') {
                buffer.write(CharUtil.SPACE);
            } else if (b == ESCAPE_CHAR) {
                if (i + 1 < bytes.length) {
                    final int u = CharUtil.digit16(bytes[i + 1]);
                    if (u >= 0 && i + 2 < bytes.length) {
                        final int l = CharUtil.digit16(bytes[i + 2]);
                        if (l >= 0) {
                            buffer.write((char) ((u << 4) + l));
                            i += 2;
                            continue;
                        }
                    }
                }
                buffer.write(b);
            } else {
                buffer.write(b);
            }
        }
        return buffer.toByteArray();
    }
}
