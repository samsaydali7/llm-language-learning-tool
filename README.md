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

- no hard dependency on a single model vendor
- local execution only for V1
- simple provider abstraction for extraction and exercise generation
- configurable model routing
- modern deployment via Docker Compose

## Repository Documents

This repository currently contains the core planning documents:

- [REQUIREMENTS.md](REQUIREMENTS.md) — product requirements for V1
- [SPEC.md](SPEC.md) — technical specification and system design

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

V1 planning is complete and the project is being prepared as a public repository with a minimal, focused documentation set.

## Intended Outcome

The final product is intended to be a private digital tutor that turns user-owned materials and audio into an organized language study system, with the learner always anchored to the original source material and supported by local AI generation.
