package guru.springframework.springairag.service;

import guru.springframework.springairag.model.Answer;
import guru.springframework.springairag.model.Question;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AIService {
    private final ChatClient chatClient;

    public Answer getAnswer(Question question) {
        return new Answer(
                chatClient.prompt()
                        .user(question.question())
                        .call()
                        .content()
        );
    }
}
