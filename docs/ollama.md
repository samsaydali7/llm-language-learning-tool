# Ollama on the Developer Host

## What Is Ollama?

Ollama is a local runtime for running language models on your own computer. It provides a command-line interface and a local HTTP API that applications can use to send prompts to installed models.

This project runs Ollama natively on the developer host. Docker is used only for PostgreSQL, RabbitMQ, and Open WebUI. Native execution can use hardware acceleration when supported by the host.

## Runtime Architecture

```text
Developer host
  |
  +--> Ollama :11434
  |      |
  |      +--> qwen3:8b       extraction
  |      +--> llama3.2:3b    exercises
  |
  +--> Docker Compose
         |
         +--> PostgreSQL
         +--> RabbitMQ
         +--> Open WebUI :3000
                         |
                         +--> host.docker.internal:11434
```

The repository intentionally has one Ollama runtime configuration. This avoids duplicate model storage, port conflicts, and unnecessary memory usage.

## Install Ollama

Install Ollama for your operating system using the [official Ollama download page](https://ollama.com/download). Supported installation paths include macOS, Linux, and Windows.

After installation, verify that the command is available:

```bash
ollama --version
```

Start the local Ollama server:

```bash
ollama serve
```

Keep this process running. Ollama exposes its API at:

```text
http://localhost:11434
```

Verify the API:

```bash
curl http://localhost:11434/api/tags
```

## Install the Project Models

Download the configured models from a second terminal:

```bash
ollama pull qwen3:8b
ollama pull llama3.2:3b
```

List installed models:

```bash
ollama list
```

The default routing is:

```env
EXTRACTION_MODEL=qwen3:8b
EXERCISE_MODEL=llama3.2:3b
```

These values are configuration, not hard-coded application behavior. They can be changed in `.env` when a different local model is preferred.

## Start the Supporting Services

With Ollama running natively, start the Docker services:

```bash
cp .env.example .env
docker compose up -d postgres rabbitmq open-webui
```

The Compose services are:

```text
postgres       structured data
rabbitmq       asynchronous processing jobs
open-webui     browser UI for testing Ollama
```

Open WebUI is available at:

[http://localhost:3000](http://localhost:3000)

Inside Docker, Open WebUI reaches native Ollama through:

```text
http://host.docker.internal:11434
```

This value is configured in `.env.example` as `OLLAMA_BASE_URL`.

## Test a Model

Run an interactive test:

```bash
ollama run llama3.2:3b
```

For extraction testing:

```bash
ollama run qwen3:8b
```

For a one-shot API request:

```bash
curl http://localhost:11434/api/generate \
  -d '{
    "model": "llama3.2:3b",
    "prompt": "Answer with one short sentence: What is a noun?",
    "stream": false,
    "options": {
      "num_predict": 32,
      "temperature": 0.2
    }
  }'
```

## Model Memory Management

Ollama loads models when they are needed. You can inspect currently loaded models:

```bash
ollama ps
```

Unload a model from memory without deleting it:

```bash
ollama stop qwen3:8b
ollama stop llama3.2:3b
```

Delete a model only when you want to remove its local files:

```bash
ollama rm qwen3:8b
```

The model remains installed after `ollama stop`; it is only removed from memory. This is useful on machines with limited RAM:

```text
Extraction session  -> stop Llama, use Qwen
Exercise session    -> stop Qwen, use Llama
```

## Model Routing Rationale

The extraction and exercise workloads have different requirements:

| Workload | Priority | Default model |
|---|---|---|
| Knowledge extraction | quality and structured output | Qwen 8B |
| Exercise generation | speed, privacy, and lower memory use | Llama 3B |
| Grammar review | configurable quality/cost balance | local provider by default |

The LLM provider abstraction also leaves room for a stronger or cloud-backed extraction provider later, while frequent exercise generation can remain on a fast local model.

## Claude Is a Separate Provider

Claude cannot run through Ollama. Claude is an Anthropic-hosted model, not a locally installable model, so it requires a separate Anthropic provider, internet access, and an Anthropic API key.

The intended architecture is:

```text
LlmProvider
  |
  +--> OllamaProvider
  |      +--> Qwen
  |      +--> Llama
  |
  +--> ClaudeProvider
         +--> Anthropic API
```

This allows Claude to be selected for quality-sensitive extraction while Ollama continues to handle local exercise generation. Provider selection is configuration, so adding Claude later should not require changing the Ollama integration.

Example routing:

```yaml
llm:
  extraction:
    provider: claude
    model: claude-sonnet

  exercises:
    provider: ollama
    model: llama3.2:3b
```

The default V1 workflow remains fully local. Claude is an optional future provider and must never be required for the local application to run.

## Troubleshooting

### Ollama is not running

```bash
ollama serve
curl http://localhost:11434/api/tags
```

### Open WebUI cannot connect to Ollama

Check the host API:

```bash
curl http://localhost:11434/api/tags
```

Check the configured endpoint:

```bash
docker compose config | grep OLLAMA_BASE_URL
```

The expected value is:

```text
http://host.docker.internal:11434
```

Do not use `localhost:11434` as the endpoint inside Open WebUI. In a container, `localhost` refers to the Open WebUI container itself.

### Models are too slow

Use the smaller exercise model and unload the other model:

```bash
ollama stop qwen3:8b
ollama run llama3.2:3b
```

### Models use too much disk space

List installed models:

```bash
ollama list
```

Remove an unused model:

```bash
ollama rm MODEL_NAME
```

## Privacy Boundary

Ollama runs locally and does not require an external LLM API for the default workflow. Use only materials the user owns or is authorized to study.

Do not commit PDFs, audio, copyrighted passages, model files, private prompts, or exported Open WebUI data to the repository.

## Related Documentation

- [README.md](../README.md)
- [Open WebUI guide](open-webui.md)
- [SPEC.md](../SPEC.md)
- [docker-compose.yml](../docker-compose.yml)
- [.env.example](../.env.example)
