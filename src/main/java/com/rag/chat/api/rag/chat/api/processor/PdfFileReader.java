package com.rag.chat.api.rag.chat.api.processor;
import com.rag.chat.api.rag.chat.api.service.TogetherAiService;
import com.rag.chat.api.rag.chat.api.service.VectorStoreService;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Component;
import com.google.gson.Gson;
import org.springframework.web.multipart.MultipartFile;

@Component
public class PdfFileReader {
    private final VectorStoreService vectorStoreService;

    private final TogetherAiService togetherAiService;
    public PdfFileReader(VectorStoreService vectorStoreService, TogetherAiService togetherAiService) {
        this.vectorStoreService = vectorStoreService;
        this.togetherAiService = togetherAiService;
    }

   // @Async
    public void pdfEmbedding(MultipartFile file, Long userId) {
         var config = PdfDocumentReaderConfig.builder()
                .withPageExtractedTextFormatter(new ExtractedTextFormatter.Builder().build())
                .build();

            var pdfReader = new PagePdfDocumentReader(file.getResource(), config);
            var textSplitter = new TokenTextSplitter();
            Gson gson = new Gson();
           // vectorStoreService.add(textSplitter.apply(pdfReader.get()));
            textSplitter.apply(pdfReader.get()).forEach(document -> {
                try {
                    Thread.sleep(3000);
                    System.out.println("Sleeping for 2s to avoid too many calls.");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                  vectorStoreService.createVectorStore(
                        document.getContent(),
                        (String) document.getMetadata().get("file_name"),
                         togetherAiService.embedd(document.getContent()
                                 .replace("__","")
                                 .replace("  ","")
                                 .replace("......","")
                                 .replace("--","")),
                        userId
                );
            });


    }

//    @Async
//    public void pdfEmbedding(MultipartFile pdfResource) {
//
//        var config = PdfDocumentReaderConfig.builder()
//                .withPageExtractedTextFormatter(
//                       p89 new ExtractedTextFormatter.Builder()
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
