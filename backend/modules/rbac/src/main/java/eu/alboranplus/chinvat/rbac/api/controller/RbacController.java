package eu.alboranplus.chinvat.rbac.api.controller;

import eu.alboranplus.chinvat.rbac.api.dto.CreatePermissionRequest;
import eu.alboranplus.chinvat.rbac.api.dto.PermissionResponse;
import eu.alboranplus.chinvat.rbac.api.dto.RoleResponse;
import eu.alboranplus.chinvat.rbac.api.dto.UpdatePermissionRequest;
import eu.alboranplus.chinvat.rbac.api.dto.UserRolesResponse;
import eu.alboranplus.chinvat.rbac.api.exception.RbacApiExceptionHandler.RbacErrorResponse;
import eu.alboranplus.chinvat.rbac.api.mapper.RbacApiMapper;
import eu.alboranplus.chinvat.rbac.application.dto.PermissionView;
import eu.alboranplus.chinvat.rbac.application.dto.RoleView;
import eu.alboranplus.chinvat.rbac.application.dto.UserRolesView;
import eu.alboranplus.chinvat.rbac.application.facade.RbacFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import eu.alboranplus.chinvat.common.pagination.PageResponse;
import eu.alboranplus.chinvat.common.pagination.PaginationRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "RBAC", description = "Role-based access control — role inspection")
@RestController
@RequestMapping("/api/v1/rbac")
public class RbacController {

  private final RbacFacade rbacFacade;
  private final RbacApiMapper rbacApiMapper;

  public RbacController(RbacFacade rbacFacade, RbacApiMapper rbacApiMapper) {
    this.rbacFacade = rbacFacade;
    this.rbacApiMapper = rbacApiMapper;
  }

