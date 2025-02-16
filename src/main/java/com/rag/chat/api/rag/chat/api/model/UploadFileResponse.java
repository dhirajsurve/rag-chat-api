package com.rag.chat.api.rag.chat.api.model;


public class UploadFileResponse {
    private String object;
    private String id;
    private String purpose;
    private String filename;

    private int bytes;
    private long created_at;
    private String status;
    private String status_details;

    public String getId() {
        return id;
    }

    public int getBytes() {
        return bytes;
    }

    public void setBytes(int bytes) {
        this.bytes = bytes;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getCreated_at() {
        return created_at;
    }

    public void setCreated_at(long created_at) {
        this.created_at = created_at;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus_details() {
        return status_details;
    }

    public void setStatus_details(String status_details) {
        this.status_details = status_details;
    }

    // Getters and setters omitted for brevity
}
