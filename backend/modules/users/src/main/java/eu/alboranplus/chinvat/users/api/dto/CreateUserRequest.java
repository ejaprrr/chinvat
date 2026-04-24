package eu.alboranplus.chinvat.users.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record CreateUserRequest(
    @NotBlank @Email String email,
    @NotBlank @Size(min = 12, max = 128) String password,
    @NotBlank @Size(max = 120) String displayName,
    Set<String> roles) {

  public CreateUserRequest {
    roles = roles == null ? Set.of() : Set.copyOf(roles);
  }
}
