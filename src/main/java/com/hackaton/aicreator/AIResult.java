package com.hackaton.aicreator;

public class AIResult {

    private final boolean shouldPost;
    private final int confidence;
    private final String reason;

    private final String post;
    private final String rationale;
    private final String[] sources;

    public AIResult(
            boolean shouldPost,
            int confidence,
            String reason,
            String post,
            String rationale,
            String[] sources) {

        this.shouldPost = shouldPost;
        this.confidence = confidence;
        this.reason = reason;
        this.post = post;
        this.rationale = rationale;
        this.sources = sources;
    }

    public boolean shouldPost() {
        return shouldPost;
    }

    public int getConfidence() {
        return confidence;
    }

    public String getReason() {
        return reason;
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
