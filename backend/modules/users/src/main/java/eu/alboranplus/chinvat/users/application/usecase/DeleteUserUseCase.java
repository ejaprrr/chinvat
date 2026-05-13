package eu.alboranplus.chinvat.users.application.usecase;

import eu.alboranplus.chinvat.users.application.port.out.UsersClockPort;
import eu.alboranplus.chinvat.users.application.port.out.UsersRepositoryPort;
import eu.alboranplus.chinvat.users.domain.exception.UserNotFoundException;
import eu.alboranplus.chinvat.users.domain.model.UserAccount;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Enterprise-grade soft delete use case for user accounts.
 * 
 * GDPR/Compliance: Marks user as deleted without removing data, enabling:
 * - Audit trail retention for legal requirements
 * - Account recovery within compliance grace period
 * - Full historical data preservation
 * - Compliance with GDPR "right to be forgotten" implementation
 */
@Service
public class DeleteUserUseCase {

  private final UsersRepositoryPort usersRepositoryPort;
  private final UsersClockPort usersClockPort;

  public DeleteUserUseCase(
      UsersRepositoryPort usersRepositoryPort,
      UsersClockPort usersClockPort) {
    this.usersRepositoryPort = usersRepositoryPort;
    this.usersClockPort = usersClockPort;
  }

  @Transactional
  public void execute(UUID id) {
    UserAccount userAccount = usersRepositoryPort.findById(id)
        .orElseThrow(() -> new UserNotFoundException("User not found: " + id));
    
    // Soft delete: mark with deletion timestamp
    Instant now = usersClockPort.now();
    UserAccount deletedUser = userAccount.withDeleted(now);
    
    usersRepositoryPort.save(deletedUser);
  }
}

