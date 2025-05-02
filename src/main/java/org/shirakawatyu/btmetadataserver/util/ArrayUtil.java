package org.shirakawatyu.btmetadataserver.util;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
public class ArrayUtil {
    public static byte[] concatBytes(byte[] ...b) {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            for (byte[] bytes : b) {
                stream.writeBytes(bytes);
            }
            return stream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
