package com.hackaton.aicreator;

public class Post {

    private String id;
    private String createdAt;
    private String text;
    private String rationale;
    private String[] sources;

    public Post() {
    }   // ← ADD THIS LINE

    public Post(String id, String createdAt, String text, String rationale, String[] sources) {
        this.id = id;
        this.createdAt = createdAt;
        this.text = text;
        this.rationale = rationale;
        this.sources = sources;
    }

    public String getId() {
        return id;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getText() {
        return text;
    }

    public String getRationale() {
        return rationale;
    }

    public String[] getSources() {
        return sources;
    }
}
