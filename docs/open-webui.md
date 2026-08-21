# Open WebUI

## What Is Open WebUI?

Open WebUI is a browser-based interface for interacting with local language models.

It provides a ChatGPT-style workspace where you can:

- select an installed model
- send prompts
- inspect model responses
- compare model behavior
- manage local model conversations

In this project, Open WebUI is a development and operator interface for testing Ollama models. It is not the product UI described by the Angular frontend architecture.

## How It Fits the System

```text
Browser
   |
   v
Open WebUI :3000
   |
  |  http://host.docker.internal:11434
   v
Native Ollama :11434
   |
   +--> Qwen extraction model
   +--> Llama exercise model
```

Open WebUI and the future application backend use the same native Ollama runtime, but they have different purposes:

Open WebUI
  Manual model testing
docker compose stop open-webui
docker compose rm -f open-webui
docker volume rm llm-language-learning-tool_open_webui_data
docker compose up -d open-webui

Angular + Spring Boot application
  Book and audio workflows
  Knowledge extraction jobs
  Source provenance
  Exercise generation
  Review and failure tracking
```

## Starting Open WebUI

Start Ollama natively first:

```bash
ollama serve
```

Then start Open WebUI in Docker:

```bash
cp .env.example .env
docker compose up -d open-webui
```

Open the UI at:

[http://localhost:3000](http://localhost:3000)

The first visit may ask you to create a local account. The account data is stored in the Docker-managed `open_webui_data` volume.

## Connection Configuration

The Compose service connects to native Ollama on the developer host:

```yaml
environment:
  OLLAMA_BASE_URL: http://host.docker.internal:11434
  OLLAMA_BASE_URLS: http://host.docker.internal:11434
```

`host.docker.internal` resolves back to the host. The Compose configuration provides the host-gateway mapping needed for Linux and supports the same address on Docker Desktop for macOS and Windows. The browser does not connect directly to this internal address; it connects to Open WebUI through port `3000`.

From the host machine, Ollama’s API is available at:

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

Changing these variables affects the model bootstrap configuration. The application’s future provider routing will use equivalent runtime configuration rather than hard-coded model names.

## Useful Testing Prompts

### Extraction experiment

```text
Given the selected educational passage, return structured JSON with vocabulary, grammar concepts, expressions, examples, topics, and source references. Do not invent information that is not supported by the passage.
```

### Exercise experiment

```text
Using only the selected knowledge items, generate five exercises. Include the exercise type, question, correct answer, explanation, and source reference. Do not use knowledge outside the provided context.
```

These prompts are for experimentation only. The production application will need structured request and response contracts, validation, provenance checks, and retry handling.

## Why It Is Useful for This Project

Open WebUI helps validate model capabilities before implementing the backend pipeline. It can be used to compare:

- extraction quality across models
- structured output reliability
- exercise quality from narrow context
- response latency
- token usage and context limits
- whether a local model is sufficient for a workload

This supports the project’s model-specialization strategy:

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

Check that both services are running:

```bash
docker compose ps ollama open-webui
```

Inspect logs:

```bash
docker compose logs ollama open-webui
```

The Open WebUI container must use:

```text
http://host.docker.internal:11434

If the logs still show `http://ollama:11434`, Open WebUI is using a connection saved in its local database from the previous Docker Ollama setup. In Open WebUI, open the admin connection settings and change the Ollama URL to:

```text
http://host.docker.internal:11434
```

Save the setting and refresh the model list.

If this is a new installation and you do not need to preserve Open WebUI account or conversation data, reset its local volume:

```bash
docker compose down
docker volume rm llm-language-learning-tool_open_webui_data
docker compose up -d open-webui
```

Then create the local Open WebUI account again. This reset is optional and deletes only Open WebUI data, not Ollama models.
```

Do not use `localhost:11434` inside the Open WebUI container. In a container, `localhost` refers to the Open WebUI container itself.

### Models do not appear

Download the models natively, then refresh Open WebUI:

```bash
ollama pull qwen3:8b
ollama pull llama3.2:3b
```

### Reset Open WebUI data

This removes the local Open WebUI account and conversation data:

```bash
docker compose down

docker volume rm llm-language-learning-tool_open_webui_data

ollama serve
docker compose up -d open-webui
```

Use this only when you intentionally want to reset the local UI data.

## Related Documentation

- [README.md](../README.md)
- [Ollama guide](ollama.md)
- [SPEC.md](../SPEC.md)
- [docker-compose.yml](../docker-compose.yml)
