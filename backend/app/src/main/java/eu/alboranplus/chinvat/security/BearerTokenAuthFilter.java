package eu.alboranplus.chinvat.security;

import eu.alboranplus.chinvat.auth.application.dto.TokenPrincipal;
import eu.alboranplus.chinvat.auth.application.facade.AuthFacade;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class BearerTokenAuthFilter extends OncePerRequestFilter {

  private static final String BEARER_PREFIX = "Bearer ";

  private final AuthFacade authFacade;

  public BearerTokenAuthFilter(AuthFacade authFacade) {
    this.authFacade = authFacade;
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    String header = request.getHeader("Authorization");

    if (header != null && header.startsWith(BEARER_PREFIX)) {
      String rawToken = header.substring(BEARER_PREFIX.length());
      authFacade
          .validateAccessToken(rawToken)
          .ifPresent(principal -> setAuthentication(principal));
    }

    filterChain.doFilter(request, response);
  }

  private void setAuthentication(TokenPrincipal principal) {
    List<SimpleGrantedAuthority> authorities =
        principal.permissions().stream().map(SimpleGrantedAuthority::new).toList();

    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(principal.email(), null, authorities);

    SecurityContextHolder.getContext().setAuthentication(authentication);

    // Keep the full token principal for downstream endpoints (e.g. /auth/me, /auth/sessions).
    authentication.setDetails(principal);
  }
}
