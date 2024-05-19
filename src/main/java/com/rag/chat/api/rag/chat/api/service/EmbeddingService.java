//package com.rag.chat.api.rag.chat.api.service;
//
//import org.springframework.ai.document.Document;
//import org.springframework.ai.embedding.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.util.Assert;
//import org.springframework.web.bind.annotation.RequestParam;
//
//import java.util.List;
//import java.util.Map;
//
//@Service
//public class EmbeddingService {
//    private final EmbeddingClient embeddingClient;
//
//    @Autowired
//    public EmbeddingService(EmbeddingClient embeddingClient) {
//        this.embeddingClient = embeddingClient;
//    }
//
//    public List<Double>  embed(  String message) {
//        return this.embeddingClient.embed(message);
//    }
//
//    public List<Double> embed(Document document) {
//        List<Double> embedResponse =  this.embeddingClient.embed(document);
//        System.out.println("embedded-response:"+embedResponse.size());
//        return embedResponse;
//    }
//     public int dimensions() {
//        return this.embed("Test String").size();
//    }
//
//
//}
