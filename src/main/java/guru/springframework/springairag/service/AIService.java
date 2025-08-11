package guru.springframework.springairag.service;

import guru.springframework.springairag.model.Answer;
import guru.springframework.springairag.model.Question;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.joining;

@Service
@Slf4j
public class AIService {
    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    @Value("classpath:/prompts/rag-prompt-template-meta.st")
    private Resource promptResource;

    public AIService(ChatClient.Builder builder, VectorStore vectorStore) {
        this.chatClient = builder.build();
        this.vectorStore = vectorStore;
    }

    public Answer getAnswer(Question question) {
        var searchRequest = SearchRequest.builder()
                .query(question.question())
                .similarityThreshold(0.5)
                .topK(4)
                .build();
        List<Document> documents = vectorStore.similaritySearch(searchRequest);
        String documentsContent = documents.stream().map(Document::getText).collect(joining("\n"));

        var promptTemplate = new PromptTemplate(promptResource);
        var prompt = promptTemplate.create(
                Map.of(
                        "input", question.question(),
                        "documents", documentsContent
                )
        );


        return new Answer(
                chatClient.prompt(prompt)
                        .user(question.question())
                        .call()
                        .content()
        );
    }
}
