package com.heartape.whisper.common;

import com.hankcs.algorithm.AhoCorasickDoubleArrayTrie;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class SensitiveWordFilter {

    private final AhoCorasickDoubleArrayTrie<Byte> trie = new AhoCorasickDoubleArrayTrie<>();

    @PostConstruct
    private void init() {
        // 模拟从数据库加载敏感词
        TreeMap<String, Byte> map = new TreeMap<>();
        map.put("暴力", (byte) 1);
        map.put("恐怖", (byte) 1);
        map.put("赌博", (byte) 1);
        map.put("傻X", (byte) 1);
        trie.build(map);
    }

    /**
     * 检查文字中是否包含敏感字符
     */
    public boolean isContained(String text) {
        return trie.matches(text);
    }

    /**
     * 替换敏感字字符
     */
    public String replace(String text, char replaceChar) {
        if (text == null || text.isEmpty()) return text;

        char[] result = text.toCharArray();
        trie.parseText(text, (begin, end, value) -> {
            for (int i = begin; i < end; i++) {
                result[i] = replaceChar;
            }
        });
        return new String(result);
    }


}
