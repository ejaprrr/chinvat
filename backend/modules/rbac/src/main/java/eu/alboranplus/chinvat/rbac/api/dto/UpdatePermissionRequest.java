package eu.alboranplus.chinvat.rbac.api.dto;

import jakarta.validation.constraints.Size;

public record UpdatePermissionRequest(@Size(max = 255) String description) {}
