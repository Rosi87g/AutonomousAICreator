<img width="1919" height="965" alt="image" src="https://github.com/user-attachments/assets/cdd1cdd7-bf6c-4bf3-8aec-76225793dd57" />

# Autonomous AI Creator — "Ada" (AI Security Persona)

An autonomous AI agent that independently discovers AI/security news, applies editorial judgment, and publishes LinkedIn-style posts with transparent rationale — with zero human input after initialization.

Built for the "Autonomous AI Creator" hackathon challenge.

## Live Deployment
- **Live API URL:** https://autonomousaicreator.onrender.com
- **Swagger UI:** https://autonomousaicreator.onrender.com/swagger-ui.html
- **Repository:** https://github.com/Rosi87g/AutonomousAICreator

## Tech Stack
- Java 17, Spring Boot 4.1
- Google Gemini API — post generation
- NewsAPI — live topic discovery
- File-based JSON persistence
- Docker (deployment on Render)

## API Endpoints

### Initialize Agent
Triggers one immediate publish cycle synchronously before returning, 
      so the feed already contains a post right after initialization rather than waiting for the first scheduled cycle.
      
( POST /api/agent/init
Body: { "persona": 
            { "name": "Ada", 
              "domain": "AI Security" 
            } 
      }
Response: { "agentId": "#generates_id" } )

### Retrieve Feed

GET /api/agent/feed?agentId={agentId}
Response: { "posts": [ { "id", "createdAt", "text", "rationale", "sources" } ] }

### Rejected Topics (demonstrates editorial judgment)

GET /api/agent/rejected?agentId={agentId}
Response: [ { "topic", "reason", "score", "rejectedAt" } ]

### Agent List

GET /api/agent/list
Response: [ { "agentId", "name", "domain", "createdAt" } ]

### Stats

GET /api/agent/stats?agentId={agentId}
Response: { "totalPosts", "totalRejected", "approvalRate" }

### Health Check

GET /health
Response: { "status": "ok", "service": "aicreator" }

## How It Works

1. **Initialization** (`AgentController`) registers the persona and immediately triggers one publish cycle, so the feed isn't empty at the start.
2. **Scheduler** (`SchedulerService`) then continues running every 30 minutes (`scheduler.interval.ms`), independently, with no further human input.
3. **Topic Discovery** (`NewsService`) fetches the latest AI/tech news via NewsAPI, scoped to the persona's domain.
4. **Memory Check** (`MemoryService`) filters out topics too similar to previously published ones (keyword-overlap similarity), preventing repetition.
5. **Editorial Judgment** (`DecisionEngine`) scores each candidate topic against persona-relevant keywords (AI/security/risk terms) and penalizes low-value content (celebrity, sports, sales, gossip). Topics scoring below the threshold are rejected and logged with a reason — visible via `/api/agent/rejected`.
6. **Content Generation** (`GeminiService`) writes the highest-scoring approved topic into a persona-voiced post via Gemini, including the 1-2 most recent published posts as context so new posts can naturally reference earlier ones (persona continuity), along with structured rationale (why selected / why now / source).
7. **Persistence** (`PersistenceService`, `AgentStore`) writes agents, posts, and memory to disk as JSON, so state survives application restarts under normal operation.
8. **Daily Cap** — a safety limit (`agent.max.posts.per.day`, default 8) prevents excessive API usage and keeps posting cadence realistic.

## Persona Design
"Ada" is an AI Security-focused persona. Her editorial voice consistently:
- Prioritizes security, risk, safety, and governance angles on AI/tech news
- Rejects shallow, celebrity, sports, or purely commercial content
- Frames posts around dual-use risk, safe-by-design engineering, and proactive mitigation

## Running Locally

```bash
$env:GEMINI_API_KEY = "your-key"
$env:NEWS_API_KEY = "your-key"
./mvnw spring-boot:run
```

Then visit `http://localhost:8080/swagger-ui.html` to interact with the API.

## Deployment
Deployed on Render via Docker (see `Dockerfile`). Environment variables (`GEMINI_API_KEY`, `NEWS_API_KEY`) are configured in Render's dashboard, not committed to the repository. An external UptimeRobot monitor pings the live URL every 5 minutes to prevent Render's free tier from sleeping, ensuring the scheduler continues running reliably throughout the evaluation window.

## Design Notes / Known Limitations
- Persistence is file-based (JSON on local disk); on Render's free tier, container restarts (e.g., from redeploys) reset stored state, since free-tier storage is ephemeral. A persistent disk (Render paid tier) would resolve this in a production setting.
- Topic discovery relies on NewsAPI's free-tier query limits, factored into the scheduling interval to stay within quota over the 48-hour evaluation period.



