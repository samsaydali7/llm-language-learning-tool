# LLM Language Learning Tool

## Abstract

This project is an LLM-powered, private, local-first language learning system designed to transform a user’s own study materials and associated audio into a structured learning engine.

Instead of building a generic chatbot, the system focuses on a grounded, domain-specific AI workflow: parse educational content, extract language knowledge, preserve source provenance, and generate personalized exercises from the relevant subset of that knowledge.

It is designed to be language-agnostic and extensible to other books and languages, as long as the content is owned or legally authorized for the user's personal study use.

The project demonstrates practical AI product thinking around ingestion, extraction, context selection, grounding, knowledge modeling, and local model orchestration.

## Problem

Most language learners study from PDFs, audio, and structured course materials, but these assets are fragmented and not naturally turned into reusable learning data. The challenge is not just parsing text — it is turning raw educational content into a structured, queryable knowledge base that can support AI-driven practice.

This project addresses that gap by using LLMs to extract meaning from the user’s authorized materials, organize it into a knowledge base, and generate learning activities based on the exact scope the learner is studying.

## Copyright and Privacy Boundaries

This application is intended for private, personal use with materials the user owns or is otherwise legally permitted to use.

The system must not be designed to store or redistribute copyrighted content without authorization. It should process only materials the user has the right to import and study, and it must keep those materials local to the user’s environment.

The app should help the user learn from their own content, not act as a repository or distribution system for third-party copyrighted books or media. The codebase itself is not intended to store or distribute copyrighted material; it is meant to run privately for the user’s own study workflow.

## Vision

The application should act like a private study engine for language learning:

- import a book and its associated audio
- preserve the book’s structure and source references
- extract vocabulary, grammar, expressions, examples, and topics
- organize content by chapter, section, and concept
- let the user learn by topic instead of only by chapter order
- generate exercises from the selected material
- review failures and connect them back to grammar and vocabulary
- support listening practice using the associated audio and transcript

## Core Capabilities

### Book and audio ingestion

The app accepts a book PDF and accompanying audio files, maps them to the correct locations in the text, and reuses the book’s transcript instead of requiring speech-to-text processing.

### Knowledge extraction

The AI layer identifies meaningful language-learning data, including:

- vocabulary
- grammar
- examples
- expressions
- topics
- useful phrases and notes
- source references such as chapter, page, and book location

This is not a simple text dump. The system is designed to extract structured learning facts and bind them back to their original source material so the model can generate relevant exercises without losing provenance.

### Structured study model

The user can select learning material based on a range of scopes, including:

- language
- book
- chapter
- section
- topic
- knowledge type such as grammar or vocabulary

This allows study to move beyond simple linear reading and toward topic-based learning.

### Exercise generation

The app generates exercises from the selected learning scope rather than from random content. It supports multiple exercise types, including multiple choice, fill-in-the-blank, translation, matching, sentence construction, and grammar transformation.

This is where the LLM reasoning is most visible: the model is asked to generate context-aware practice items based on a narrow slice of the user's knowledge base, not based on broad unfiltered content. The design supports both:

- persistent exercise generation for study sessions and review routines
- on-demand generation for small, targeted queries from a selected knowledge subset

### Review and retention

Users can review generated exercises, see correct answers, and track mistakes. Failed material is connected back to vocabulary, grammar, and topic metadata so it can be revisited in future sessions.

## V1 Scope

The first version is intentionally focused:

- single-user local-first workflow
- private local storage
- local LLM execution through Ollama
- support for user-owned study materials only
- extensibility to other languages and books
- no public accounts, no cloud deployment, no social features

## 3. Core Architecture

```text
User
  |
  v
Frontend (Angular)
  |
  v
API Layer (Spring Boot)
  |
  +--> Upload + organize PDFs / audio
  +--> Manage study scope
  +--> Trigger extraction jobs
  +--> Trigger exercise jobs
  +--> Review attempts and failures
  |
  v
Storage + Data Layer
  |
  +--> PostgreSQL
  +--> File storage
  +--> Knowledge base
  +--> Exercise sets
  +--> Failed knowledge review
  |
  v
Local AI Layer (Ollama + LLM provider abstraction)
  |
  +--> Extract vocabulary, grammar, examples, topics
  +--> Generate exercises, flashcards, listening tasks
  +--> Review grammar and mistakes
```

```text
LlmProvider
  |
  +-- QwenProvider
  |
  +-- LlamaProvider
  |
  +-- ClaudeProvider (future)

Methods:
- extractKnowledge(request)
- generateExercises(request)
- generateGrammarReview(request)
```

```text
PDF + Audio + Book Structure
        |
        v
Document Parsing
        |
        v
Transcript Mapping + Source Provenance
        |
        v
Structured Knowledge Base
        |
        +--> vocab
        +--> grammar
        +--> expressions
        +--> examples
        +--> topics
        |
        v
Study Scope Selection
        |
        v
Exercise Generation + Review Loop
```

