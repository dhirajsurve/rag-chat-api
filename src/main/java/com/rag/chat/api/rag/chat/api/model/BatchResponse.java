package com.rag.chat.api.rag.chat.api.model;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Map;

public class BatchResponse {
    private String id;
    private String object;
    private String endpoint;
    private String errors;

    @JsonProperty("input_file_id")
    private String inputFileId;

    @JsonProperty("completion_window")
    private String completionWindow;

    private String status;

    @JsonProperty("output_file_id")
    private String outputFileId;

    @JsonProperty("error_file_id")
    private String errorFileId;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("in_progress_at")
    private Instant inProgressAt;

    @JsonProperty("expires_at")
    private Instant expiresAt;

    @JsonProperty("finalizing_at")
    private Instant finalizingAt;

    @JsonProperty("completed_at")
    private Instant completedAt;

    @JsonProperty("failed_at")
    private Instant failedAt;

    @JsonProperty("expired_at")
    private Instant expiredAt;

    @JsonProperty("cancelling_at")
    private Instant cancellingAt;

    @JsonProperty("cancelled_at")
    private Instant cancelledAt;

    @JsonProperty("request_counts")
    private Map<String, Integer> requestCounts;

    private String metadata;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getErrors() {
        return errors;
    }

    public void setErrors(String errors) {
        this.errors = errors;
    }

    public String getInputFileId() {
        return inputFileId;
    }

    public void setInputFileId(String inputFileId) {
        this.inputFileId = inputFileId;
    }

    public String getCompletionWindow() {
        return completionWindow;
    }

    public void setCompletionWindow(String completionWindow) {
        this.completionWindow = completionWindow;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOutputFileId() {
        return outputFileId;
    }

    public void setOutputFileId(String outputFileId) {
        this.outputFileId = outputFileId;
    }

    public String getErrorFileId() {
        return errorFileId;
    }

    public void setErrorFileId(String errorFileId) {
        this.errorFileId = errorFileId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getInProgressAt() {
        return inProgressAt;
    }

    public void setInProgressAt(Instant inProgressAt) {
        this.inProgressAt = inProgressAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getFinalizingAt() {
        return finalizingAt;
    }

    public void setFinalizingAt(Instant finalizingAt) {
        this.finalizingAt = finalizingAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public Instant getFailedAt() {
        return failedAt;
    }

    public void setFailedAt(Instant failedAt) {
        this.failedAt = failedAt;
    }

    public Instant getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(Instant expiredAt) {
        this.expiredAt = expiredAt;
    }

    public Instant getCancellingAt() {
        return cancellingAt;
    }

    public void setCancellingAt(Instant cancellingAt) {
        this.cancellingAt = cancellingAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(Instant cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public Map<String, Integer> getRequestCounts() {
        return requestCounts;
    }

    public void setRequestCounts(Map<String, Integer> requestCounts) {
        this.requestCounts = requestCounts;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
// Getters and Setters
}
