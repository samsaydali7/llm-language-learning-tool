package com.languagelearning.llm;

import java.util.List;

public interface LlmProvider {
    List<String> listModels();
    String generate(String model, String prompt, int maxTokens);
}
