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
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
  public ResponseEntity<List<PermissionResponse>> listPermissions() {
    List<PermissionResponse> permissions =
        rbacFacade.listPermissions().stream().map(rbacApiMapper::toResponse).toList();
    return ResponseEntity.ok(permissions);
  }

  @PostMapping("/permissions")
  public ResponseEntity<PermissionResponse> createPermission(
      @Valid @RequestBody CreatePermissionRequest request) {
    PermissionView created = rbacFacade.createPermission(request.code(), request.description());
    return ResponseEntity.ok(rbacApiMapper.toResponse(created));
  }

  @PutMapping("/permissions/{code}")
  public ResponseEntity<PermissionResponse> updatePermission(
      @PathVariable String code, @Valid @RequestBody UpdatePermissionRequest request) {
    PermissionView updated = rbacFacade.updatePermission(code, request.description());
    return ResponseEntity.ok(rbacApiMapper.toResponse(updated));
  }

  @DeleteMapping("/permissions/{code}")
  public ResponseEntity<Void> deletePermission(@PathVariable String code) {
    rbacFacade.deletePermission(code);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/users/{userId}/roles")
  public ResponseEntity<UserRolesResponse> getUserRoles(@PathVariable Long userId) {
    UserRolesView rolesView = rbacFacade.getUserRoles(userId);
    return ResponseEntity.ok(rbacApiMapper.toResponse(rolesView));
  }

  @PostMapping("/users/{userId}/roles/{roleName}")
  public ResponseEntity<Void> assignRoleToUser(@PathVariable Long userId, @PathVariable String roleName) {
    rbacFacade.assignRoleToUser(userId, roleName, "api");
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/users/{userId}/roles/{roleName}")
  public ResponseEntity<Void> removeRoleFromUser(@PathVariable Long userId, @PathVariable String roleName) {
    rbacFacade.removeRoleFromUser(userId, roleName);
    return ResponseEntity.noContent().build();
  }
}
