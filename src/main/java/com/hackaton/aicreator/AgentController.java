package com.hackaton.aicreator;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agent")
public class AgentController {

    @Autowired
    private AgentStore agentStore;

    @PostMapping("/init")
    public InitResponse init(@RequestBody InitRequest request) {
        String agentId = UUID.randomUUID().toString();
        agentStore.registerAgent(agentId, request.getPersona());

        System.out.println("Agent initialized: " + agentId
                + " | Persona: " + request.getPersona().getName()
                + " | Domain: " + request.getPersona().getDomain());

        return new InitResponse(agentId);
    }

    @GetMapping("/feed")
    public FeedResponse feed(@RequestParam String agentId) {
        List<Post> posts = agentStore.getPosts(agentId);
        return new FeedResponse(posts);
    }

    @GetMapping("/rejected")
    public List<RejectedTopic> rejected(@RequestParam String agentId) {
        return agentStore.getRejections(agentId);
    }
}
