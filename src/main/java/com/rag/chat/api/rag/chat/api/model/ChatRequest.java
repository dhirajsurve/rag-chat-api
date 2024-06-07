package com.rag.chat.api.rag.chat.api.model;
public   class ChatRequest {
    private String prompt;
    private String fileName;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    // Getter and Setter
    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    @Override
    public String toString() {
        return "ChatRequest{" +
                "prompt='" + prompt + '\'' +
                ", fileName='" + fileName + '\'' +
                '}';
    }
}
