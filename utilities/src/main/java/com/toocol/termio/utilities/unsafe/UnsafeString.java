package com.toocol.termio.utilities.unsafe;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/27 21:14
 * @version: 0.0.1
 */
@SuppressWarnings("all")
public class UnsafeString {
    public static final Unsafe UNSAFE;
    public static final long STRING_VALUE_FIELD_OFFSET;
    public static final long STRING_OFFSET_FIELD_OFFSET;
    public static final long STRING_COUNT_FIELD_OFFSET;
    public static final boolean ENABLED;

    private static final boolean WRITE_TO_FINAL_FIELDS = true;
    private static final boolean DISABLE = false;

    private static Unsafe loadUnsafe() {
        try {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            return (Unsafe) unsafeField.get(null);

        } catch (Exception e) {
            return null;
        }
    }

    static {
        UNSAFE = DISABLE ? null : loadUnsafe();
        ENABLED = UNSAFE != null;
    }

    private static long getFieldOffset(String fieldName) {
        if (ENABLED) {
            try {
                return UNSAFE.objectFieldOffset(String.class.getDeclaredField(fieldName));
            } catch (NoSuchFieldException e) {
                // field undefined
            }
        }
        return -1L;
    }

    static {
        STRING_VALUE_FIELD_OFFSET = getFieldOffset("value");
        STRING_OFFSET_FIELD_OFFSET = getFieldOffset("offset");
        STRING_COUNT_FIELD_OFFSET = getFieldOffset("count");
    }

    private enum StringImplementation {
        DIRECT_CHARS {
            @Override
            public byte[] toBytes(String string) {
                return (byte[]) UNSAFE.getObject(string, STRING_VALUE_FIELD_OFFSET);
            }

            @Override
            public String noCopyStringFromBytes(byte[] bytes) {
                if (WRITE_TO_FINAL_FIELDS) {
                    String string = new String();
                    UNSAFE.putObject(string, STRING_VALUE_FIELD_OFFSET, bytes);
                    return string;
                } else {
                    return new String(bytes);
                }
            }
        },
        OFFSET {
            @Override
            public byte[] toBytes(String string) {
                byte[] value = (byte[]) UNSAFE.getObject(string, STRING_VALUE_FIELD_OFFSET);
                int offset = UNSAFE.getInt(string, STRING_OFFSET_FIELD_OFFSET);
                int count = UNSAFE.getInt(string, STRING_COUNT_FIELD_OFFSET);
                if (offset == 0 && count == value.length)
                    // no need to copy
                    return value;
                else
                    return string.getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public String noCopyStringFromBytes(byte[] bytes) {
                if (WRITE_TO_FINAL_FIELDS) {
                    String string = new String();
                    UNSAFE.putObject(string, STRING_VALUE_FIELD_OFFSET, bytes);
                    UNSAFE.putInt(string, STRING_COUNT_FIELD_OFFSET, bytes.length);
                    return string;
                } else {
                    return new String(bytes, StandardCharsets.UTF_8);
                }
            }
        },
        UNKNOWN {
            @Override
            public byte[] toBytes(String string) {
                return string.getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public String noCopyStringFromBytes(byte[] bytes) {
                return new String(bytes, StandardCharsets.UTF_8);
            }
        };

        public abstract byte[] toBytes(String string);
        public abstract String noCopyStringFromBytes(byte[] bytes);
    }

    public static StringImplementation STRING_IMPLEMENTATION = computeStringImplementation();

    private static StringImplementation computeStringImplementation() {

        if (STRING_VALUE_FIELD_OFFSET != -1L) {
            if (STRING_OFFSET_FIELD_OFFSET != -1L && STRING_COUNT_FIELD_OFFSET != -1L) {
                return StringImplementation.OFFSET;

            } else if (STRING_OFFSET_FIELD_OFFSET == -1L && STRING_COUNT_FIELD_OFFSET == -1L) {
                return StringImplementation.DIRECT_CHARS;
            } else {
                return StringImplementation.UNKNOWN;
            }
        } else {
            return StringImplementation.UNKNOWN;
        }
    }

    public static boolean hasUnsafe() {
        return ENABLED;
    }

    public static final char [] EMPTY_CHARS = new char[0];
    public static final String EMPTY_STRING = "";

    public static byte[] toBytes(final String string) {
        if (string == null) return new byte[0];
        return STRING_IMPLEMENTATION.toBytes(string);

    }

    public static byte[] toBytesNoCheck(final CharSequence charSequence) {
        return toBytes(charSequence.toString());
    }

    public static byte[] toBytes(final CharSequence charSequence) {
        if (charSequence == null) return new byte[0];
        return toBytes(charSequence.toString());
    }

    public static String noCopyStringFromBytes(final byte[] bytes) {
        if (bytes==null) return EMPTY_STRING;
        return STRING_IMPLEMENTATION.noCopyStringFromBytes(bytes);
    }


    public static String noCopyStringFromBytesNoCheck(final byte[] bytes) {
        return STRING_IMPLEMENTATION.noCopyStringFromBytes(bytes);
    }
}
