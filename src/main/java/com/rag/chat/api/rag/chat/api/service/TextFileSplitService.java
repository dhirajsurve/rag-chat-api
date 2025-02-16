package com.rag.chat.api.rag.chat.api.service;

import com.google.gson.Gson;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class TextFileSplitService {
    private final VectorStoreService vectorStoreService;
    private final TogetherAiService togetherAiService;

    public TextFileSplitService(VectorStoreService vectorStoreService, TogetherAiService togetherAiService) {
        this.vectorStoreService = vectorStoreService;
        this.togetherAiService = togetherAiService;
    }

    public List<String> splitFile(InputStream inputStream, int chunkSize) throws IOException {
        List<String> chunks = new ArrayList<>();
        StringBuilder chunk = new StringBuilder(chunkSize);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (chunk.length() + line.length() > chunkSize) {
                    chunks.add(chunk.toString());
                    chunk.setLength(0);
                }
                chunk.append(line).append(System.lineSeparator());
            }
            if (chunk.length() > 0) {
                chunks.add(chunk.toString());
            }
        }

        return chunks;
    }


    public void textEmbedding(List<String> chunks, String filename, Long userId) {
        chunks.forEach(document->
            vectorStoreService.createVectorStore(
                    document,
                    filename,
                    togetherAiService.embedd(document
                            .replace("__","")
                            .replace("  ","")
                            .replace("......","")
                            .replace("--","")),
                    userId
            ));


    }
}
