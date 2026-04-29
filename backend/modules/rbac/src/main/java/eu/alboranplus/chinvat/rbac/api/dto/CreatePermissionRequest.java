package eu.alboranplus.chinvat.rbac.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePermissionRequest(
    @NotBlank @Size(max = 120) String code,
    @Size(max = 255) String description) {}
