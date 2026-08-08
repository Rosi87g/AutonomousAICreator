package com.hackaton.aicreator;

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

    private final Map<String, Persona> agents = new ConcurrentHashMap<>();
    private final Map<String, List<Post>> feeds = new ConcurrentHashMap<>();
    private final Map<String, List<RejectedTopic>> rejections = new ConcurrentHashMap<>();

    @Autowired
    private PersistenceService persistenceService;

    @PostConstruct
    public void loadFromDisk() {
        agents.putAll(persistenceService.loadAgents());
        feeds.putAll(persistenceService.loadFeeds());
        System.out.println("Loaded " + agents.size() + " agent(s) from disk.");
    }

    public void registerAgent(String agentId, Persona persona) {
        agents.put(agentId, persona);
        feeds.put(agentId, new ArrayList<>());
        persist();
    }

    public Persona getPersona(String agentId) {
        return agents.get(agentId);
    }

    public boolean exists(String agentId) {
        return agents.containsKey(agentId);
    }

    public void addPost(String agentId, Post post) {
        feeds.get(agentId).add(0, post);
        persist();
    }

    public List<Post> getPosts(String agentId) {
        return feeds.getOrDefault(agentId, new ArrayList<>());
    }

    public Set<String> getAllAgentIds() {
        return agents.keySet();
    }

    public void addRejection(String agentId, String topic, String reason, int score) {
        rejections.computeIfAbsent(agentId, k -> new ArrayList<>())
                .add(0, new RejectedTopic(topic, reason, score, java.time.Instant.now().toString()));
        persist();
    }

    public List<RejectedTopic> getRejections(String agentId) {
        return rejections.getOrDefault(agentId, new ArrayList<>());
    }

    public long countPostsInLast24Hours(String agentId) {
        java.time.Instant cutoff = java.time.Instant.now().minus(java.time.Duration.ofHours(24));
        return feeds.getOrDefault(agentId, new ArrayList<>()).stream()
                .filter(p -> java.time.Instant.parse(p.getCreatedAt()).isAfter(cutoff))
                .count();
    }

    private void persist() {
        persistenceService.saveAgents(agents);
        persistenceService.saveFeeds(feeds);
    }
}
