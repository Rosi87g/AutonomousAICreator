package com.hackaton.aicreator;

public class AIResponse {

    private String post;
    private String rationale;
    private String[] sources;

    public AIResponse(String post, String rationale, String[] sources) {
        this.post = post;
        this.rationale = rationale;
        this.sources = sources;
    }

    public String getPost() {
        return post;
    }

    public String getRationale() {
        return rationale;
    }

    public String[] getSources() {
        return sources;
    }
}
