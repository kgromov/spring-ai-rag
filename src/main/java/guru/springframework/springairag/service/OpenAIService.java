package guru.springframework.springairag.service;

import guru.springframework.springairag.model.Answer;
import guru.springframework.springairag.model.Question;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
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
@RequiredArgsConstructor
public class OpenAIService {
    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    @Value("classpath:/prompts/rag-prompt-template-meta.st")
    private Resource promptResource;

    public Answer getAnswer(Question question) {
        var searchRequest = SearchRequest.query(question.question()).withTopK(4);
        List<Document> documents = vectorStore.similaritySearch(searchRequest);
        String documentsContent = documents.stream().map(Document::getContent).collect(joining("\n"));

        var promptTemplate = new PromptTemplate(promptResource);
        var prompt = promptTemplate.create(
                Map.of(
                        "input", question.question(),
                        "documents", documentsContent
                )
        );
        var response = chatClient.call(prompt);
        return new Answer(response.getResult().getOutput().getContent());
    }
}
