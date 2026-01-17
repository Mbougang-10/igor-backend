package com.yow.access.dto;

import com.yow.access.entities.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ResourceTreeResponse {

    private UUID id;
    private String name;
    private String type;
    private List<ResourceTreeResponse> children = new ArrayList<>();

    public static ResourceTreeResponse fromEntity(Resource resource) {
        ResourceTreeResponse dto = new ResourceTreeResponse();
        dto.id = resource.getId();
        dto.name = resource.getName();
        dto.type = resource.getType();
        return dto;
    }

    public void addChild(ResourceTreeResponse child) {
        this.children.add(child);
    }

    // getters only (immutabilité côté API)
    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public List<ResourceTreeResponse> getChildren() { return children; }
}
