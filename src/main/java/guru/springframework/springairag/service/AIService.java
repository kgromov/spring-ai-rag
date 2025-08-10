package guru.springframework.springairag.service;

import guru.springframework.springairag.model.Answer;
import guru.springframework.springairag.model.Question;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
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

    @Value("classpath:/prompts/rag-prompt-template-meta.st")
    private Resource promptResource;

    public AIService(ChatClient.Builder builder, VectorStore vectorStore) {
        this.chatClient = builder
                .defaultAdvisors(RetrievalAugmentationAdvisor.builder()
                        .documentRetriever(VectorStoreDocumentRetriever.builder()
                                .vectorStore(vectorStore)
                                .similarityThreshold(0.5)
                                .topK(4)
                                .build()
                        )
                        // TODO:
//                        .documentPostProcessors((query, documents) -> builder.build().prompt())
                        .build()
                )
                .build();
    }

    public Answer getAnswer(Question question) {
        var promptTemplate = new PromptTemplate(promptResource);
        var prompt = promptTemplate.create(
                Map.of(
                        "input", question.question()
//                        "documents", documentsContent
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
