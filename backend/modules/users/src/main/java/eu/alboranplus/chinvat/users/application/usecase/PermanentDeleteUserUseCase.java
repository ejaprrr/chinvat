package eu.alboranplus.chinvat.users.application.usecase;

import eu.alboranplus.chinvat.users.application.port.out.UsersRepositoryPort;
import eu.alboranplus.chinvat.users.domain.exception.UserNotFoundException;
import eu.alboranplus.chinvat.users.domain.model.UserAccount;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Permanently delete a user account (hard delete).
 * 
 * CAUTION: This operation is IRREVERSIBLE.
 * Enterprise-grade: Should only be called by administrators after compliance review.
 * 
 * Use cases:
 * - Cleanup after compliance grace period expires
 * - Complete data removal after GDPR "right to be forgotten" request
 * - Test data cleanup
 */
@Service
public class PermanentDeleteUserUseCase {

  private final UsersRepositoryPort usersRepositoryPort;

  public PermanentDeleteUserUseCase(UsersRepositoryPort usersRepositoryPort) {
    this.usersRepositoryPort = usersRepositoryPort;
  }

  @Transactional
  public void execute(UUID id) {
    UserAccount userAccount = usersRepositoryPort.findById(id)
        .orElseThrow(() -> new UserNotFoundException("User not found: " + id));
    
    // Hard delete: permanently remove all data
    usersRepositoryPort.deleteById(id);
  }
}
