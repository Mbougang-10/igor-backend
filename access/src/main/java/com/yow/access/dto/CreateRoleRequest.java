package com.yow.access.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateRoleRequest {

    @NotBlank(message = "Role name is required")
    private String name;

    private String scope; // "GLOBAL" or "TENANT"

    private List<Short> permissionIds; // IDs des permissions à assigner
}
