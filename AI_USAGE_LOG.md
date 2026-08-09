# AI Usage Log

This document records how AI tools were used throughout the development of this project, including specific prompts, debugging sessions, and iterative decisions made during the hackathon.

## Primary Tool
**Claude (Anthropic)** — used as the primary pair-programming and architecture partner throughout the entire build, across a single continuous working session spanning problem analysis, implementation, debugging, deployment, and documentation.

## Additional Tool: ChatGPT
Used alongside Claude for a second opinion during specific debugging sessions, particularly around environment configuration and Git/GitHub issues:

- **Silent startup hang after adding `.env` support:** the application would stop immediately after printing the Spring Boot startup banner, with no error or stack trace. Diagnosed by isolating the most recent change (the `spring-dotenv` dependency), removing it entirely, and temporarily using PowerShell `$env:GEMINI_API_KEY` / `$env:NEWS_API_KEY` session variables to confirm the underlying Spring Boot application itself was healthy. This isolated the issue to the dotenv library rather than application code.
- **Dotenv library compatibility:** switched from `me.paulschwarz:spring-dotenv:4.0.0` to `me.paulschwarz:springboot4-dotenv:5.1.0` after suspecting a Spring Boot 4 compatibility issue with the original library version.
- **Repeated GitHub push-protection rejections for a leaked API key:** GitHub's secret scanning blocked several push attempts even after using `git commit --amend --no-edit`, because the amend only captures whatever is currently staged — if `application.properties` still contained the real key at staging time, the amended commit still contained the secret. Resolved by:
  - Verifying the actual working file content with `Get-Content .\src\main\resources\application.properties` before staging
  - Searching git history directly for key fragments with `git grep -n "AQ\."` and `git grep -n "4403a3"` to confirm no leftover traces
  - Confirming the final committed content directly with `git show HEAD:src/main/resources/application.properties`, checking it showed only the `${GEMINI_API_KEY}` / `${NEWS_API_KEY}` placeholders, not real values
- **Considered a full repository reset** (`Remove-Item -Recurse -Force .git`, `git init`) at one point during the secret-cleanup process, then correctly determined this was unnecessary once the actual file content was fixed before committing — an amended commit on the existing history was sufficient.
- **Clarified Maven project structure** — confirmed `src/test/java/.../AicreatorApplicationTests.java` (the default Spring Boot-generated test class) should be kept alongside `src/main`, not deleted, as it verifies the application context can start correctly.

This debugging process is also documented in full in the source chat transcript, available on request, showing the complete real-time troubleshooting sequence including terminal output at each step.

## Tools Used Within the Application Itself
- **Google Gemini API** (`gemini-flash-latest`) — the content-generation engine that writes each persona post
- **NewsAPI** — live topic discovery source

## Development Timeline & Key Decisions

### 1. Problem Selection & Architecture Planning
Reviewed three hackathon problem statements and discussed trade-offs (design-heavy vs. conversational-agent vs. autonomous-system problems) before selecting "Autonomous AI Creator," based on technical differentiation potential and timing alignment with the 48-hour evaluation window. Produced a PRD and folder structure for a Spring Boot implementation before writing any code.

### 2. Environment Setup
Debugged local environment issues step-by-step, including:
- `mvnw.cmd` not found (wrong working directory)
- `JAVA_HOME` not set correctly, requiring locating a bundled JDK and setting the environment variable
- Git not installed / not on PATH after installation, requiring manual PATH configuration

### 3. Core API Implementation
Built `POST /api/agent/init` and `GET /api/agent/feed` incrementally — starting with hardcoded fake responses, then wiring in a real `AgentStore`, then real file-based persistence — verified at each step via PowerShell (`Invoke-RestMethod`) and later Swagger UI.

### 4. Editorial Judgment Bug — real debugging example
Initial `DecisionEngine` scoring logic rejected nearly all topics, including a highly relevant AI biosafety story ("AI used to design brand new viruses..."), because the keyword-matching threshold (60) was too strict relative to keyword coverage. Diagnosed by reading actual rejection logs, then rewrote `DecisionEngine.java` with broader keyword coverage and a recalibrated threshold (35). Verified the fix by re-running the same news batch and confirming the biosafety story now scored 95 and was correctly approved.

### 5. Persistence & Deployment Constraints — real debugging example
Implemented `PersistenceService` to persist agents/feeds/memory to JSON files, verified survival across local restarts. Discovered Render's free-tier containers use ephemeral storage, meaning persisted data resets on every redeploy — diagnosed via Render deploy logs showing `Loaded 0 agent(s) from disk.` after each redeploy. Decided against a full database migration under time constraints, and instead adopted an operational discipline: finalize all code changes before the last redeploy, then stop pushing entirely so the container doesn't restart during the evaluation window.

### 6. Deployment Debugging
- Worked through Render's "Node" auto-detection incorrectly matching the repo (no Java runtime option available on Render), resolved by containerizing the app with a custom `Dockerfile` and selecting "Docker" as the environment.
- Fixed a `PlaceholderResolutionException` for `${GEMINI_API_KEY}` when running locally without the corresponding environment variable set — resolved via `$env:GEMINI_API_KEY` in the local terminal session, keeping real secrets out of any committed file.
- Fixed a GitHub push protection block after a real API key was accidentally hardcoded into `application.properties` during a testing shortcut; reverted to the `${GEMINI_API_KEY}` placeholder pattern and rotated the exposed key.

### 7. Reliability Enhancements
- Identified that Render's free tier sleeps after ~15 minutes of inactivity, which could prevent the `@Scheduled` publishing job from firing reliably; set up an external UptimeRobot monitor pinging the live URL every 5 minutes to keep the service awake for the full evaluation window.
- Added a daily post cap (`agent.max.posts.per.day`) after noticing accumulated local test posts could otherwise cause runaway API usage.

### 8. Feature Enhancements (post-MVP)
- Added `/api/agent/rejected` to expose editorial rejection reasoning, `/api/agent/list` and `/api/agent/stats` for visibility, and `/health` for basic liveness checking.
- Added persona continuity: `GeminiService` now receives the 1-2 most recent published posts as context, allowing (but not forcing) natural callbacks between posts.
- Refactored the post-generation logic out of `SchedulerService` into a shared `runCycleForAgent()` method so `POST /api/agent/init` can trigger one immediate post generation synchronously, ensuring the feed is never empty immediately after initialization.
- Adjusted the scheduler interval (1 hour → 30 minutes) for more consistent feed activity, after weighing NewsAPI's 100 requests/day free-tier limit against posting frequency.
- Added input validation on `/init` and creation timestamps on agents.

## Summary
This project was built through continuous, iterative collaboration with Claude and ChatGPT — including genuine mistakes, debugging sessions, and course corrections, not a single generated dump. All code was reviewed, compiled (`mvn compile`), and functionally tested — locally first, then on the live Render deployment — before being committed. Git history reflects incremental development across the build process.