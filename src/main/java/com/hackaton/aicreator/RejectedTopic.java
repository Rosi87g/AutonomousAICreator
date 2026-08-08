package com.hackaton.aicreator;

public class RejectedTopic {

    private String topic;
    private String reason;
    private int score;
    private String rejectedAt;

    public RejectedTopic() {
    }

    public RejectedTopic(String topic, String reason, int score, String rejectedAt) {
        this.topic = topic;
        this.reason = reason;
        this.score = score;
        this.rejectedAt = rejectedAt;
    }

    public String getTopic() {
        return topic;
    }

    public String getReason() {
        return reason;
    }

    public int getScore() {
        return score;
    }

    public String getRejectedAt() {
        return rejectedAt;
    }
}
