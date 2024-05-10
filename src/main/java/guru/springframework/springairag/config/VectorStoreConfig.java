package guru.springframework.springairag.config;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StopWatch;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@Configuration
public class VectorStoreConfig {

    @Bean
    SimpleVectorStore simpleVectorStore(EmbeddingClient embeddingClient, VectorStoreProperties vectorStoreProperties) {
        var store =  new SimpleVectorStore(embeddingClient);
        File vectorStoreFile = new File(vectorStoreProperties.vectorStorePath());
        if (vectorStoreFile.exists()) {
            this.loadData(store, vectorStoreFile);
        } else {
            this.saveData(vectorStoreProperties, store, vectorStoreFile);
        }
        return store;
    }

    private void loadData(SimpleVectorStore store, File vectorStoreFile) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("Load data from vector store");
        try {
            store.load(vectorStoreFile);
        } finally {
            stopWatch.stop();
            var taskInfo = stopWatch.lastTaskInfo();
            log.info("Time to {} = {} ms", taskInfo.getTaskName(), taskInfo.getTimeMillis());
        }
    }

    @SneakyThrows
    private void saveData(VectorStoreProperties vectorStoreProperties, SimpleVectorStore store, File vectorStoreFile) {
        log.info("Loading documents into vector store");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("Save data to vector store");
        try {
            vectorStoreProperties.documentsToLoad().forEach(document -> {
                log.debug("Loading document: " + document.getFilename());
                TikaDocumentReader documentReader = new TikaDocumentReader(document);
                List<Document> docs = documentReader.get();
                TextSplitter textSplitter = new TokenTextSplitter();
                List<Document> splitDocs = textSplitter.apply(docs);
                store.add(splitDocs);
            });
            Files.createFile(Paths.get(vectorStoreFile.getAbsolutePath()));
            store.save(vectorStoreFile);
        } finally {
            stopWatch.stop();
            var taskInfo = stopWatch.lastTaskInfo();
            log.info("Time to {} = {} ms", taskInfo.getTaskName(), taskInfo.getTimeMillis());
        }
    }
}
