package eu.alboranplus.chinvat.users.api.controller;

import eu.alboranplus.chinvat.common.pagination.PaginationRequest;
import eu.alboranplus.chinvat.common.pagination.PageResponse;
import eu.alboranplus.chinvat.users.api.dto.CreateUserRequest;
import eu.alboranplus.chinvat.users.api.dto.UpdateUserRequest;
import eu.alboranplus.chinvat.users.api.dto.UserResponse;
import eu.alboranplus.chinvat.users.api.exception.UsersApiExceptionHandler.UsersErrorResponse;
import eu.alboranplus.chinvat.users.api.mapper.UsersApiMapper;
import eu.alboranplus.chinvat.users.application.dto.UserView;
import eu.alboranplus.chinvat.users.application.facade.UsersFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
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
            schema = @Schema(implementation = UsersErrorResponse.class))),
    @ApiResponse(
        responseCode = "409",
        description = "Email or username already registered",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = UsersErrorResponse.class)))
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
            schema = @Schema(implementation = UsersErrorResponse.class)))
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
            schema = @Schema(implementation = UsersErrorResponse.class)))
  })
  @GetMapping("/{id}")
  public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
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
            schema = @Schema(implementation = UsersErrorResponse.class))),
    @ApiResponse(responseCode = "409", description = "Username already taken",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = UsersErrorResponse.class)))
  })
  @PutMapping("/{id}")
  public ResponseEntity<UserResponse> updateUser(
      @PathVariable Long id,
      @Valid @RequestBody UpdateUserRequest request,
      Authentication authentication) {
    UserView userView = usersFacade.updateUser(id, usersApiMapper.toCommand(request), actor(authentication));
    return ResponseEntity.ok(usersApiMapper.toResponse(userView));
  }

  @Operation(summary = "Delete user")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "User deleted"),
    @ApiResponse(
      responseCode = "401",
      description = "Unauthorized — missing or invalid bearer token",
      content = @Content(schema = @Schema(hidden = true))),
    @ApiResponse(responseCode = "404", description = "User not found",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = UsersErrorResponse.class)))
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteUser(@PathVariable Long id, Authentication authentication) {
    usersFacade.deleteUser(id, actor(authentication));
    return ResponseEntity.noContent().build();
  }

  private static String actor(Authentication authentication) {
    return authentication.getName();
  }
}

