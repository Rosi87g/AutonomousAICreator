package com.hackaton.aicreator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class NewsService {

    @Value("${news.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @SuppressWarnings("unchecked")
    public List<NewsItem> getLatestNews(Persona persona) {

        String query = persona.getDomain();

        String url = "https://newsapi.org/v2/everything"
                + "?q=(" + query + " OR cybersecurity OR AI)"
                + "&language=en"
                + "&sortBy=publishedAt"
                + "&searchIn=title"
                + "&pageSize=10"
                + "&apiKey=" + apiKey;

        List<NewsItem> newsList = new ArrayList<>();

        try {

            Map<String, Object> response
                    = restTemplate.getForObject(url, Map.class);

            if (response != null) {

                List<Map<String, Object>> articles
                        = (List<Map<String, Object>>) response.get("articles");

                if (articles != null) {

                    for (Map<String, Object> article : articles) {

                        Object title = article.get("title");
                        Object articleUrl = article.get("url");

                        if (title != null && articleUrl != null) {

                            newsList.add(
                                    new NewsItem(
                                            title.toString(),
                                            articleUrl.toString()
                                    )
                            );
                        }
                    }
                }
            }

        } catch (Exception e) {

            System.out.println("News API Error: " + e.getMessage());

        }

        if (newsList.isEmpty()) {

            newsList.add(
                    new NewsItem(
                            "Latest AI News",
                            "https://newsapi.org"
                    )
            );

        }

        return newsList;
    }
}
