package com.hackaton.aicreator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Service
public class PersistenceService {

    private static final String AGENTS_KEY = "aicreator:agents";
    private static final String FEEDS_KEY = "aicreator:feeds";
    private static final String MEMORY_KEY = "aicreator:memory";
    private static final String REJECTIONS_KEY = "aicreator:rejections";

    private final ObjectMapper mapper;
    private final RestTemplate restTemplate;

    private final String redisUrl;
    private final String redisToken;

    public PersistenceService(
            @Value("${upstash.redis.url}") String redisUrl,
            @Value("${upstash.redis.token}") String redisToken) {

        this.redisUrl = redisUrl;
        this.redisToken = redisToken;

        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);

        this.restTemplate = new RestTemplate();
    }

    // =========================================================
    // AGENTS
    // =========================================================
    public synchronized void saveAgents(
            Map<String, Persona> agents) {

        save(AGENTS_KEY, agents);
    }

    public synchronized Map<String, Persona> loadAgents() {

        String json = get(AGENTS_KEY);

        if (json == null || json.isBlank()) {
            return new HashMap<>();
        }

        try {

            var typeFactory = mapper.getTypeFactory();

            var mapType = typeFactory.constructMapType(
                    HashMap.class,
                    typeFactory.constructType(String.class),
                    typeFactory.constructType(Persona.class)
            );

            return mapper.readValue(json, mapType);

        } catch (Exception e) {

            System.out.println(
                    "Failed to load agents from Redis: "
                    + e.getMessage()
            );

            return new HashMap<>();
        }
    }

    // =========================================================
    // FEEDS
    // =========================================================
    public synchronized void saveFeeds(
            Map<String, List<Post>> feeds) {

        save(FEEDS_KEY, feeds);
    }

    public synchronized Map<String, List<Post>> loadFeeds() {

        String json = get(FEEDS_KEY);

        if (json == null || json.isBlank()) {
            return new HashMap<>();
        }

        try {

            var typeFactory = mapper.getTypeFactory();

            var postListType
                    = typeFactory.constructCollectionType(
                            List.class,
                            Post.class
                    );

            var mapType
                    = typeFactory.constructMapType(
                            HashMap.class,
                            typeFactory.constructType(String.class),
                            postListType
                    );

            return mapper.readValue(json, mapType);

        } catch (Exception e) {

            System.out.println(
                    "Failed to load feeds from Redis: "
                    + e.getMessage()
            );

            return new HashMap<>();
        }
    }

    // =========================================================
    // MEMORY
    // =========================================================
    public synchronized void saveMemory(
            Map<String, List<Set<String>>> memory) {

        save(MEMORY_KEY, memory);
    }

    public synchronized Map<String, List<Set<String>>> loadMemory() {

        String json = get(MEMORY_KEY);

        if (json == null || json.isBlank()) {
            return new HashMap<>();
        }

        try {

            var typeFactory = mapper.getTypeFactory();

            var setType
                    = typeFactory.constructCollectionType(
                            Set.class,
                            String.class
                    );

            var topicListType
                    = typeFactory.constructCollectionType(
                            List.class,
                            setType
                    );

            var mapType
                    = typeFactory.constructMapType(
                            HashMap.class,
                            typeFactory.constructType(String.class),
                            topicListType
                    );

            return mapper.readValue(json, mapType);

        } catch (Exception e) {

            System.out.println(
                    "Failed to load memory from Redis: "
                    + e.getMessage()
            );

            return new HashMap<>();
        }
    }

    // =========================================================
    // REJECTIONS
    // =========================================================
    public synchronized void saveRejections(
            Map<String, List<RejectedTopic>> rejections) {

        save(REJECTIONS_KEY, rejections);
    }

    public synchronized Map<String, List<RejectedTopic>> loadRejections() {

        String json = get(REJECTIONS_KEY);

        if (json == null || json.isBlank()) {
            return new HashMap<>();
        }

        try {

            var typeFactory = mapper.getTypeFactory();

            var rejectionListType
                    = typeFactory.constructCollectionType(
                            List.class,
                            RejectedTopic.class
                    );

            var mapType
                    = typeFactory.constructMapType(
                            HashMap.class,
                            typeFactory.constructType(String.class),
                            rejectionListType
                    );

            return mapper.readValue(json, mapType);

        } catch (Exception e) {

            System.out.println(
                    "Failed to load rejections from Redis: "
                    + e.getMessage()
            );

            return new HashMap<>();
        }
    }

    // =========================================================
    // REDIS SET
    // =========================================================
    private synchronized void save(
            String key,
            Object value) {

        try {

            String json = mapper.writeValueAsString(value);

            executeRedisCommand(
                    List.of(
                            "SET",
                            key,
                            json
                    )
            );

        } catch (Exception e) {

            System.out.println(
                    "Failed to save data to Redis ["
                    + key
                    + "]: "
                    + e.getMessage()
            );
        }
    }

    // =========================================================
    // REDIS GET
    // =========================================================
    private synchronized String get(
            String key) {

        try {

            Map<String, Object> response
                    = executeRedisCommand(
                            List.of(
                                    "GET",
                                    key
                            )
                    );

            if (response == null) {
                return null;
            }

            Object result = response.get("result");

            if (result == null) {
                return null;
            }

            return result.toString();

        } catch (Exception e) {

            System.out.println(
                    "Failed to load data from Redis ["
                    + key
                    + "]: "
                    + e.getMessage()
            );

            return null;
        }
    }

    // =========================================================
    // REDIS HTTP REQUEST
    // =========================================================
    @SuppressWarnings("unchecked")
    private Map<String, Object> executeRedisCommand(
            List<String> command) {

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(
                MediaType.APPLICATION_JSON
        );

        headers.setBearerAuth(redisToken);

        HttpEntity<List<String>> request
                = new HttpEntity<>(
                        command,
                        headers
                );

        ResponseEntity<Map> response
                = restTemplate.postForEntity(
                        redisUrl,
                        request,
                        Map.class
                );

        return response.getBody();
    }
}
