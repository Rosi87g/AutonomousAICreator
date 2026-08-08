package com.hackaton.aicreator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class PersistenceService {

    private static final String DATA_DIR = "data";

    private static final String AGENTS_FILE
            = DATA_DIR + "/agents.json";

    private static final String FEEDS_FILE
            = DATA_DIR + "/feeds.json";

    private static final String MEMORY_FILE
            = DATA_DIR + "/memory.json";

    private final ObjectMapper mapper;

    public PersistenceService() {

        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);

        new File(DATA_DIR).mkdirs();
    }

    // =========================================================
    // AGENTS
    // =========================================================
    public synchronized void saveAgents(
            Map<String, Persona> agents) {

        try {

            mapper.writeValue(
                    new File(AGENTS_FILE),
                    agents
            );

        } catch (IOException e) {

            System.out.println(
                    "Failed to save agents: "
                    + e.getMessage()
            );
        }
    }

    public synchronized Map<String, Persona> loadAgents() {

        File file = new File(AGENTS_FILE);

        if (!file.exists()) {
            return new HashMap<>();
        }

        try {

            var typeFactory = mapper.getTypeFactory();

            var mapType = typeFactory.constructMapType(
                    HashMap.class,
                    typeFactory.constructType(String.class),
                    typeFactory.constructType(Persona.class)
            );

            return mapper.readValue(file, mapType);

        } catch (IOException e) {

            System.out.println(
                    "Failed to load agents: "
                    + e.getMessage()
            );

            return new HashMap<>();
        }
    }

    // =========================================================
    // FEEDS
    // ======== ================================================
    public synchronized void saveFeeds(
            Map<String, List<Post>> feeds) {

        try {

            mapper.writeValue(
                    new File(FEEDS_FILE),
                    feeds
            );

        } catch (IOException e) {

            System.out.println(
                    "Failed to save feeds: "
                    + e.getMessage()
            );
        }
    }

    public synchronized Map<String, List<Post>> loadFeeds() {

        File file = new File(FEEDS_FILE);

        if (!file.exists()) {
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

            return mapper.readValue(file, mapType);

        } catch (IOException e) {

            System.out.println(
                    "Failed to load feeds: "
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

        try {

            mapper.writeValue(
                    new File(MEMORY_FILE),
                    memory
            );

        } catch (IOException e) {

            System.out.println(
                    "Failed to save memory: "
                    + e.getMessage()
            );
        }
    }

    public synchronized Map<String, List<Set<String>>> loadMemory() {

        File file = new File(MEMORY_FILE);

        if (!file.exists()) {
            return new HashMap<>();
        }

        try {

            var typeFactory = mapper.getTypeFactory();

            var topicListType
                    = typeFactory.constructCollectionType(
                            List.class,
                            typeFactory.constructCollectionType(
                                    Set.class,
                                    String.class
                            )
                    );

            var mapType
                    = typeFactory.constructMapType(
                            HashMap.class,
                            typeFactory.constructType(String.class),
                            topicListType
                    );

            return mapper.readValue(file, mapType);

        } catch (IOException e) {

            System.out.println(
                    "Failed to load memory: "
                    + e.getMessage()
            );

            return new HashMap<>();
        }
    }
}
