package eu.alboranplus.chinvat.users.api.controller;

import eu.alboranplus.chinvat.users.api.dto.CreateUserRequest;
import eu.alboranplus.chinvat.users.api.dto.UserResponse;
import eu.alboranplus.chinvat.users.api.mapper.UsersApiMapper;
import eu.alboranplus.chinvat.users.application.dto.UserView;
import eu.alboranplus.chinvat.users.application.facade.UsersFacade;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UsersController {

  private final UsersFacade usersFacade;
  private final UsersApiMapper usersApiMapper;

  public UsersController(UsersFacade usersFacade, UsersApiMapper usersApiMapper) {
    this.usersFacade = usersFacade;
    this.usersApiMapper = usersApiMapper;
  }

  @PostMapping
  public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
    UserView userView = usersFacade.createUser(usersApiMapper.toCommand(request));
    return ResponseEntity.status(HttpStatus.CREATED).body(usersApiMapper.toResponse(userView));
  }
}
