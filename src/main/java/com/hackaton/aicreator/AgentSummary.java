package com.hackaton.aicreator;

public class AgentSummary {

    private String agentId;
    private String name;
    private String domain;
    private String createdAt;

    public AgentSummary() {
    }

    public AgentSummary(String agentId, String name, String domain, String createdAt) {
        this.agentId = agentId;
        this.name = name;
        this.domain = domain;
        this.createdAt = createdAt;
    }

    public String getAgentId() {
        return agentId;
    }

    public String getName() {
        return name;
    }

    public String getDomain() {
        return domain;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
