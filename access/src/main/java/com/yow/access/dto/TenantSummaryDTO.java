package com.yow.access.dto;

import java.time.Instant;
import java.util.UUID;

public class TenantSummaryDTO {
    private UUID id;
    private String name;
    private String code;
    private String status;
    private Instant createdAt;
    private String ownerName;
    private String ownerEmail;

    public TenantSummaryDTO(UUID id, String name, String code, String status, Instant createdAt, String ownerName, String ownerEmail) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.status = status;
        this.createdAt = createdAt;
        this.ownerName = ownerName;
        this.ownerEmail = ownerEmail;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public String getOwnerEmail() { return ownerEmail; }
    public void setOwnerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; }
}
