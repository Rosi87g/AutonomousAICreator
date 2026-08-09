package com.hackaton.aicreator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class MemoryService {

    private final Map<String, List<Set<String>>> memory
            = new ConcurrentHashMap<>();

    private final PersistenceService persistenceService;

    private static final Set<String> STOP_WORDS = Set.of(
            "the", "a", "an", "and", "or", "of", "to", "for",
            "with", "in", "on", "at", "by", "from", "is",
            "are", "was", "were", "new", "latest", "launches",
            "launch", "announces", "announced", "introduces",
            "introduced", "today", "after", "before", "its",
            "their", "this", "that"
    );

    // =========================================================
    // CONSTRUCTOR
    // =========================================================
    public MemoryService(
            PersistenceService persistenceService) {

        this.persistenceService
                = persistenceService;

        Map<String, List<Set<String>>> storedMemory
                = persistenceService.loadMemory();

        memory.putAll(
                storedMemory
        );

        System.out.println(
                "Loaded memory for "
                + memory.size()
                + " agent(s) from persistent storage."
        );
    }

    // =========================================================
    // CHECK WHETHER TOPIC WAS ALREADY SEEN
    // =========================================================
    public boolean hasSeenTopic(
            String agentId,
            String topic) {

        Set<String> newKeywords
                = extractKeywords(topic);

        if (newKeywords.isEmpty()) {
            return false;
        }

        List<Set<String>> previousTopics
                = memory.getOrDefault(
                        agentId,
                        List.of()
                );

        for (Set<String> previousKeywords
                : previousTopics) {

            // Exact keyword match
            if (previousKeywords.equals(
                    newKeywords)) {

                return true;
            }

            // Calculate intersection
            int intersection = 0;

            for (String keyword
                    : newKeywords) {

                if (previousKeywords.contains(
                        keyword)) {

                    intersection++;
                }
            }

            // Calculate union
            Set<String> union
                    = new HashSet<>(
                            newKeywords
                    );

            union.addAll(
                    previousKeywords
            );

            if (!union.isEmpty()) {

                double similarity
                        = (double) intersection
                        / union.size();

                /*
                 * 75% similarity means we consider
                 * the topic substantially the same.
                 */
                if (similarity >= 0.75) {
                    return true;
                }
            }
        }

        return false;
    }

    // =========================================================
    // REMEMBER TOPIC
    // =========================================================
    public synchronized void rememberTopic(
            String agentId,
            String topic) {

        Set<String> keywords
                = extractKeywords(topic);

        if (keywords.isEmpty()) {
            return;
        }

        memory
                .computeIfAbsent(
                        agentId,
                        key -> new ArrayList<>()
                )
                .add(
                        new HashSet<>(
                                keywords
                        )
                );

        // Save updated memory to Redis
        persistenceService.saveMemory(
                memory
        );
    }

    // =========================================================
    // GET MEMORY
    // =========================================================
    public List<Set<String>> getMemory(
            String agentId) {

        return memory.getOrDefault(
                agentId,
                List.of()
        );
    }

    // =========================================================
    // KEYWORD EXTRACTION
    // =========================================================
    private Set<String> extractKeywords(
            String text) {

        Set<String> keywords
                = new HashSet<>();

        if (text == null
                || text.isBlank()) {

            return keywords;
        }

        String cleaned
                = text
                        .toLowerCase()
                        .replaceAll(
                                "[^a-z0-9 ]",
                                " "
                        );

        for (String word
                : cleaned.split("\\s+")) {

            if (word.length() < 3) {
                continue;
            }

            if (STOP_WORDS.contains(word)) {
                continue;
            }

            keywords.add(word);
        }

        return keywords;
    }
}
