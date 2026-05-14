package eu.alboranplus.chinvat.rbac.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "New permission payload")
public record CreatePermissionRequest(
    @Schema(description = "Unique permission code", example = "USERS:MANAGE")
        @NotBlank @Size(max = 120) String code,
    @Schema(description = "Human-readable description", example = "Allows managing user accounts")
        @Size(max = 255) String description) {}
