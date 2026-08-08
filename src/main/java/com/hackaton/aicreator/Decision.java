package com.hackaton.aicreator;

public class Decision {

    private final boolean shouldPost;
    private final String reason;
    private final int confidence;

    public Decision(boolean shouldPost, String reason, int confidence) {
        this.shouldPost = shouldPost;
        this.reason = reason;
        this.confidence = confidence;
    }

    public boolean shouldPost() {
        return shouldPost;
    }

    public boolean isApproved() {
        return shouldPost;
    }

    public String getReason() {
        return reason;
    }

    public int getConfidence() {
        return confidence;
    }

    public int getScore() {
        return confidence;
    }
}
