# Language Learning Engine

## Version

V1.0

## Status

Final

## Purpose

A private, local-first, language-agnostic language learning application that converts textbooks and associated audio into a structured knowledge base and uses that knowledge base to generate personalized learning activities.

The first corpus is:

French for Dummies 6-in-1

The first target language is:

French

The architecture must not contain French-specific business logic and must support additional languages and books in the future.

## 1. Goals

The application must allow the user to:

Create a language.
Create a learning corpus/book.
Upload a complete PDF.
Upload associated audio files.
Extract the textbook structure.
Extract existing audio transcripts from the PDF.
Map audio references in the PDF to audio files.
Extract vocabulary.
Extract grammar.
Extract expressions.
Extract examples.
Extract topics.
Store source provenance.
Browse the extracted knowledge.
Select learning scope.
Generate exercises by topic/chapter/etc.
Generate flashcards.
Perform simple listening exercises.
Review grammar.
Track exercise failures.
Connect failures to grammar/vocabulary/topics.
Use failures for future review.

## 2. Exercise Generation Modes

The application must support both persistent, job-based exercise generation and ad hoc, on-demand generation from a selected knowledge subset.

### 2.1 Persistent Exercise Generation

The default generation flow for V1 shall be asynchronous and stored.

The system shall:

Create an exercise-generation job for a selected scope such as a topic, chapter, section, book, or combination of filters.
Read the relevant knowledge items from the knowledge base.
Generate a batch of exercises based on that scope.
Persist the generated exercises and metadata.
Associate each exercise with its source references and knowledge items.
Allow the user to review, retry, and revisit the generated set later.

This mode is intended for structured learning sessions, review cycles, and repeated practice.

### 2.2 On-Demand Generation

The application shall also support immediate generation for targeted, user-driven requests based on a narrow knowledge selection.

Examples include:

Generate 3 fill-in-the-blank exercises from the Restaurant topic.
Generate 5 grammar questions from the selected vocabulary entries.
Generate a quick listening quiz from a selected chapter section.

This mode shall:

Use a narrowly scoped subset of the user's selected knowledge.
Allow fast generation for a small number of exercises.
Optionally persist the generated result so it can be reviewed or repeated later.
Respect source provenance and keep generated exercises linked to their originating material.

### 2.3 Hybrid Model

V1 shall use a hybrid approach:

Queued generation is the default for durable learning sessions and review routines.
On-demand generation is available for quick, targeted queries during browsing or exploration of the knowledge base.

The two modes shall share the same generation pipeline and validation rules, while differing in scope, persistence, and expected latency.

## 3. Non-Goals for V1

The following are explicitly excluded:

Speech recognition
Pronunciation scoring
Speaking evaluation
AI conversation
Automatic essay correction
Mobile applications
Social features
Gamification
Public user accounts
Cloud deployment
Advanced adaptive curriculum
Advanced spaced repetition
Claude API dependency

These can be added later.

## 3. Core Architecture

                    USER
                     |
                     v
                WEB FRONTEND
                     |
                     v
                SPRING BOOT
                     |
        +------------+------------+
        |            |            |
        v            v            v
   PostgreSQL     RabbitMQ     File Storage
        |            |
        |            v
        |       Processing Jobs
        |            |
        |      +-----+------+
        |      |            |
        |      v            v
        |     QWEN        LLAMA
        |  EXTRACTION    EXERCISES
        |      |            |
        +------+------------+
               |
               v
        KNOWLEDGE BASE
               |
       +-------+-------+
       |       |       |
       v       v       v
   Exercises Flashcards Listening
       |
       v
  User Attempts
       |
       v
 Failed Knowledge
       |
       v
 Grammar/Vocabulary Review

## 4. Technology Stack

### Backend

Java + Spring Boot

### Frontend

Angular

### Database

PostgreSQL

### Messaging

RabbitMQ

### PDF processing

Apache PDFBox

### Local LLM runtime

Ollama

### Extraction LLM

Qwen

Recommended initial model:

Qwen3 8B class model

### Exercise LLM

Llama

Recommended initial model:

Llama 3.x 8B class model

The exact model versions should be configurable.

### Deployment

Docker Compose

## 5. Hardware Target

The initial development machine:

MacBook Air M4
16 GB unified memory

The application should therefore be designed around relatively small quantized local models.

The system must not require a 30B/70B model.

## 6. Local-First Principle

The application must work without external LLM APIs.

V1:

Qwen  -> local
Llama -> local
DB    -> local
Files -> local

Claude may be introduced later as an optional provider.

## 7. LLM Abstraction

The application must not directly depend on Qwen or Llama implementations.

Use an abstraction:

public interface LlmProvider {


    KnowledgeExtractionResult extractKnowledge(
        KnowledgeExtractionRequest request
    );


    ExerciseGenerationResult generateExercises(
        ExerciseGenerationRequest request
    );


    GrammarReviewResult generateGrammarReview(
        GrammarReviewRequest request
    );
}

Providers:

LlmProvider
    |
    +-- QwenProvider
    |
    +-- LlamaProvider
    |
    +-- ClaudeProvider (future)

## 8. Model Routing

Configuration:

llm:


  extraction:
    provider: qwen
    model: qwen3:8b


  exercises:
    provider: llama
    model: llama3.1:8b


  grammar-review:
    provider: llama
    model: llama3.1:8b
