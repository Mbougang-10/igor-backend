package com.yow.access.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class MoveResourceRequest {
    @NotNull
    private UUID newParentId;

    public MoveResourceRequest() {
    }

    public MoveResourceRequest(UUID newParentId) {
        this.newParentId = newParentId;
    }

    public UUID getNewParentId() {
        return newParentId;
    }
}
