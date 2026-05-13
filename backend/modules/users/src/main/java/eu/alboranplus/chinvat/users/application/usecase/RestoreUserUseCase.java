package eu.alboranplus.chinvat.users.application.usecase;

import eu.alboranplus.chinvat.users.application.port.out.UsersRepositoryPort;
import eu.alboranplus.chinvat.users.domain.exception.UserNotFoundException;
import eu.alboranplus.chinvat.users.domain.model.UserAccount;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Restore a soft-deleted user account.
 * 
 * Enterprise-grade: Re-activates user within compliance grace period.
 * Audit trail preserved showing deletion and restoration events.
 */
@Service
public class RestoreUserUseCase {

  private final UsersRepositoryPort usersRepositoryPort;

  public RestoreUserUseCase(UsersRepositoryPort usersRepositoryPort) {
    this.usersRepositoryPort = usersRepositoryPort;
  }

  @Transactional
  public UserAccount execute(UUID id) {
    UserAccount userAccount = usersRepositoryPort.findById(id)
        .orElseThrow(() -> new UserNotFoundException("User not found: " + id));
    
    if (userAccount.isActive()) {
      throw new IllegalArgumentException("User is already active (not deleted)");
    }
    
    // Restore: clear deletedAt timestamp
    UserAccount restoredUser = userAccount.withRestored();
    return usersRepositoryPort.save(restoredUser);
  }
}
