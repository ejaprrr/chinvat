package eu.alboranplus.chinvat.rbac.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Permission update payload — only description can be updated")
public record UpdatePermissionRequest(
    @Schema(description = "Updated human-readable description",
        example = "Allows managing user accounts")
        @Size(max = 255) String description) {}
