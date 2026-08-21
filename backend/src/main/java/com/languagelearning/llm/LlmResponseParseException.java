package com.languagelearning.llm;

/** Thrown when a local model's response cannot be parsed into the expected structured result. */
public class LlmResponseParseException extends RuntimeException {

    public LlmResponseParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public LlmResponseParseException(String message) {
        super(message);
    }
}
