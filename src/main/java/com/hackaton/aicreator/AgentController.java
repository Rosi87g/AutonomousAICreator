package com.hackaton.aicreator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/agent")
public class AgentController {

    @Autowired
    private AgentStore agentStore;

    @Autowired
    private SchedulerService schedulerService;

    @PostMapping("/init")
    public InitResponse init(@RequestBody InitRequest request) {

        if (request.getPersona() == null
                || request.getPersona().getName() == null || request.getPersona().getName().isBlank()
                || request.getPersona().getDomain() == null || request.getPersona().getDomain().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Persona name and domain are required.");
        }

        request.getPersona().setCreatedAt(java.time.Instant.now().toString());

        String agentId = UUID.randomUUID().toString();
        agentStore.registerAgent(agentId, request.getPersona());

        System.out.println("Agent initialized: " + agentId
                + " | Persona: " + request.getPersona().getName()
                + " | Domain: " + request.getPersona().getDomain());

        // Generate an immediate first post so the feed isn't empty right after init
        schedulerService.runCycleForAgent(agentId);

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

    @GetMapping("/stats")
    public java.util.Map<String, Object> stats(@RequestParam String agentId) {
        int posts = agentStore.getPosts(agentId).size();
        int rejected = agentStore.getRejections(agentId).size();
        int total = posts + rejected;

        java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("totalPosts", posts);
        result.put("totalRejected", rejected);
        result.put("approvalRate", total == 0 ? 0 : Math.round((posts * 100.0) / total));
        return result;
    }

    @GetMapping("/list")
    public List<AgentSummary> listAgents() {
        List<AgentSummary> summaries = new ArrayList<>();

        for (String agentId : agentStore.getAllAgentIds()) {
            Persona persona = agentStore.getPersona(agentId);
            summaries.add(new AgentSummary(
                    agentId,
                    persona.getName(),
                    persona.getDomain(),
                    persona.getCreatedAt()
            ));
        }

        return summaries;
    }
}
