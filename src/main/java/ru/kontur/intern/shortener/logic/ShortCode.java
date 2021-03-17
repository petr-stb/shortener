package ru.kontur.intern.shortener.logic;

import java.nio.ByteBuffer;
import java.time.Instant;
import org.apache.commons.codec.binary.Base64;

public class ShortCode {

    private static int count;
    private static final int FACTOR = 1000000;

    private final String code;

    public ShortCode() {
        final long number = Instant.now().toEpochMilli() * FACTOR + count;
        count++;
        final byte[] bytes = ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(number).array();
        final Base64 base64 = new Base64();
        code = base64.encodeAsString(bytes)
                .replace("=", "")
                .replace("+", "_")
                .replace("/", "-");
    }

    @Override
    public String toString() {
        return code;
    }
}