## Technology Direction

The project is intended to remain local-first and model-agnostic at the service boundary.

Core design principles:


## Local AI Stack

The repository uses Ollama natively on the developer host for local model execution. Docker runs the supporting services:

```text
Docker Compose
  |
  +--> PostgreSQL       structured knowledge and review data
  +--> RabbitMQ         asynchronous extraction and exercise jobs
         |
         v
  Native Ollama :11434
         |
         +--> Qwen extraction model
         +--> Llama exercise model
```

Open WebUI (a browser UI for manually testing Ollama models) is optional and not part of the Compose stack - see "Optional: Open WebUI for manual model testing" below if you want it. On memory-constrained hardware it's worth leaving off entirely: an idle instance is one more thing competing with Ollama for RAM, and on a 16GB machine that's measurably felt (real-world extraction throughput roughly doubled after stopping it during a live run).

## Full Local Setup

### 1. Install Ollama

Install Ollama for your operating system using the [official Ollama download page](https://ollama.com/download). Supported installation paths include macOS, Linux, and Windows.

Verify the installation:

```bash
ollama --version
```

### 2. Start Ollama

```bash
ollama serve
```

In another terminal, download the configured models:

```bash
ollama pull qwen3:8b
ollama pull llama3.2:3b
```

### 3. Configure the Docker services

```bash
cp .env.example .env
docker compose up -d postgres rabbitmq
```

The default model routing is configurable through `.env` (see `.env.example` for the full list):

```env
EXTRACTION_PROVIDER=qwen
EXTRACTION_MODEL=qwen3:8b
EXERCISE_PROVIDER=llama
EXERCISE_MODEL=llama3.2:3b
GRAMMAR_REVIEW_PROVIDER=llama
GRAMMAR_REVIEW_MODEL=llama3.2:3b
```

The extraction and exercise workloads are deliberately separated. A stronger or cloud-backed provider can later be selected for extraction when quality justifies its cost, while a local model can continue handling high-volume exercise generation. Changing that routing should not require changing application code.

Claude, if added later, would be integrated as a separate Anthropic provider. It cannot be accessed through Ollama because it is a hosted model. This would allow Claude to handle quality-sensitive extraction while local Ollama models handle frequent exercise generation. The default V1 workflow remains local and does not require an external API key.

The Compose configuration maps `host.docker.internal` to the host gateway so the backend can reach native Ollama on macOS, Linux, or Windows.

### Optional: Open WebUI for manual model testing

Open WebUI is a browser UI for poking at Ollama models directly - useful while developing, not needed to run the app. It's intentionally not part of `docker-compose.yml` so it never runs (and consumes RAM) unless you actually want it. Start it standalone whenever you need it:

```bash
docker run -d --name open-webui \
  -p 3000:8080 \
  -e OLLAMA_BASE_URL=http://host.docker.internal:11434 \
  --add-host=host.docker.internal:host-gateway \
  -v open_webui_data:/app/backend/data \
  ghcr.io/open-webui/open-webui:main
```

Then open [http://localhost:3000](http://localhost:3000). Stop it with `docker stop open-webui && docker rm open-webui` when done (`-v open_webui_data:...` keeps its settings/history for next time even after the container is removed). See the [Open WebUI guide](docs/open-webui.md) for more.

## Running the Full Application

The application itself (Spring Boot backend + Angular frontend) lives in [backend/](backend/) and [frontend/](frontend/). With Ollama running natively (steps 1-2 above) and its models pulled:

```bash
cp .env.example .env
docker compose up -d --build
```

This starts Postgres, RabbitMQ, the backend API, and the frontend together. Open the app at [http://localhost:8081](http://localhost:8081). The backend API is also reachable directly at [http://localhost:8080/api](http://localhost:8080/api) if useful for debugging.

`--build` only needs to run the first time, or after you change backend/frontend code - for every other start (including after a reboot, if the containers didn't already come back up on their own) plain `docker compose up -d` is enough and is faster since it skips rebuilding images:

```bash
docker compose up -d
```

To stop everything (data is preserved - see "Data Persistence" below):

```bash
docker compose stop      # stop containers, keep them around to restart quickly
# or
docker compose down      # stop and remove containers (volumes/data untouched)
```

### Running backend and frontend natively instead

Useful for active development, where rebuilding a Docker image on every change is slow.

```bash
# Supporting services only
docker compose up -d postgres rabbitmq

# Backend (JDK 21 required; the Maven wrapper needs no local Maven install)
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# Frontend, in another terminal
cd frontend
npm install
npm start
```

The frontend dev server proxies `/api` to `http://localhost:8080` (see `frontend/proxy.conf.json`), so open [http://localhost:4200](http://localhost:4200).

### First use

1. Add a language (e.g. code `fr`, name `French`).
2. Add a book under that language, with the explanation language you want (e.g. `en`).
3. Upload the book's PDF - this kicks off an async job that detects its structure (chapters/sections) and then extracts vocabulary, grammar, expressions, examples, and topics section by section.
4. Upload the book's audio files - references like "Track 12" found in the text are matched to uploaded files automatically where possible; anything unmatched can be linked manually.
5. Browse the book's structure and topics once extraction completes, then use Exercises, Flashcards, Listening, and Grammar review to study, and Review mistakes to revisit what you've gotten wrong.

### Data Persistence

Postgres, RabbitMQ, and the backend's uploaded PDFs/audio each write to a named Docker volume (`postgres_data`, `rabbitmq_data`, `backend_storage` in `docker-compose.yml`). Named volumes live on disk independently of any container, so your data survives:

- `docker compose stop` / `docker compose restart`
- `docker compose down` (stops and removes the *containers*, not the volumes)
- a full host restart, once Docker is running again

All services also run with `restart: unless-stopped`, so after Docker itself restarts (e.g. following a host reboot) they come back up on their own - just make sure Docker Desktop (or your Docker daemon) is set to start automatically if you want that to happen without manual intervention.

**The one command that does delete your data is `docker compose down -v`** (or `docker volume rm`/`docker volume prune`) - the `-v` flag removes volumes along with the containers. Avoid it unless you specifically want to wipe the database and start over.

To back up your data, copy the volumes' contents (e.g. `docker run --rm -v llm-language-learning-tool_postgres_data:/data -v $(pwd):/backup alpine tar czf /backup/postgres_backup.tar.gz -C /data .`) or use `pg_dump` against the running `postgres` container.

### Backend architecture at a glance

The backend is organized by domain (`language`, `book`, `structure`, `storage`, `audio`, `knowledge`, `topic`, `extraction`, `llm`, `scope`, `exercise`, `flashcard`, `listening`, `grammar`, `attempt`, `job`), each following the same `entity/ repository/ service/ api/` shape. The `llm` package holds the `LlmProvider` abstraction (SPEC.md #7) exactly as specified, with `QwenProvider` and `LlamaProvider` both delegating to a shared `OllamaClient`, a stubbed `ClaudeProvider` seam for a future Anthropic integration, and an `LlmRouter` that resolves provider + model per workload from the `llm.*` config (SPEC.md #8) - application code never depends on a concrete provider. PDF structure detection reads the PDF's own outline/bookmarks first, falling back to a font-size heuristic when a PDF has none; only the knowledge-extraction step (vocabulary/grammar/expressions/examples/topics within a section) calls the LLM. Structure and knowledge extraction, and job-based exercise generation, all run asynchronously through RabbitMQ (SPEC.md #2.1); on-demand exercise generation and flashcards run synchronously, sharing the same `scope/KnowledgeQueryService` and `exercise/ExerciseGenerationService` pipeline either way (SPEC.md #2.3).

## Repository Documents

- [REQUIREMENTS.md](REQUIREMENTS.md) — product requirements for V1
- [SPEC.md](SPEC.md) — technical specification and system design
- [docs/ollama.md](docs/ollama.md) — native Ollama runtime guide
- [docs/open-webui.md](docs/open-webui.md) — browser UI for testing local models
- [backend/](backend/) — Spring Boot API, knowledge extraction pipeline, and LLM provider abstraction
- [frontend/](frontend/) — Angular application (books, structure browsing, exercises, flashcards, listening, grammar review, mistake review)

## Portfolio Framing

This project demonstrates practical LLM system design in a real-world domain:

- document ingestion and structured extraction using AI
- local-first architecture for private model execution
- model abstraction and provider routing
- async processing for generation jobs
- grounding model outputs in source provenance
- scoped retrieval of relevant knowledge before generation
- review loops tied to mistakes and learning outcomes
- handling of prompt context limits, quality control, and constrained generation

It is a good fit for a portfolio because it shows understanding of the systems side of AI work: not just prompting, but orchestration, retrieval, provenance, workflow design, and production-ready local deployment constraints.

## Status

V1 is implemented and has been run end-to-end (PDF upload → async structure and knowledge extraction against a real Ollama instance → browsing → both exercise-generation modes → attempts/failure tracking → flashcards → listening → audio-reference matching), plus a production Angular build and the backend's automated test suite (`cd backend && mvn test`) all passing. See "Running the Full Application" above to run it yourself.

## Intended Outcome

The final product is intended to be a private digital tutor that turns user-owned materials and audio into an organized language study system, with the learner always anchored to the original source material and supported by local AI generation.
