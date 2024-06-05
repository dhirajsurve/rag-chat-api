package com.rag.chat.api.rag.chat.api.processor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rag.chat.api.rag.chat.api.service.TogetherAiService;
import com.rag.chat.api.rag.chat.api.service.VectorStoreService;
import jakarta.annotation.PostConstruct;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import com.google.gson.Gson;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static com.rag.chat.api.rag.chat.api.utils.DoubleArrayConverter.convertListToArray;

@Component
public class PdfFileReader {
//    @Value("classpath:RFP.pdf")
//    private Resource pdfResource;
    private final VectorStoreService vectorStoreService;

    private final TogetherAiService togetherAiService;
    public PdfFileReader(VectorStoreService vectorStoreService, TogetherAiService togetherAiService) {
        this.vectorStoreService = vectorStoreService;
        this.togetherAiService = togetherAiService;
    }

   // @Async
    public void pdfEmbedding(MultipartFile file) {
         var config = PdfDocumentReaderConfig.builder()
                .withPageExtractedTextFormatter(new ExtractedTextFormatter.Builder().build())
                .build();

            var pdfReader = new PagePdfDocumentReader(file.getResource(), config);
            var textSplitter = new TokenTextSplitter();
            Gson gson = new Gson();
           // vectorStoreService.add(textSplitter.apply(pdfReader.get()));
            textSplitter.apply(pdfReader.get()).forEach(document -> {
                vectorStoreService.createVectorStore(
                        document.getContent(),
                        gson.toJson(document.getMetadata()),
                         togetherAiService.embedd(document.getContent())
                );
            });


    }

//    @Async
//    public void pdfEmbedding(MultipartFile pdfResource) {
//
//        var config = PdfDocumentReaderConfig.builder()
//                .withPageExtractedTextFormatter(
//                        new ExtractedTextFormatter.Builder()
//                                .build())
//                .build();
//
//        var pdfReader = new PagePdfDocumentReader(pdfResource, config);
//        var textSplitter = new TokenTextSplitter();
//        Gson gson = new Gson();
//        textSplitter.apply(pdfReader.get()).forEach(document -> {
//                vectorStoreService.createVectorStore(document.getContent(),gson.toJson(document.getMetadata()),convertListToArray(embeddingClient.embed(document)));
//        });
//
//    }
}
