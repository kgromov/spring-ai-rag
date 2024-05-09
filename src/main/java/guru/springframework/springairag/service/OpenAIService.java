package guru.springframework.springairag.service;

import guru.springframework.springairag.model.Answer;
import guru.springframework.springairag.model.Question;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OpenAIService {
    private final ChatClient chatClient;
    private final EmbeddingClient embeddingClient;
    private final VectorStore vectorStore;

    public Answer getAnswer(Question question) {
        var promptTemplate = new PromptTemplate(question.question());
        var prompt = promptTemplate.create();
        var response = chatClient.call(prompt);
        return new Answer(response.getResult().getOutput().getContent());
    }
}
