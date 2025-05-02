package org.shirakawatyu.btmetadataserver.util;


public class EncodeUtil {
    public static String hexToURLEncode(String hex) {
        StringBuilder encoded = new StringBuilder();
        for (int i = 0; i < hex.length(); i += 2) {
            String h = hex.substring(i, Math.min(i + 2, hex.length()));
            encoded.append("%").append(h);
        }
        return encoded.toString();
    }

    public static byte[] hexToBytes(String hex) {
        int length = hex.length();
        byte[] bytes = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            String subStr = hex.substring(i, Math.min(i + 2, hex.length()));
            bytes[i / 2] = (byte) Integer.parseInt(subStr, 16);
        }
        return bytes;
    }
}
