package eu.alboranplus.chinvat.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import eu.alboranplus.chinvat.auth.application.dto.TokenPrincipal;
import eu.alboranplus.chinvat.auth.application.facade.AuthFacade;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class BearerTokenAuthFilterTest {

  @Mock private AuthFacade authFacade;

  @InjectMocks private BearerTokenAuthFilter sut;

  @AfterEach
  void clearContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void doFilter_validBearerToken_setsAuthenticationInContext() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);

    given(request.getHeader("Authorization")).willReturn("Bearer valid-token");
    given(authFacade.validateAccessToken("valid-token"))
        .willReturn(
            Optional.of(
                new TokenPrincipal(
                    1L, "alice@example.com", Set.of("USER"), Set.of("PROFILE:READ"))));

    sut.doFilterInternal(request, response, chain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    assertThat(SecurityContextHolder.getContext().getAuthentication().isAuthenticated()).isTrue();
    assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
        .isEqualTo("alice@example.com");
    verify(chain).doFilter(request, response);
  }

  @Test
  void doFilter_invalidToken_doesNotSetAuthentication() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);

    given(request.getHeader("Authorization")).willReturn("Bearer invalid-token");
    given(authFacade.validateAccessToken("invalid-token")).willReturn(Optional.empty());

    sut.doFilterInternal(request, response, chain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(chain).doFilter(request, response);
  }

  @Test
  void doFilter_missingAuthorizationHeader_passesThrough() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);

    given(request.getHeader("Authorization")).willReturn(null);

    sut.doFilterInternal(request, response, chain);

    verify(authFacade, never()).validateAccessToken(org.mockito.ArgumentMatchers.any());
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(chain).doFilter(request, response);
  }

  @Test
  void doFilter_nonBearerAuthHeader_passesThrough() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);

    given(request.getHeader("Authorization")).willReturn("Basic dXNlcjpwYXNz");

    sut.doFilterInternal(request, response, chain);

    verify(authFacade, never()).validateAccessToken(org.mockito.ArgumentMatchers.any());
    verify(chain).doFilter(request, response);
  }
}
