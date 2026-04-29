package eu.alboranplus.chinvat.rbac.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Permission definition")
public record PermissionResponse(
    @Schema(description = "Unique permission code", example = "USERS:MANAGE") String code,
    @Schema(description = "Human readable description", example = "Manage users")
        String description) {}
