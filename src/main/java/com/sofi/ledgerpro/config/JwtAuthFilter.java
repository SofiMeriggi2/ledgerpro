package com.sofi.ledgerpro.config;

import com.sofi.ledgerpro.repo.UserRepository;
import com.sofi.ledgerpro.model.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

  private final JwtService jwt;
  private final UserRepository users;

  public JwtAuthFilter(JwtService jwt, UserRepository users) {
    this.jwt = jwt;
    this.users = users;
  }

  /** Helper: obtiene el UUID del principal guardado en el token */
  public static UUID userId(Authentication auth) {
    return UUID.fromString((String) auth.getPrincipal());
  }

  @Override
  protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {

    String header = req.getHeader("Authorization");

    if (header != null
        && header.startsWith("Bearer ")
        && SecurityContextHolder.getContext().getAuthentication() == null) {

      String token = header.substring(7);
      try {
        Jws<Claims> jws = jwt.verify(token);
        Claims claims = jws.getBody();

        // sub puede ser UUID o email; también probamos claim "uid"
        String raw = Optional.ofNullable(claims.get("uid", String.class))
                             .orElseGet(claims::getSubject);

        String principalUuid;
        try {
          // ¿ya es un UUID?
          UUID.fromString(raw);
          principalUuid = raw;
        } catch (IllegalArgumentException ex) {
          // Parece email → buscamos el usuario
          principalUuid = users.findByEmail(raw)
              .map(User::getId)
              .map(UUID::toString)
              .orElseThrow(() -> new JwtException("Token inválido"));
        }

        var principal = new UsernamePasswordAuthenticationToken(
            principalUuid, // 🔒 SIEMPRE guardo el UUID como principal
            null,
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        principal.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
        SecurityContextHolder.getContext().setAuthentication(principal);

      } catch (JwtException | IllegalArgumentException ignored) {
        // token inválido/expirado → seguimos sin autenticar
      }
    }

    chain.doFilter(req, res);
  }
}
