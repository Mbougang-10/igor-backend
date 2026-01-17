package com.yow.access.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class CreateResourceRequest {

    @NotNull
    private UUID parentResourceId;

    @NotBlank
    private String name;

    @NotBlank
    private String type;

    public UUID getParentResourceId() {
        return parentResourceId;
    }

    public void setParentResourceId(UUID parentResourceId) {
        this.parentResourceId = parentResourceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
