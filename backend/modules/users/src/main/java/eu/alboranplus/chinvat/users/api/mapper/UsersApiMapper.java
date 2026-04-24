package eu.alboranplus.chinvat.users.api.mapper;

import eu.alboranplus.chinvat.users.api.dto.CreateUserRequest;
import eu.alboranplus.chinvat.users.api.dto.UserResponse;
import eu.alboranplus.chinvat.users.application.command.CreateUserCommand;
import eu.alboranplus.chinvat.users.application.dto.UserView;
import org.springframework.stereotype.Component;

@Component
public class UsersApiMapper {

  public CreateUserCommand toCommand(CreateUserRequest request) {
    return new CreateUserCommand(
        request.email(), request.password(), request.displayName(), request.roles());
  }

  public UserResponse toResponse(UserView userView) {
    return new UserResponse(
        userView.id(),
        userView.email(),
        userView.displayName(),
        userView.roles(),
        userView.active());
  }
}
