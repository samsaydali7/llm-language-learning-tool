# LLM Language Learning Tool

## Abstract

This project is a private, local-first language learning application built around a simple principle: turn a user’s own study materials and their associated audio into a structured learning system that can generate relevant exercises, review material, and support study by topic, chapter, and source reference.

The system is designed to be language-agnostic and extensible to other books, languages, and learning materials, as long as the content is owned or legally authorized for the user’s personal study use.

The goal is not to build a generic chatbot or a public SaaS product. Instead, the project focuses on a personal study workflow that runs privately on the user’s machine, preserves source provenance, and uses local AI models to transform authorized educational content into usable learning knowledge.

## Problem

Most language learners study from books, PDF course materials, and audio resources, but these assets are usually fragmented. The text, structure, audio, and explanations are spread across different sources and are not naturally turned into reusable study material.

This project addresses that gap by extracting meaning from the user’s authorized materials, organizing it into a knowledge base, and generating learning activities directly from the user’s selected scope.

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

The system identifies meaningful language-learning data, including:

- vocabulary
- grammar
- examples
- expressions
- topics
- useful phrases and notes
- source references such as chapter, page, and book location

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

The design supports both:

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

## Architecture Summary

The planned architecture includes:

- Angular frontend for user interaction
- Java + Spring Boot backend
- PostgreSQL for persistent data
- RabbitMQ for async processing jobs
- local file storage for PDFs and audio
- local LLM providers through an abstraction layer
- a knowledge base that stores extracted learning content and source provenance

The system is designed around local models and a small development target, with the expectation that the app should run on a typical developer machine such as a MacBook Air with 16 GB memory.

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

## Status

V1 planning is complete and the project is being prepared as a public repository with a minimal, focused documentation set.

## Intended Outcome

The final product is intended to be a private digital tutor that turns books and audio into an organized language study system, with the learner always anchored to the original source material and supported by local AI generation.
