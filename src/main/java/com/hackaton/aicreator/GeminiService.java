package com.hackaton.aicreator;

import com.google.genai.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GeminiService {

    private final Client client;

    @Autowired
    private PersonaProfileService personaProfileService;

    public GeminiService(@Value("${gemini.api.key}") String apiKey) {
        this.client = Client.builder()
                .apiKey(apiKey)
                .build();
    }

    public AIResponse generatePost(Persona persona, NewsItem news) {

        try {

            String profile = personaProfileService.buildProfile(persona);

            String prompt = """
                    You are an autonomous LinkedIn content creator.

                    Persona Name:
                    %s

                    Domain:
                    %s

                    Persona Profile:
                    %s

                    Today's Topic:
                    %s

                    Reference Source:
                    %s

                    Instructions:

                    - Write as THIS persona only.
                    - Maintain the same editorial voice every time.
                    - Express the persona's opinion naturally.
                    - Focus only on AI and technology.
                    - Do not use clickbait.
                    - Keep the tone professional.
                    - Length: 100–150 words.

                    Respond in EXACTLY this format:

                    POST:
                    <LinkedIn Post>

                    RATIONALE:
                    Explain:
                    - Why this topic was selected
                    - Why it is relevant now
                    - Why it matches the persona

                    SOURCES:
                    %s
                    """.formatted(
                    persona.getName(),
                    persona.getDomain(),
                    profile,
                    news.getTitle(),
                    news.getUrl(),
                    news.getUrl()
            );

            String response = client.models.generateContent(
                    "gemini-flash-latest",
                    prompt,
                    null
            ).text();

            String post = "";
            String rationale = "";
            String[] sources = new String[0];

            String[] sections = response.split("RATIONALE:");

            if (sections.length >= 2) {

                post = sections[0]
                        .replace("POST:", "")
                        .trim();

                String[] second = sections[1].split("SOURCES:");

                rationale = second[0].trim();

                if (second.length >= 2) {
                    sources = second[1]
                            .trim()
                            .split("\\R");
                }
            }

            return new AIResponse(
                    post,
                    rationale,
                    sources
            );

        } catch (Exception e) {

            System.out.println("Gemini unavailable: " + e.getMessage());

            return new AIResponse(
                    "AI service is temporarily busy.",
                    "Gemini returned an error. Scheduler will retry later.",
                    new String[0]
            );
        }
    }
}
