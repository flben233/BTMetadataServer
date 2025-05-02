package org.shirakawatyu.btmetadataserver.util;

public class BinaryUtil {
    public static int bytesToInt(byte[] bytes) {
        int result = 0;
        for (byte aByte : bytes) {
            result <<= 8;
            result |= (aByte & 0xFF);
        }
        return result;
    }

    public static byte[] intToBytes(int i) {
        byte[] bytes = new byte[4];
        for (int j = 0; j < 4; j++) {
            bytes[j] = (byte) ((i >> (24 - j * 8)) & 0xFF);
        }
        return bytes;
    }

    public static byte[] ipToBytes(String ip) {
        String[] parts = ip.split("\\.");
        byte[] bytes = new byte[4];
        for (int i = 0; i < 4; i++) {
            bytes[i] = (byte) Integer.parseInt(parts[i]);
        }
        return bytes;
    }
}
