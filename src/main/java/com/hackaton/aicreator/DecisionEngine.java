package com.hackaton.aicreator;

import org.springframework.stereotype.Service;

@Service
public class DecisionEngine {

    public Decision evaluate(Persona persona, NewsItem news) {

        String title = news.getTitle().toLowerCase();
        int score = 0;

        // -------------------------
        // Core AI / Technology Keywords
        // -------------------------
        if (title.contains("ai") || title.contains("artificial intelligence")) {
            score += 30;
        }
        if (title.contains("model")) {
            score += 15;
        }
        if (title.contains("algorithm")) {
            score += 15;
        }
        if (title.contains("machine learning") || title.contains(" ml ")) {
            score += 20;
        }
        if (title.contains("llm")) {
            score += 25;
        }
        if (title.contains("neural")) {
            score += 20;
        }
        if (title.contains("data")) {
            score += 10;
        }
        if (title.contains("chatbot") || title.contains("agent")) {
            score += 15;
        }
        if (title.contains("research")) {
            score += 15;
        }
        if (title.contains("open source")) {
            score += 15;
        }
        if (title.contains("developer")) {
            score += 10;
        }
        if (title.contains("benchmark")) {
            score += 10;
        }
        if (title.contains("robot") || title.contains("automation")) {
            score += 15;
        }

        // -------------------------
        // Safety / Security / Risk Keywords (broad)
        // -------------------------
        if (title.contains("security")) {
            score += 30;
        }
        if (title.contains("cyber")) {
            score += 25;
        }
        if (title.contains("hack") || title.contains("breach")) {
            score += 25;
        }
        if (title.contains("vulnerab") || title.contains("exploit")) {
            score += 25;
        }
        if (title.contains("attack") || title.contains("threat")) {
            score += 20;
        }
        if (title.contains("malware") || title.contains("virus")) {
            score += 25;
        }
        if (title.contains("privacy")) {
            score += 20;
        }
        if (title.contains("risk") || title.contains("safety") || title.contains("biosafety")) {
            score += 20;
        }
        if (title.contains("regulation") || title.contains("policy") || title.contains("governance")) {
            score += 15;
        }
        if (title.contains("fraud") || title.contains("scam")) {
            score += 15;
        }

        // -------------------------
        // Notable Companies / Products
        // -------------------------
        if (title.contains("openai") || title.contains("anthropic") || title.contains("claude")
                || title.contains("gemini") || title.contains("google") || title.contains("microsoft")
                || title.contains("nvidia") || title.contains("copilot") || title.contains("meta")) {
            score += 15;
        }

        // -------------------------
        // Low Value Content (hard penalties)
        // -------------------------
        if (title.contains("celebrity")) {
            score -= 60;
        }
        if (title.contains("viral")) {
            score -= 40;
        }
        if (title.contains("meme")) {
            score -= 60;
        }
        if (title.contains("rumor")) {
            score -= 50;
        }
        if (title.contains("gossip")) {
            score -= 60;
        }
        if (title.contains("movie") || title.contains("film")) {
            score -= 40;
        }
        if (title.contains("cricket") || title.contains("football")) {
            score -= 60;
        }
        if (title.contains("discount") || title.contains("sale") || title.contains("% off")) {
            score -= 40;
        }
        if (title.contains("horoscope")) {
            score -= 60;
        }

        // -------------------------
        // Persona Domain Matching (bonus)
        // -------------------------
        String domain = persona.getDomain().toLowerCase();

        if (domain.contains("security")) {
            if (title.contains("security") || title.contains("cyber") || title.contains("hack")
                    || title.contains("vulnerab") || title.contains("breach") || title.contains("risk")
                    || title.contains("safety") || title.contains("privacy")) {
                score += 20;
            }
        }

        if (domain.contains("machine learning")) {
            if (title.contains("model") || title.contains("training") || title.contains("neural")) {
                score += 20;
            }
        }

        if (domain.contains("robot")) {
            if (title.contains("robot") || title.contains("automation")) {
                score += 20;
            }
        }

        // -------------------------
        // Final Decision — lowered threshold
        // -------------------------
        int finalScore = Math.max(0, Math.min(score, 100));

        if (finalScore >= 35) {
            return new Decision(true, "Relevant AI/tech topic with sufficient editorial value (score " + finalScore + ").", finalScore);
        }

        return new Decision(false, "Topic scored too low on relevance/quality (score " + finalScore + ").", finalScore);
    }
}
