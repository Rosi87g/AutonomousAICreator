package com.hackaton.aicreator;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class AgentStore {

    private final Map<String, Persona> agents
            = new ConcurrentHashMap<>();

    private final Map<String, List<Post>> feeds
            = new ConcurrentHashMap<>();

    private final Map<String, List<RejectedTopic>> rejections
            = new ConcurrentHashMap<>();

    @Autowired
    private PersistenceService persistenceService;

    // =========================================================
    // LOAD DATA WHEN APPLICATION STARTS
    // =========================================================
    @PostConstruct
    public void loadFromStorage() {

        agents.putAll(
                persistenceService.loadAgents()
        );

        feeds.putAll(
                persistenceService.loadFeeds()
        );

        rejections.putAll(
                persistenceService.loadRejections()
        );

        System.out.println(
                "Loaded "
                + agents.size()
                + " agent(s) from persistent storage."
        );

        System.out.println(
                "Loaded "
                + feeds.size()
                + " feed(s) from persistent storage."
        );

        System.out.println(
                "Loaded "
                + rejections.size()
                + " rejection history record(s)."
        );
    }

    // =========================================================
    // REGISTER AGENT
    // =========================================================
    public synchronized void registerAgent(
            String agentId,
            Persona persona) {

        agents.put(
                agentId,
                persona
        );

        // Don't erase an existing feed if the same
        // agent is registered again.
        feeds.putIfAbsent(
                agentId,
                new ArrayList<>()
        );

        rejections.putIfAbsent(
                agentId,
                new ArrayList<>()
        );

        persist();
    }

    // =========================================================
    // GET PERSONA
    // =========================================================
    public Persona getPersona(
            String agentId) {

        return agents.get(agentId);
    }

    // =========================================================
    // CHECK AGENT
    // =========================================================
    public boolean exists(
            String agentId) {

        return agents.containsKey(agentId);
    }

    // =========================================================
    // ADD POST
    // =========================================================
    public synchronized void addPost(
            String agentId,
            Post post) {

        feeds
                .computeIfAbsent(
                        agentId,
                        key -> new ArrayList<>()
                )
                .add(
                        0,
                        post
                );

        persist();
    }

    // =========================================================
    // GET POSTS
    // =========================================================
    public List<Post> getPosts(
            String agentId) {

        return feeds.getOrDefault(
                agentId,
                new ArrayList<>()
        );
    }

    // =========================================================
    // GET ALL AGENTS
    // =========================================================
    public Set<String> getAllAgentIds() {

        return agents.keySet();
    }

    // =========================================================
    // ADD REJECTION
    // =========================================================
    public synchronized void addRejection(
            String agentId,
            String topic,
            String reason,
            int score) {

        rejections
                .computeIfAbsent(
                        agentId,
                        key -> new ArrayList<>()
                )
                .add(
                        0,
                        new RejectedTopic(
                                topic,
                                reason,
                                score,
                                Instant.now().toString()
                        )
                );

        persist();
    }

    // =========================================================
    // GET REJECTIONS
    // =========================================================
    public List<RejectedTopic> getRejections(
            String agentId) {

        return rejections.getOrDefault(
                agentId,
                new ArrayList<>()
        );
    }

    // =========================================================
    // COUNT POSTS IN LAST 24 HOURS
    // =========================================================
    public long countPostsInLast24Hours(
            String agentId) {

        Instant cutoff
                = Instant.now()
                        .minus(Duration.ofHours(24));

        return feeds
                .getOrDefault(
                        agentId,
                        new ArrayList<>()
                )
                .stream()
                .filter(post
                        -> Instant.parse(
                        post.getCreatedAt()
                ).isAfter(cutoff)
                )
                .count();
    }

    // =========================================================
    // PERSIST EVERYTHING
    // =========================================================
    private synchronized void persist() {

        persistenceService.saveAgents(
                agents
        );

        persistenceService.saveFeeds(
                feeds
        );

        persistenceService.saveRejections(
                rejections
        );
    }
}
