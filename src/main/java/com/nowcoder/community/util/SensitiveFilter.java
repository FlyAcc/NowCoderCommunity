package com.nowcoder.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(SensitiveFilter.class);

    private static final String REPLACEMENT = "*";

    private final TrieNode root = new TrieNode();

    @PostConstruct
    public void init() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String keyword;
            while ((keyword = reader.readLine()) != null) {
                addKeyWord(keyword);
            }
        } catch (IOException e) {
            LOGGER.error("加载敏感词文件失败：" + e.getMessage());
        }
    }

    private void addKeyWord(String keyword) {
        TrieNode temp = root;
        int i = 0;
        for (char c : keyword.toCharArray()) {
            TrieNode subNode = temp.getSubNode(c);
            if (subNode == null) {
                subNode = new TrieNode();
                temp.addSubNode(c, subNode);
            }

            // 指向子节点
            temp = subNode;
            if (i++ == keyword.length() - 1) {
                temp.setKeywordEnd(true);
            }
        }
    }

    /**
     * 过滤敏感词
     *
     * @param text 待过滤文本
     * @return 过滤后的文本
     */
    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return text;
        }

        TrieNode temp = root;
        int begin = 0, curr = 0;
        StringBuilder sb = new StringBuilder();
        while (curr < text.length()) {
            char c = text.charAt(curr);
            if (isSymbol(c)) {
                if (temp == root) {
                    sb.append(c);
                    begin++;
                }
                curr++;
                continue;
            }

            temp = temp.getSubNode(c);
            if (temp == null) {
                sb.append(text.charAt(begin));
                curr = ++begin;
                temp = root;
            } else if (temp.isKeywordEnd()) {
                while (begin <= curr) {
                    sb.append(REPLACEMENT);
                    begin++;
                }
                curr = begin;
            } else {
                curr++;
            }
        }

        return sb.append(text.substring(begin)).toString();
    }

    private boolean isSymbol(char c) {
        // 0x2E80~0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    /**
     * 前缀树
     */
    private static class TrieNode {
        // 关键词结束标志
        private boolean keywordEnd;

        // 子节点
        private final Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return keywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            this.keywordEnd = keywordEnd;
        }

        public TrieNode getSubNode(char c) {
            return subNodes.get(c);
        }

        public void addSubNode(Character c, TrieNode child) {
            this.subNodes.put(c, child);
        }
    }
}
