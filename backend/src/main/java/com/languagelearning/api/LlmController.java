package com.languagelearning.api;

import java.util.List;

import com.languagelearning.llm.LlmProvider;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class LlmController {
    private final LlmProvider provider;

    public LlmController(LlmProvider provider) {
        this.provider = provider;
    }

    @GetMapping("/models")
    public ModelsResponse models() {
        return new ModelsResponse(provider.listModels());
    }

    @PostMapping("/generate")
    @ResponseStatus(HttpStatus.OK)
    public GenerateResponse generate(@Valid @RequestBody GenerateRequest request) {
        return new GenerateResponse(provider.generate(request.model(), request.prompt(), request.maxTokens()));
    }

    public record ModelsResponse(List<String> models) {}

    public record GenerateRequest(
            String model,
            @NotBlank String prompt,
            @Min(1) @Max(512) int maxTokens) {}

    public record GenerateResponse(String response) {}
}
