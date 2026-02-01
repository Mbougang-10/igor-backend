package com.yow.access.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleAssignmentRequest {

    @NotNull
    private UUID targetUserId;

    @NotNull
    private Short roleId;

    @NotNull
    private UUID resourceId;

    // getters / setters
}
