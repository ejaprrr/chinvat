package eu.alboranplus.chinvat.users.api.mapper;

import eu.alboranplus.chinvat.users.api.dto.CreateUserRequest;
import eu.alboranplus.chinvat.users.api.dto.UpdateUserRequest;
import eu.alboranplus.chinvat.users.api.dto.UserResponse;
import eu.alboranplus.chinvat.users.application.command.CreateUserCommand;
import eu.alboranplus.chinvat.users.application.command.UpdateUserCommand;
import eu.alboranplus.chinvat.users.application.dto.UserView;
import eu.alboranplus.chinvat.users.domain.model.AccessLevel;
import org.springframework.stereotype.Component;

@Component
public class UsersApiMapper {

  public CreateUserCommand toCommand(CreateUserRequest request) {
    return new CreateUserCommand(
        request.username(),
        request.fullName(),
        request.phoneNumber(),
        request.email(),
        request.password(),
        request.userType(),
        AccessLevel.NORMAL,
        request.addressLine(),
        request.postalCode(),
        request.city(),
        request.country(),
        request.defaultLanguage());
  }

  public UpdateUserCommand toCommand(UpdateUserRequest request) {
    return new UpdateUserCommand(
        request.username(),
        request.fullName(),
        request.phoneNumber(),
        request.userType(),
        request.accessLevel(),
        request.addressLine(),
        request.postalCode(),
        request.city(),
        request.country(),
        request.defaultLanguage());
  }

  public UserResponse toResponse(UserView userView) {
    return new UserResponse(
        userView.id(),
        userView.username(),
        userView.fullName(),
        userView.phoneNumber(),
        userView.email(),
        userView.userType().name(),
        userView.accessLevel().name(),
        userView.addressLine(),
        userView.postalCode(),
        userView.city(),
        userView.country(),
        userView.defaultLanguage());
  }
}

