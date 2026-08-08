package com.hackaton.aicreator;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SchedulerService {

    @Autowired
    private AgentStore agentStore;

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private NewsService newsService;

    @Autowired
    private MemoryService memoryService;

    @Autowired
    private DecisionEngine decisionEngine;

    @Value("${agent.max.posts.per.day}")
    private int maxPostsPerDay;

    @Scheduled(fixedRateString = "${scheduler.interval.ms}")
    public void publishCycle() {

        for (String agentId : agentStore.getAllAgentIds()) {

            try {
                Persona persona = agentStore.getPersona(agentId);

                long postsToday = agentStore.countPostsInLast24Hours(agentId);
                if (postsToday >= maxPostsPerDay) {
                    System.out.println("""
==================================================
AUTONOMOUS AGENT
Agent   : %s
Status  : DAILY POST LIMIT REACHED (%d/%d)
Action  : Skipping cycle until next day.
==================================================
""".formatted(persona.getName(), postsToday, maxPostsPerDay));
                    continue;
                }

                List<NewsItem> newsList = newsService.getLatestNews(persona);

                NewsItem selectedNews = null;
                Decision selectedDecision = null;

                for (NewsItem news : newsList) {

                    if (memoryService.hasSeenTopic(agentId, news.getTitle())) {
                        continue;
                    }

                    Decision decision = decisionEngine.evaluate(persona, news);

                    if (!decision.isApproved()) {
                        agentStore.addRejection(agentId, news.getTitle(), decision.getReason(), decision.getScore());

                        System.out.println("""
==================================================
EDITORIAL REJECTION
Agent   : %s
Topic   : %s
Reason  : %s
Score   : %d
==================================================
""".formatted(persona.getName(), news.getTitle(), decision.getReason(), decision.getScore()));

                        continue;
                    }

                    if (selectedDecision == null || decision.getScore() > selectedDecision.getScore()) {
                        selectedNews = news;
                        selectedDecision = decision;
                    }
                }

                if (selectedNews == null) {
                    System.out.println("""
==================================================
AUTONOMOUS AGENT
Agent   : %s
Status  : NO APPROVED TOPICS THIS CYCLE
Action  : Waiting for next cycle.
==================================================
""".formatted(persona.getName()));
                    continue;
                }

                List<Post> recentPosts = agentStore.getPosts(agentId);
                AIResponse ai = geminiService.generatePost(persona, selectedNews, recentPosts);

                if ("AI service is temporarily busy.".equals(ai.getPost())) {
                    System.out.println("""
==================================================
AUTONOMOUS AGENT
Agent   : %s
Status  : GEMINI TEMPORARILY UNAVAILABLE
Action  : Scheduler will retry next cycle.
==================================================
""".formatted(persona.getName()));
                    continue;
                }

                Post newPost = new Post(
                        UUID.randomUUID().toString(),
                        Instant.now().toString(),
                        ai.getPost(),
                        ai.getRationale(),
                        ai.getSources()
                );

                agentStore.addPost(agentId, newPost);
                memoryService.rememberTopic(agentId, selectedNews.getTitle());

                System.out.println("""
==================================================
AUTONOMOUS AGENT
Agent   : %s
News    : %s
Score   : %d
Status  : POST GENERATED
==================================================
""".formatted(persona.getName(), selectedNews.getTitle(), selectedDecision.getScore()));

            } catch (Exception ex) {
                System.out.println("""
==================================================
AUTONOMOUS AGENT ERROR
Agent ID : %s
Error    : %s
==================================================
""".formatted(agentId, ex.getMessage()));
            }
        }
    }
}
