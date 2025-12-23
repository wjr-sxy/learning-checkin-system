package com.example.learningcheckin.service;

import java.util.List;
import java.util.Set;

public interface ISensitiveWordService {
    
    /**
     * Initialize or Reload sensitive words from database
     */
    void reload();

    /**
     * Check if text contains any sensitive word
     * @param text
     * @return true if contains sensitive word
     */
    boolean containsSensitive(String text);

    /**
     * Find first sensitive word in text
     * @param text
     * @return the word found, or null
     */
    String findFirstSensitive(String text);

    /**
     * Find all sensitive words in text
     * @param text
     * @return list of words found
     */
    Set<String> findAllSensitive(String text);

    /**
     * Replace sensitive words with replacement char
     * @param text
     * @param replacement
     * @return filtered text
     */
    String filterSensitive(String text, String replacement);

    /**
     * Log sensitive word hit
     */
    void logSensitiveHit(Long userId, String content, java.util.Set<String> words, String sourceType, Long sourceId);

    /**
     * Import sensitive words from list
     */
    void importWords(List<String> words);
}
