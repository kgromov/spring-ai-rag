package guru.springframework.springairag.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.postretrieval.document.DocumentPostProcessor;
import org.springframework.ai.rag.util.PromptAssert;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.List;

import static java.util.Optional.ofNullable;

@Slf4j
public class CustomDocumentPostProcessor implements DocumentPostProcessor {

    private final ChatClient chatClient;

    private final PromptTemplate promptTemplate;

    public CustomDocumentPostProcessor(ChatClient.Builder chatClientBuilder, PromptTemplate promptTemplate) {
        Assert.notNull(chatClientBuilder, "chatClientBuilder cannot be null");
        Assert.notNull(promptTemplate, "promptTemplate cannot be null");
        this.chatClient = chatClientBuilder.build();
        this.promptTemplate = promptTemplate;
        PromptAssert.templateHasRequiredPlaceholders(this.promptTemplate, "input", "documents");
    }

    @Override
    public List<Document> process(Query query, List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return documents;
        }

        log.debug("Compressing documents for query: {}", query.text());

        return documents.stream()
                .map(document -> document.mutate()
                        .text(this.chatClient.prompt()
                                .user(user -> user.text(this.promptTemplate.getTemplate())
                                        .param("documents", ofNullable(document.getText()).orElse(""))
                                        .param("input", query.text()))
                                .options(ChatOptions.builder()
                                        .temperature(0.2)
                                        .build())
                                .call()
                                .content())
                        .build())
                .toList();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ChatClient.Builder chatClientBuilder;
        private PromptTemplate promptTemplate;

        private Builder() {
        }

        public Builder chatClientBuilder(ChatClient.Builder chatClientBuilder) {
            this.chatClientBuilder = chatClientBuilder;
            return this;
        }

        public Builder promptTemplate(PromptTemplate promptTemplate) {
            this.promptTemplate = promptTemplate;
            return this;
        }

        public CustomDocumentPostProcessor build() {
            return new CustomDocumentPostProcessor(this.chatClientBuilder, this.promptTemplate);
        }
    }

}
