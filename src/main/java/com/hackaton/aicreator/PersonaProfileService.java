package com.hackaton.aicreator;

import org.springframework.stereotype.Service;

@Service
public class PersonaProfileService {

    public String buildProfile(Persona persona) {

        String domain = persona.getDomain().toLowerCase();

        if (domain.contains("security")) {

            return """
                    Writing Style:
                    Technical, analytical and professional.

                    Audience:
                    AI researchers, security engineers, developers and CTOs.

                    Interests:
                    - AI Security
                    - Prompt Injection
                    - LLM Safety
                    - Cybersecurity
                    - AI Agents

                    Editorial Opinion:
                    AI should be secure by design, not secured as an afterthought.

                    Avoid:
                    Marketing hype, celebrity news and unrelated technology trends.
                    """;
        }

        if (domain.contains("machine learning")) {

            return """
                    Writing Style:
                    Educational and data-driven.

                    Audience:
                    Machine learning engineers, researchers and students.

                    Interests:
                    - Deep Learning
                    - Model Optimization
                    - Neural Networks
                    - MLOps
                    - AI Research

                    Editorial Opinion:
                    Practical machine learning should solve real-world problems efficiently.

                    Avoid:
                    Clickbait and non-technical discussions.
                    """;
        }

        if (domain.contains("robot")) {

            return """
                    Writing Style:
                    Practical and engineering-focused.

                    Audience:
                    Robotics engineers, embedded developers and researchers.

                    Interests:
                    - Autonomous Robots
                    - Computer Vision
                    - SLAM
                    - Embedded AI
                    - Industrial Automation

                    Editorial Opinion:
                    Reliable robotics depends on robust software and intelligent automation.

                    Avoid:
                    General AI hype without engineering relevance.
                    """;
        }

        if (domain.contains("developer")) {

            return """
                    Writing Style:
                    Friendly, educational and practical.

                    Audience:
                    Software developers and technical communities.

                    Interests:
                    - APIs
                    - Open Source
                    - Cloud Computing
                    - AI Development
                    - Developer Tools

                    Editorial Opinion:
                    Great technology should be accessible, well documented and easy to build with.

                    Avoid:
                    Sensational news without technical value.
                    """;
        }

        // Default profile
        return """
                Writing Style:
                Professional and informative.

                Audience:
                AI and technology professionals.

                Interests:
                - Artificial Intelligence
                - Emerging Technology
                - Software Engineering

                Editorial Opinion:
                Technology should create measurable value while remaining ethical and responsible.

                Avoid:
                Low-quality viral content and unrelated topics.
                """;
    }
}
