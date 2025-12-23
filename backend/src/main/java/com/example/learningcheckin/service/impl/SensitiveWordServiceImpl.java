package com.example.learningcheckin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.learningcheckin.entity.Blacklist;
import com.example.learningcheckin.entity.SysLog;
import com.example.learningcheckin.mapper.BlacklistMapper;
import com.example.learningcheckin.mapper.SysLogMapper;
import com.example.learningcheckin.service.ISensitiveWordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class SensitiveWordServiceImpl implements ISensitiveWordService {

    @Autowired
    private BlacklistMapper blacklistMapper;

    @Autowired
    private SysLogMapper sysLogMapper;

    private TrieNode root = new TrieNode();

    private static class TrieNode {
        boolean isEnd = false;
        Map<Character, TrieNode> subNodes = new HashMap<>();

        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }
    }

    @PostConstruct
    public void init() {
        reload();
    }

    @Override
    public synchronized void reload() {
        log.info("Reloading sensitive words...");
        root = new TrieNode();
        List<Blacklist> list = blacklistMapper.selectList(new LambdaQueryWrapper<Blacklist>()
                .eq(Blacklist::getType, "SENSITIVE_WORD"));
        
        int count = 0;
        for (Blacklist item : list) {
            if (item.getValue() != null && !item.getValue().trim().isEmpty()) {
                addWord(item.getValue().trim());
                count++;
            }
        }
        log.info("Loaded {} sensitive words.", count);
    }

    private void addWord(String word) {
        TrieNode tempNode = root;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);
            if (subNode == null) {
                subNode = new TrieNode();
                tempNode.addSubNode(c, subNode);
            }
            tempNode = subNode;
            if (i == word.length() - 1) {
                tempNode.isEnd = true;
            }
        }
    }

    @Override
    public boolean containsSensitive(String text) {
        if (text == null || text.isEmpty()) return false;
        return findFirstSensitive(text) != null;
    }

    @Override
    public String findFirstSensitive(String text) {
        if (text == null || text.isEmpty()) return null;

        for (int i = 0; i < text.length(); i++) {
            int length = checkSensitiveWord(text, i);
            if (length > 0) {
                return text.substring(i, i + length);
            }
        }
        return null;
    }

    @Override
    public Set<String> findAllSensitive(String text) {
        Set<String> sensitiveWords = new HashSet<>();
        if (text == null || text.isEmpty()) return sensitiveWords;

        for (int i = 0; i < text.length(); i++) {
            int length = checkSensitiveWord(text, i);
            if (length > 0) {
                sensitiveWords.add(text.substring(i, i + length));
                i = i + length - 1; // Skip matched word
            }
        }
        return sensitiveWords;
    }

    @Override
    public String filterSensitive(String text, String replacement) {
        if (text == null || text.isEmpty()) return text;
        StringBuilder result = new StringBuilder(text);
        
        int i = 0;
        while (i < result.length()) {
            int length = checkSensitiveWord(result.toString(), i);
            if (length > 0) {
                for (int j = 0; j < length; j++) {
                    result.setCharAt(i + j, replacement.charAt(0));
                }
                i = i + length;
            } else {
                i++;
            }
        }
        return result.toString();
    }

    @Override
    public void logSensitiveHit(Long userId, String content, Set<String> words, String sourceType, Long sourceId) {
        if (words == null || words.isEmpty()) return;

        SysLog log = new SysLog();
        log.setLogType("SENSITIVE");
        log.setUserId(userId);
        log.setModule("内容审核");
        log.setAction("触发敏感词: " + sourceType + "(" + sourceId + ")");
        
        // Truncate content if too long
        String snippet = content.length() > 500 ? content.substring(0, 500) + "..." : content;
        String extraInfo = String.format("{\"words\":\"%s\", \"sourceType\":\"%s\", \"sourceId\":%d, \"content\":\"%s\"}", 
                String.join(",", words), sourceType, sourceId, snippet.replace("\"", "\\\""));
        
        log.setContent("Detected: " + String.join(",", words));
        log.setExtraInfo(extraInfo);
        log.setCreateTime(LocalDateTime.now());
        
        sysLogMapper.insert(log);
    }

    @Override
    @Transactional
    public void importWords(List<String> words) {
        if (words == null || words.isEmpty()) return;
        
        int added = 0;
        for (String word : words) {
            if (word == null || word.trim().isEmpty()) continue;
            String w = word.trim();
            
            // Check if exists
            Long count = blacklistMapper.selectCount(new LambdaQueryWrapper<Blacklist>()
                    .eq(Blacklist::getType, "SENSITIVE_WORD")
                    .eq(Blacklist::getValue, w));
            
            if (count == 0) {
                Blacklist b = new Blacklist();
                b.setType("SENSITIVE_WORD");
                b.setValue(w);
                b.setReason("Imported");
                b.setCreateTime(LocalDateTime.now());
                blacklistMapper.insert(b);
                added++;
            }
        }
        if (added > 0) {
            reload();
        }
        log.info("Imported {} new sensitive words.", added);
    }

    private int checkSensitiveWord(String text, int beginIndex) {
        TrieNode tempNode = root;
        int maxMatchLen = 0;

        for (int i = beginIndex; i < text.length(); i++) {
            char c = text.charAt(i);
            tempNode = tempNode.getSubNode(c);

            if (tempNode == null) {
                break;
            }

            if (tempNode.isEnd) {
                maxMatchLen = i - beginIndex + 1;
            }
        }

        return maxMatchLen;
    }
}
