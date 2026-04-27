package eu.alboranplus.chinvat.auth.infrastructure.crypto;

import static org.assertj.core.api.Assertions.assertThat;

import eu.alboranplus.chinvat.auth.application.dto.IssuedTokenPair;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = AuthTokenIssuerAdapter.class)
@TestPropertySource(properties = {"auth.tokens.access-ttl=PT15M", "auth.tokens.refresh-ttl=P14D"})
class AuthTokenIssuerAdapterTest {

  @Autowired private AuthTokenIssuerAdapter sut;

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Test
  void issue_returnsNonNullTokenPair() {
    IssuedTokenPair pair = sut.issue(1L, "alice@example.com", NOW);

    assertThat(pair.accessToken()).isNotBlank();
    assertThat(pair.refreshToken()).isNotBlank();
  }

  @Test
  void issue_accessTokenExpiresIn15Minutes() {
    IssuedTokenPair pair = sut.issue(1L, "alice@example.com", NOW);

    assertThat(pair.expiresAt()).isEqualTo(NOW.plusSeconds(900));
  }

  @Test
  void issue_refreshTokenExpiresIn14Days() {
    IssuedTokenPair pair = sut.issue(1L, "alice@example.com", NOW);

    assertThat(pair.refreshExpiresAt()).isEqualTo(NOW.plusSeconds(1_209_600));
  }

  @Test
  void issue_accessAndRefreshTokensAreDistinct() {
    IssuedTokenPair pair = sut.issue(1L, "alice@example.com", NOW);

    assertThat(pair.accessToken()).isNotEqualTo(pair.refreshToken());
  }

  @Test
  void issue_multipleInvocations_produceUniqueTokens() {
    Set<String> tokens = new HashSet<>();
    for (int i = 0; i < 20; i++) {
      IssuedTokenPair pair = sut.issue(1L, "alice@example.com", NOW);
      tokens.add(pair.accessToken());
      tokens.add(pair.refreshToken());
    }
    assertThat(tokens).hasSize(40);
  }

  @Test
  void issue_accessTokenDecodedPayload_containsAccessKind() {
    IssuedTokenPair pair = sut.issue(42L, "bob@example.com", NOW);
    String decoded = new String(Base64.getUrlDecoder().decode(pair.accessToken()), StandardCharsets.UTF_8);

    assertThat(decoded).startsWith("A:");
    assertThat(decoded).contains("42");
    assertThat(decoded).contains("bob@example.com");
  }

  @Test
  void issue_refreshTokenDecodedPayload_containsRefreshKind() {
    IssuedTokenPair pair = sut.issue(42L, "bob@example.com", NOW);
    String decoded = new String(Base64.getUrlDecoder().decode(pair.refreshToken()), StandardCharsets.UTF_8);

    assertThat(decoded).startsWith("R:");
  }
}
