package org.shirakawatyu.btmetadataserver.util;


public class StringUtil {
    public static String random(int len) {
        String[] source = {
                "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
                "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",
                "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"
        };
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            int index = (int) (Math.random() * source.length);
            sb.append(source[index]);
        }
        return sb.toString();
    }
}