  @Operation(
      summary = "Get role by name",
      description = "Returns the role definition including all associated permissions. Requires authentication.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Role found",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = RoleResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized — missing or invalid bearer token",
        content = @Content(schema = @Schema(hidden = true))),
    @ApiResponse(
        responseCode = "404",
        description = "Role not found",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = RbacErrorResponse.class)))
  })
  @GetMapping("/roles/{roleName}")
  public ResponseEntity<RoleResponse> getRole(
      @Parameter(description = "Role name to look up", example = "ADMIN") @PathVariable
          String roleName) {
    RoleView roleView = rbacFacade.getRole(roleName);
    return ResponseEntity.ok(rbacApiMapper.toResponse(roleView));
  }

  @GetMapping("/permissions")
    @Operation(
      summary = "List all permissions",
      description = "Returns all defined permissions. Requires authentication.")
    @ApiResponses({
    @ApiResponse(
      responseCode = "200",
      description = "Permissions returned",
      content =
        @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = PermissionResponse.class))),
    @ApiResponse(
      responseCode = "401",
      description = "Unauthorized — missing or invalid bearer token",
      content = @Content(schema = @Schema(hidden = true)))
    })
  public ResponseEntity<List<PermissionResponse>> listPermissions() {
    List<PermissionResponse> permissions =
        rbacFacade.listPermissions().stream().map(rbacApiMapper::toResponse).toList();
    return ResponseEntity.ok(permissions);
  }

  @PostMapping("/permissions")

    @Operation(
        summary = "List permissions with pagination",
        description = "Returns all defined permissions with pagination support.")
    @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Permissions returned"),
      @ApiResponse(
          responseCode = "401",
          description = "Unauthorized — missing or invalid bearer token",
          content = @Content(schema = @Schema(hidden = true)))
      })
    @GetMapping("/permissions/paged")
    public ResponseEntity<PageResponse<PermissionResponse>> listPermissionsPaged(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) String sort) {
      PaginationRequest paginationRequest = new PaginationRequest(page, size, sort);
      PageResponse<PermissionView> pageResponse = rbacFacade.listPermissionsPaged(paginationRequest);
    
      List<PermissionResponse> responseData = pageResponse.data().stream()
          .map(rbacApiMapper::toResponse).toList();
    
      return ResponseEntity.ok(PageResponse.of(responseData, pageResponse.pagination()));
    }

    @PostMapping("/permissions")
  @PreAuthorize("hasAuthority('RBAC:MANAGE')")
    @Operation(
      summary = "Create permission",
      description = "Creates a new permission. Requires RBAC:MANAGE authority.")
    @ApiResponses({
    @ApiResponse(
      responseCode = "200",
      description = "Permission created",
      content =
        @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = PermissionResponse.class))),
    @ApiResponse(
      responseCode = "401",
      description = "Unauthorized — missing or invalid bearer token",
      content = @Content(schema = @Schema(hidden = true))),
    @ApiResponse(
      responseCode = "403",
      description = "Forbidden — RBAC:MANAGE authority is required",
      content = @Content(schema = @Schema(hidden = true))),
    @ApiResponse(
      responseCode = "409",
      description = "Permission already exists",
      content =
        @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = RbacErrorResponse.class)))
    })
  public ResponseEntity<PermissionResponse> createPermission(
      @Valid @RequestBody CreatePermissionRequest request, Authentication authentication) {
    PermissionView created =
        rbacFacade.createPermission(request.code(), request.description(), actor(authentication));
    return ResponseEntity.ok(rbacApiMapper.toResponse(created));
  }

  @PutMapping("/permissions/{code}")
  @PreAuthorize("hasAuthority('RBAC:MANAGE')")
    @Operation(
      summary = "Update permission",
      description = "Updates an existing permission. Requires RBAC:MANAGE authority.")
    @ApiResponses({
    @ApiResponse(
      responseCode = "200",
      description = "Permission updated",
      content =
        @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = PermissionResponse.class))),
    @ApiResponse(
      responseCode = "401",
      description = "Unauthorized — missing or invalid bearer token",
      content = @Content(schema = @Schema(hidden = true))),
    @ApiResponse(
      responseCode = "403",
      description = "Forbidden — RBAC:MANAGE authority is required",
      content = @Content(schema = @Schema(hidden = true))),
    @ApiResponse(
      responseCode = "404",
      description = "Permission not found",
      content =
        @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = RbacErrorResponse.class)))
    })
  public ResponseEntity<PermissionResponse> updatePermission(
      @PathVariable String code,
      @Valid @RequestBody UpdatePermissionRequest request,
      Authentication authentication) {
    PermissionView updated =
        rbacFacade.updatePermission(code, request.description(), actor(authentication));
    return ResponseEntity.ok(rbacApiMapper.toResponse(updated));
  }

  @DeleteMapping("/permissions/{code}")
  @PreAuthorize("hasAuthority('RBAC:MANAGE')")
    @Operation(
      summary = "Delete permission",
      description = "Deletes a permission by code. Requires RBAC:MANAGE authority.")
    @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Permission deleted"),
    @ApiResponse(
      responseCode = "401",
      description = "Unauthorized — missing or invalid bearer token",
      content = @Content(schema = @Schema(hidden = true))),
    @ApiResponse(
      responseCode = "403",
      description = "Forbidden — RBAC:MANAGE authority is required",
      content = @Content(schema = @Schema(hidden = true))),
    @ApiResponse(
      responseCode = "404",
      description = "Permission not found",
      content =
        @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = RbacErrorResponse.class)))
    })
  public ResponseEntity<Void> deletePermission(@PathVariable String code, Authentication authentication) {
    rbacFacade.deletePermission(code, actor(authentication));
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/users/{userId}/roles")
    @Operation(
      summary = "Get user roles",
      description = "Returns all assigned roles for the given user. Requires authentication.")
    @ApiResponses({
    @ApiResponse(
      responseCode = "200",
      description = "User roles returned",
      content =
        @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = UserRolesResponse.class))),
    @ApiResponse(
      responseCode = "401",
      description = "Unauthorized — missing or invalid bearer token",
      content = @Content(schema = @Schema(hidden = true))),
    @ApiResponse(
      responseCode = "404",
      description = "User not found",
      content =
        @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = RbacErrorResponse.class)))
    })
  public ResponseEntity<UserRolesResponse> getUserRoles(@PathVariable UUID userId) {
    UserRolesView rolesView = rbacFacade.getUserRoles(userId);
    return ResponseEntity.ok(rbacApiMapper.toResponse(rolesView));
  }

  @PostMapping("/users/{userId}/roles/{roleName}")
  @PreAuthorize("hasAuthority('USERS:MANAGE')")
    @Operation(
      summary = "Assign role to user",
      description = "Assigns a role to a user. Requires USERS:MANAGE authority.")
    @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Role assigned"),
    @ApiResponse(
      responseCode = "401",
      description = "Unauthorized — missing or invalid bearer token",
      content = @Content(schema = @Schema(hidden = true))),
    @ApiResponse(
      responseCode = "403",
      description = "Forbidden — USERS:MANAGE authority is required",
      content = @Content(schema = @Schema(hidden = true))),
    @ApiResponse(
      responseCode = "404",
      description = "User or role not found",
      content =
        @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = RbacErrorResponse.class)))
    })
  public ResponseEntity<Void> assignRoleToUser(
      @PathVariable UUID userId, @PathVariable String roleName, Authentication authentication) {
    rbacFacade.assignRoleToUser(userId, roleName, actor(authentication));
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/users/{userId}/roles/{roleName}")
  @PreAuthorize("hasAuthority('USERS:MANAGE')")
    @Operation(
      summary = "Remove role from user",
      description = "Removes a role from a user. Requires USERS:MANAGE authority.")
    @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Role removed"),
    @ApiResponse(
      responseCode = "401",
      description = "Unauthorized — missing or invalid bearer token",
      content = @Content(schema = @Schema(hidden = true))),
    @ApiResponse(
      responseCode = "403",
      description = "Forbidden — USERS:MANAGE authority is required",
      content = @Content(schema = @Schema(hidden = true))),
    @ApiResponse(
      responseCode = "404",
      description = "User or role not found",
      content =
        @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = RbacErrorResponse.class)))
    })
  public ResponseEntity<Void> removeRoleFromUser(
      @PathVariable UUID userId, @PathVariable String roleName, Authentication authentication) {
    rbacFacade.removeRoleFromUser(userId, roleName, actor(authentication));
    return ResponseEntity.noContent().build();
  }

  private static String actor(Authentication authentication) {
    return authentication.getName();
  }
}
