# Open WebUI

## What Is Open WebUI?

Open WebUI is a browser-based interface for interacting with local language models.

It provides a ChatGPT-style workspace where you can:

- select an installed model
- send prompts
- inspect model responses
- compare model behavior
- manage local model conversations

In this project, Open WebUI is a development and operator interface for testing Ollama models. It is not the product UI described by the Angular frontend architecture, and it is **not** part of `docker-compose.yml` - it's optional, run standalone only when you actually want it (see "Starting Open WebUI" below). Keeping it out of the default stack matters on memory-constrained hardware: it's one more process competing with Ollama for RAM, and on a 16GB machine that's a measurable hit to real extraction throughput.

## How It Fits the System

```text
Browser
   |
   v
Open WebUI :3000 (standalone container, started on demand)
   |
  |  http://host.docker.internal:11434
   v
Native Ollama :11434
   |
   +--> Qwen extraction model
   +--> Llama exercise model
```

Open WebUI and the application backend use the same native Ollama runtime, but they have different purposes:

- **Open WebUI**: manual model testing, run standalone, started/stopped independently of the app
- **Angular + Spring Boot application** (`docker compose up`): book and audio workflows, knowledge extraction jobs, source provenance, exercise generation, review and failure tracking

## Starting Open WebUI

Start Ollama natively first:

```bash
ollama serve
```

Then start Open WebUI as a standalone container:

```bash
docker run -d --name open-webui \
  -p 3000:8080 \
  -e OLLAMA_BASE_URL=http://host.docker.internal:11434 \
  --add-host=host.docker.internal:host-gateway \
  -v open_webui_data:/app/backend/data \
  ghcr.io/open-webui/open-webui:main
```

Open the UI at:

[http://localhost:3000](http://localhost:3000)

The first visit may ask you to create a local account. The account data is stored in the `open_webui_data` Docker volume (named the same as the one the old Compose-managed setup used, so existing data carries over if you have any).

When you're done, stop and remove the container - the volume (and its saved settings/history) stays behind for next time:

```bash
docker stop open-webui
docker rm open-webui
```

## Connection Configuration

The container connects to native Ollama on the developer host via the `OLLAMA_BASE_URL` environment variable and the `host.docker.internal` host-gateway mapping shown in the `docker run` command above. The browser does not connect directly to this internal address; it connects to Open WebUI through port `3000`.

From the host machine, Ollama's API is available at:

```text
http://localhost:11434
```

## Selecting a Model

After the models are downloaded, Open WebUI should show them in its model selector.

The default models are:

```text
qwen3:8b
llama3.2:3b
```

Use Qwen to experiment with extraction-style prompts and Llama to experiment with exercise-generation prompts. The names are configurable through `.env`:

```env
EXTRACTION_MODEL=qwen3:8b
EXERCISE_MODEL=llama3.2:3b
```

Changing these variables affects the application's model routing (SPEC.md #8); they don't affect Open WebUI directly, since it's a standalone testing tool - select whichever model you want from its own model picker.

## Useful Testing Prompts

### Extraction experiment

```text
Given the selected educational passage, return structured JSON with vocabulary, grammar concepts, expressions, examples, topics, and source references. Do not invent information that is not supported by the passage.
```

### Exercise experiment

```text
Using only the selected knowledge items, generate five exercises. Include the exercise type, question, correct answer, explanation, and source reference. Do not use knowledge outside the provided context.
```

These prompts are for experimentation only. The production application uses structured request/response contracts, validation, provenance checks, and retry handling - see `backend/src/main/java/com/languagelearning/llm/`.

## Why It Is Useful for This Project

Open WebUI helps validate model capabilities before implementing (or while debugging) the backend pipeline. It can be used to compare:

- extraction quality across models
- structured output reliability
- exercise quality from narrow context
- response latency
- token usage and context limits
- whether a local model is sufficient for a workload

This supports the project's model-specialization strategy:

```text
Extraction quality requirement
        |
        +--> stronger local model
        +--> cloud-backed provider, if enabled

Exercise generation requirement
        |
        +--> smaller local model
```

The extraction and exercise models do not need to be the same model. Their configuration can change independently according to quality, latency, privacy, and cost requirements.

## Data and Privacy

Open WebUI is running locally in this setup, but prompts and conversations may be persisted in its Docker volume. Only use materials the user owns or is authorized to use.

Do not commit any of the following to the repository:

- PDFs
- audio files
- copyrighted passages
- model files
- private prompts or conversations
- exported Open WebUI data

The `open_webui_data` volume is local runtime data and is not part of the Git repository.

## Troubleshooting

### Open WebUI cannot connect to Ollama

Check that the container is running and inspect its logs:

```bash
docker ps --filter name=open-webui
docker logs open-webui
```

The container must be using `http://host.docker.internal:11434` as its Ollama URL (set via `OLLAMA_BASE_URL` in the `docker run` command). If Open WebUI's admin connection settings show something else - e.g. a stale `http://ollama:11434` saved from an earlier setup - open its admin connection settings in the UI and change the Ollama URL to:

```text
http://host.docker.internal:11434
```

Save the setting and refresh the model list.

Do not use `localhost:11434` inside the Open WebUI container - in a container, `localhost` refers to the container itself, not the host.

### Models do not appear

Download the models natively, then refresh Open WebUI:

```bash
ollama pull qwen3:8b
ollama pull llama3.2:3b
```

### Reset Open WebUI data

This removes the local Open WebUI account and conversation data. Only do this if you intentionally want a clean slate - it does not touch Ollama models or anything from the main application:

```bash
docker stop open-webui
docker rm open-webui
docker volume rm open_webui_data
# then start it again as in "Starting Open WebUI" above
```

## Related Documentation

- [README.md](../README.md)
- [Ollama guide](ollama.md)
- [SPEC.md](../SPEC.md)
- [docker-compose.yml](../docker-compose.yml)
