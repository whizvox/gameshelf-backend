package me.whizvox.gameshelf.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.whizvox.gameshelf.exception.ServiceException;
import me.whizvox.gameshelf.user.User;
import me.whizvox.gameshelf.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuthorizeJWTFilter extends OncePerRequestFilter {

  private final ObjectMapper objectMapper;
  private final UserService userService;

  @Autowired
  public AuthorizeJWTFilter(ObjectMapper objectMapper, UserService userService) {
    this.objectMapper = objectMapper;
    this.userService = userService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    String token = request.getHeader("Authorization");
    boolean malformedAuthorization = false;
    if (token != null) {
      if (token.startsWith("Bearer ")) {
        token = token.substring(7);
        try {
            User user = userService.getUserFromAccessToken(token).orElse(null);
            if (user != null) {
              UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
              auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
              SecurityContextHolder.getContext().setAuthentication(auth);
            } else {
              malformedAuthorization = true;
            }
        } catch (Exception e) {
          malformedAuthorization = true;
        }
      } else {
        malformedAuthorization = true;
      }
    }
    if (malformedAuthorization) {
      throw ServiceException.forbidden();
    } else {
      filterChain.doFilter(request, response);
    }
  }

}
