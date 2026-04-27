package eu.alboranplus.chinvat.rbac.api.controller;

import eu.alboranplus.chinvat.rbac.api.dto.RoleResponse;
import eu.alboranplus.chinvat.rbac.api.exception.RbacApiExceptionHandler.RbacErrorResponse;
import eu.alboranplus.chinvat.rbac.api.mapper.RbacApiMapper;
import eu.alboranplus.chinvat.rbac.application.dto.RoleView;
import eu.alboranplus.chinvat.rbac.application.facade.RbacFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
}
