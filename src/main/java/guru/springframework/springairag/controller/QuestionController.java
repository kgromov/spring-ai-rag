package guru.springframework.springairag.controller;

import guru.springframework.springairag.model.Answer;
import guru.springframework.springairag.model.Question;
import guru.springframework.springairag.service.OpenAIService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class QuestionController {
    private final OpenAIService openAIService;

    @PostMapping("/ask")
    private Answer ask(@RequestBody Question question) {
        return openAIService.getAnswer(question);
    }
}
