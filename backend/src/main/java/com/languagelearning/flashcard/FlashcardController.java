package com.languagelearning.flashcard;

import com.languagelearning.scope.api.StudyScopeRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/flashcards")
@RequiredArgsConstructor
public class FlashcardController {

    private final FlashcardService flashcardService;

    @PostMapping("/generate")
    public List<Flashcard> generate(@RequestBody StudyScopeRequest scope) {
        return flashcardService.generate(scope.toScope());
    }
}
