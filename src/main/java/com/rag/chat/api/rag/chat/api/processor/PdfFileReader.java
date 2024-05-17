package com.rag.chat.api.rag.chat.api.processor;

import com.rag.chat.api.rag.chat.api.service.EmbeddingService;
import jakarta.annotation.PostConstruct;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class PdfFileReader {
    @Value("classpath:RFP.pdf")
    private Resource pdfResource;

    private final EmbeddingService embeddingService;
    public PdfFileReader(  EmbeddingService embeddingService) {

        this.embeddingService = embeddingService;
    }

    @PostConstruct
    public void pdfEmbedding() {

        var config = PdfDocumentReaderConfig.builder()
                .withPageExtractedTextFormatter(
                        new ExtractedTextFormatter.Builder()
                                .build())
                .build();

        var pdfReader = new PagePdfDocumentReader(pdfResource, config);
        var textSplitter = new TokenTextSplitter();
        textSplitter.apply(pdfReader.get()).forEach(document -> {

            embeddingService.embed(document);

        });

    }
}
