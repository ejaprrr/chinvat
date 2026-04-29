package eu.alboranplus.chinvat.auth.application.usecase;

import eu.alboranplus.chinvat.auth.application.command.RequestPasswordResetCommand;
import eu.alboranplus.chinvat.auth.application.dto.AuthUserProjection;
import eu.alboranplus.chinvat.auth.application.dto.PasswordResetRequestResult;
import eu.alboranplus.chinvat.auth.application.port.out.AuthClockPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthPasswordResetTokenPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthRecoveryTokenGeneratorPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthUsersPort;
import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RequestPasswordResetUseCase {

  private final AuthUsersPort authUsersPort;
  private final AuthRecoveryTokenGeneratorPort tokenGeneratorPort;
  private final AuthPasswordResetTokenPort passwordResetTokenPort;
  private final AuthClockPort authClockPort;

  private final Duration resetTokenTtl;

  public RequestPasswordResetUseCase(
      AuthUsersPort authUsersPort,
      AuthRecoveryTokenGeneratorPort tokenGeneratorPort,
      AuthPasswordResetTokenPort passwordResetTokenPort,
      AuthClockPort authClockPort,
      @Value("${auth.password-reset.ttl:PT30M}") Duration resetTokenTtl) {
    this.authUsersPort = authUsersPort;
    this.tokenGeneratorPort = tokenGeneratorPort;
    this.passwordResetTokenPort = passwordResetTokenPort;
    this.authClockPort = authClockPort;
    this.resetTokenTtl = resetTokenTtl;
  }

  @Transactional
  public PasswordResetRequestResult execute(RequestPasswordResetCommand command) {
    Instant now = authClockPort.now();

    var userOpt = authUsersPort.findByEmail(command.email()).filter(AuthUserProjection::active);
    if (userOpt.isEmpty()) {
      return new PasswordResetRequestResult(null, now);
    }

    var user = userOpt.get();
    Instant expiresAt = now.plus(resetTokenTtl);

    String resetCode =
        tokenGeneratorPort.generateToken(user.userId(), user.email(), expiresAt);
    passwordResetTokenPort.save(
      user.userId(), resetCode, now, expiresAt, command.clientIp(), command.userAgent());

    String codeForResponse = command.revealCode() ? resetCode : null;
    return new PasswordResetRequestResult(codeForResponse, now);
  }
}

