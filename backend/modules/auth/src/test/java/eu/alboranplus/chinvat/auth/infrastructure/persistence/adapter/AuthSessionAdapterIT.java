package eu.alboranplus.chinvat.auth.infrastructure.persistence.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import eu.alboranplus.chinvat.auth.infrastructure.persistence.repository.AuthSessionJpaRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import eu.alboranplus.chinvat.auth.AuthTestApplication;
import eu.alboranplus.chinvat.auth.domain.model.AuthSessionTokenKind;

@SpringBootTest(classes = AuthTestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class AuthSessionAdapterIT {

  @Autowired private AuthSessionJpaRepository repository;

  private AuthSessionAdapter sut;

  private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID USER_ID_2 = UUID.fromString("00000000-0000-0000-0000-000000000002");
  private static final Instant NOW = Instant.parse("2026-01-01T12:00:00Z");
  private static final Instant FUTURE = NOW.plusSeconds(3600);
  private static final Instant PAST = NOW.minusSeconds(3600);

  @BeforeEach
  void setUp() {
    sut = new AuthSessionAdapter(repository);
  }

  @Test
  void save_persistsSession_tokenHashedWithSha256() {
    sut.save(
        USER_ID,
        AuthSessionTokenKind.ACCESS,
        "my-raw-token",
        NOW,
        FUTURE,
        "127.0.0.1",
        "TestAgent");

    assertThat(repository.findAll()).hasSize(1);
    // Raw token must NOT be stored
    assertThat(repository.findAll().get(0).getSessionTokenHash()).doesNotContain("my-raw-token");
    // SHA-256 hex is 64 chars
    assertThat(repository.findAll().get(0).getSessionTokenHash()).hasSize(64);
  }

  @Test
  void findActiveUserId_validNonExpiredToken_returnsUserId() {
    sut.save(
        USER_ID,
        AuthSessionTokenKind.ACCESS,
        "valid-token",
        NOW,
        FUTURE,
        "127.0.0.1",
        "Agent");

    Optional<UUID> result = sut.findActiveUserId("valid-token", NOW);

    assertThat(result).contains(USER_ID);
  }

  @Test
  void findActiveUserId_expiredToken_returnsEmpty() {
    sut.save(
        USER_ID,
        AuthSessionTokenKind.ACCESS,
        "expired-token",
        NOW,
        PAST,
        "127.0.0.1",
        "Agent");

    Optional<UUID> result = sut.findActiveUserId("expired-token", NOW);

    assertThat(result).isEmpty();
  }

  @Test
  void findActiveUserId_unknownToken_returnsEmpty() {
    assertThat(sut.findActiveUserId("nonexistent-token", NOW)).isEmpty();
  }

  @Test
  void revokeByRawToken_setsRevokedAt() {
    sut.save(
        USER_ID,
        AuthSessionTokenKind.ACCESS,
        "to-revoke",
        NOW,
        FUTURE,
        "127.0.0.1",
        "Agent");

    sut.revokeByRawToken("to-revoke", NOW);

    Optional<UUID> result = sut.findActiveUserId("to-revoke", NOW.plusSeconds(1));
    assertThat(result).isEmpty();
  }

  @Test
  void findActiveUserId_revokedToken_returnsEmpty() {
    sut.save(
        USER_ID,
        AuthSessionTokenKind.ACCESS,
        "revoked-access",
        NOW,
        FUTURE,
        "127.0.0.1",
        "Agent");
    sut.revokeByRawToken("revoked-access", NOW);

    assertThat(sut.findActiveUserId("revoked-access", NOW)).isEmpty();
  }

  @Test
  void revokeAllByUserId_revokesAllSessions() {
    sut.save(USER_ID, AuthSessionTokenKind.ACCESS, "token-a", NOW, FUTURE, "127.0.0.1", "Agent");
    sut.save(USER_ID, AuthSessionTokenKind.REFRESH, "token-b", NOW, FUTURE, "127.0.0.1", "Agent");
    sut.save(USER_ID_2, AuthSessionTokenKind.ACCESS, "other-user-token", NOW, FUTURE, "127.0.0.1", "Agent");

    sut.revokeAllByUserId(USER_ID, NOW);

    assertThat(sut.findActiveUserId("token-a", NOW)).isEmpty();
    assertThat(sut.findActiveUserId("token-b", NOW)).isEmpty();
    // Other user's session is unaffected
    assertThat(sut.findActiveUserId("other-user-token", NOW)).contains(USER_ID_2);
  }

  @Test
  void differentTokensSameRawValue_wouldCollide_uniqueHashEnforced() {
    // Same raw token saved twice should fail due to unique hash constraint
    sut.save(
        USER_ID,
        AuthSessionTokenKind.ACCESS,
        "unique-token",
        NOW,
        FUTURE,
        "127.0.0.1",
        "Agent");

    org.assertj.core.api.Assertions.assertThatThrownBy(
            () -> {
              sut.save(
                  USER_ID,
                  AuthSessionTokenKind.ACCESS,
                  "unique-token",
                  NOW,
                  FUTURE,
                  "10.0.0.1",
                  "AnotherAgent");
              repository.flush();
            })
        .isInstanceOf(Exception.class); // DataIntegrityViolationException or similar
  }
}
