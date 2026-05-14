package eu.alboranplus.chinvat.users.api.controller;

import eu.alboranplus.chinvat.common.pagination.PaginationRequest;
import eu.alboranplus.chinvat.common.pagination.PageResponse;
import eu.alboranplus.chinvat.common.api.error.ApiErrorResponse;
import eu.alboranplus.chinvat.users.api.dto.CreateUserRequest;
import eu.alboranplus.chinvat.users.api.dto.UpdateUserRequest;
import eu.alboranplus.chinvat.users.api.dto.UserResponse;
import eu.alboranplus.chinvat.users.api.mapper.UsersApiMapper;
import eu.alboranplus.chinvat.users.application.dto.UserView;
import eu.alboranplus.chinvat.users.application.facade.UsersFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Users", description = "User registration and management")
@RestController
@RequestMapping("/api/v1/users")
public class UsersController {

  private final UsersFacade usersFacade;
  private final UsersApiMapper usersApiMapper;

  public UsersController(UsersFacade usersFacade, UsersApiMapper usersApiMapper) {
    this.usersFacade = usersFacade;
    this.usersApiMapper = usersApiMapper;
  }

  @Operation(
      summary = "Register a new user",
      description = "Creates a new user account. No authentication required.",
      security = {})
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "User created successfully",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = UserResponse.class))),
    @ApiResponse(
        responseCode = "400",
        description = "Validation failed",
        content = @Content(mediaType = "application/json",
          schema = @Schema(implementation = ApiErrorResponse.class))),
    @ApiResponse(
        responseCode = "409",
        description = "Email or username already registered",
        content = @Content(mediaType = "application/json",
          schema = @Schema(implementation = ApiErrorResponse.class)))
  })
  @PostMapping
  public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
    UserView userView = usersFacade.createUser(usersApiMapper.toCommand(request));
    return ResponseEntity.status(HttpStatus.CREATED).body(usersApiMapper.toResponse(userView));
  }

  @Operation(summary = "Get all users")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Paginated list of users"),
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized — missing or invalid bearer token",
        content = @Content(schema = @Schema(hidden = true))),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid pagination parameters",
        content = @Content(mediaType = "application/json",
          schema = @Schema(implementation = ApiErrorResponse.class)))
  })
  @GetMapping
  public ResponseEntity<PageResponse<UserResponse>> getAllUsers(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) String sort) {
    PaginationRequest paginationRequest = new PaginationRequest(page, size, sort);
    PageResponse<UserView> pagedUsers = usersFacade.getAllUsersPaged(paginationRequest);
    PageResponse<UserResponse> response =
        new PageResponse<>(
            pagedUsers.data().stream().map(usersApiMapper::toResponse).toList(),
            pagedUsers.pagination());
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Get user by ID")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "User found"),
    @ApiResponse(
      responseCode = "401",
      description = "Unauthorized — missing or invalid bearer token",
      content = @Content(schema = @Schema(hidden = true))),
    @ApiResponse(responseCode = "404", description = "User not found",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = ApiErrorResponse.class)))
  })
  @GetMapping("/{id}")
  public ResponseEntity<UserResponse> getUserById(
      @Parameter(description = "User UUID", example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID id) {
    return ResponseEntity.ok(usersApiMapper.toResponse(usersFacade.getUserById(id)));
  }

  @Operation(summary = "Update user")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "User updated"),
    @ApiResponse(
      responseCode = "401",
      description = "Unauthorized — missing or invalid bearer token",
      content = @Content(schema = @Schema(hidden = true))),
    @ApiResponse(responseCode = "404", description = "User not found",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = ApiErrorResponse.class))),
    @ApiResponse(responseCode = "409", description = "Username already taken",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = ApiErrorResponse.class)))
  })
  @PutMapping("/{id}")
  public ResponseEntity<UserResponse> updateUser(
      @Parameter(description = "User UUID", example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID id,
      @Valid @RequestBody UpdateUserRequest request,
      Authentication authentication) {
    UserView userView = usersFacade.updateUser(id, usersApiMapper.toCommand(request), actor(authentication));
    return ResponseEntity.ok(usersApiMapper.toResponse(userView));
  }

  @Operation(summary = "Delete user")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "User deleted (soft delete)"),
    @ApiResponse(
      responseCode = "401",
      description = "Unauthorized — missing or invalid bearer token",
      content = @Content(schema = @Schema(hidden = true))),
    @ApiResponse(responseCode = "404", description = "User not found",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = ApiErrorResponse.class)))
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteUser(
      @Parameter(description = "User UUID", example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID id,
      Authentication authentication) {
    usersFacade.deleteUser(id, actor(authentication));
    return ResponseEntity.noContent().build();
  }

  @Operation(
      summary = "Restore a soft-deleted user",
      description = "Re-activates a previously deleted user account. Returns the restored user.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "User restored successfully",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = UserResponse.class))),
    @ApiResponse(
      responseCode = "401",
      description = "Unauthorized — missing or invalid bearer token",
      content = @Content(schema = @Schema(hidden = true))),
    @ApiResponse(responseCode = "404", description = "User not found or not deleted",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = ApiErrorResponse.class))),
    @ApiResponse(responseCode = "409", description = "User is already active",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = ApiErrorResponse.class)))
  })
  @PostMapping("/{id}/restore")
  public ResponseEntity<UserResponse> restoreUser(
      @Parameter(description = "User UUID", example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID id,
      Authentication authentication) {
    UserView userView = usersFacade.restoreUser(id, actor(authentication));
    return ResponseEntity.ok(usersApiMapper.toResponse(userView));
  }

  @Operation(
      summary = "Permanently delete a user (hard delete)",
      description = "⚠️ IRREVERSIBLE OPERATION. Permanently removes all user data. Admin-only. Use only after compliance review.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "User permanently deleted"),
    @ApiResponse(
      responseCode = "401",
      description = "Unauthorized — missing or invalid bearer token",
      content = @Content(schema = @Schema(hidden = true))),
    @ApiResponse(responseCode = "403", description = "Admin-only operation",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = ApiErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "User not found",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = ApiErrorResponse.class)))
  })
  @DeleteMapping("/{id}/permanent")
  public ResponseEntity<Void> permanentlyDeleteUser(
      @Parameter(description = "User UUID", example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID id,
      Authentication authentication) {
    usersFacade.permanentlyDeleteUser(id, actor(authentication));
    return ResponseEntity.noContent().build();
  }

  private static String actor(Authentication authentication) {
    return authentication.getName();
  }
}

