package org.shirakawatyu.btmetadataserver.bittorrent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BencodeParser {
    private int position = 0;
    private final byte[] data;

    public BencodeParser(byte[] data) {
        this.data = data;
    }

    /**
     * 解析Bencode编码的数据
     *
     * @return 解析后的Object对象（可能是String、Long、List或Map）
     */
    public Object parse() throws IOException {
        if (position >= data.length) {
            throw new IOException("Unexpected end of data");
        }

        char currentChar = (char) data[position];

        // 根据当前字符确定解析方式
        return switch (currentChar) {
            case 'i' ->  // 整数: i<数字>e
                    parseInteger();
            case 'l' ->  // 列表: l<元素>e
                    parseList();
            case 'd' ->  // 字典: d<键><值>e
                    parseDictionary();
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' ->
                // 字符串: <长度>:<内容>
                    parseString();
            default -> throw new IOException("Unknown token: " + currentChar + " at position " + position);
        };
    }

    /**
     * 解析整数
     */
    private Long parseInteger() throws IOException {
        // 跳过'i'
        position++;

        int start = position;
        while (position < data.length && data[position] != 'e') {
            position++;
        }

        if (position >= data.length) {
            throw new IOException("Unexpected end of integer");
        }

        // 跳过'e'
        position++;

        // 提取整数值
        String numStr = new String(data, start, position - start - 1);
        return Long.parseLong(numStr);
    }

    /**
     * 解析字符串
     */
    private String parseString() throws IOException {
        // 查找':'的位置
        int colonPos = position;
        while (colonPos < data.length && data[colonPos] != ':') {
            colonPos++;
        }

        if (colonPos >= data.length) {
            throw new IOException("String length delimiter ':' not found");
        }

        // 提取字符串长度
        String lengthStr = new String(data, position, colonPos - position);
        int length = Integer.parseInt(lengthStr);

        // 更新位置到字符串内容的开头
        position = colonPos + 1;

        // 确认有足够的字符
        if (position + length > data.length) {
            throw new IOException("String data exceeds available bytes");
        }

        // 提取字符串内容
        String result = new String(data, position, length);
        position += length;

        return result;
    }

    /**
     * 解析列表
     */
    private List<Object> parseList() throws IOException {
        // 跳过'l'
        position++;

        List<Object> list = new ArrayList<>();

        // 持续解析直到遇到'e'
        while (position < data.length && data[position] != 'e') {
            list.add(parse());
        }

        if (position >= data.length) {
            throw new IOException("Unexpected end of list");
        }

        // 跳过'e'
        position++;

        return list;
    }

    /**
     * 解析字典
     */
    private Map<String, Object> parseDictionary() throws IOException {
        // 跳过'd'
        position++;

        Map<String, Object> dict = new LinkedHashMap<>();  // 使用LinkedHashMap保持顺序

        // 持续解析直到遇到'e'
        while (position < data.length && data[position] != 'e') {
            // 键必须是字符串
            String key = (String) parse();
            Object value = parse();
            dict.put(key, value);
        }

        if (position >= data.length) {
            throw new IOException("Unexpected end of dictionary");
        }

        // 跳过'e'
        position++;

        return dict;
    }
}