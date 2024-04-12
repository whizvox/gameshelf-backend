package me.whizvox.gameshelf.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.whizvox.gameshelf.user.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@EnableWebSecurity
@Configuration
public class GSWebSecurityConfiguration {

  private final String apiPrefix;

  public GSWebSecurityConfiguration(@Value("${apiPrefix}") String apiPrefix) {
    this.apiPrefix = apiPrefix;
  }

  private RequestMatcher[] matchers(HttpMethod method, String... patterns) {
    RequestMatcher[] matchers = new RequestMatcher[patterns.length];
    for (int i = 0; i < patterns.length; i++) {
      matchers[i] = new AntPathRequestMatcher(apiPrefix + "/" + patterns[i], method.name());
    }
    return matchers;
  }

  private RequestMatcher[] combine(RequestMatcher[]... matchers) {
    int total = 0;
    for (RequestMatcher[] subMatchers : matchers) {
      total += subMatchers.length;
    }
    RequestMatcher[] newMatchers = new RequestMatcher[total];
    total = 0;
    for (RequestMatcher[] subMatchers : matchers) {
      System.arraycopy(subMatchers, 0, newMatchers, total, subMatchers.length);
      total += subMatchers.length;
    }
    return newMatchers;
  }

  private static AuthorizationManager<RequestAuthorizationContext> customAccess(Role minRole, boolean mustBeVerified,
                                                                         boolean mustNotBeBanned) {
    return new UserBasedAuthorizationManager(minRole, mustBeVerified, mustNotBeBanned);
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                 AuthorizeJWTFilter jwtTokenFilter,
                                                 ObjectMapper objectMapper) throws Exception {
    http.authorizeHttpRequests(authorizeRequests ->
        authorizeRequests
            .requestMatchers(combine(
                matchers(HttpMethod.GET, "user"),
                matchers(HttpMethod.POST, "user"),
                matchers(HttpMethod.PUT, "user"),
                matchers(HttpMethod.DELETE, "user")
            )).access(customAccess(Role.ADMIN, true, true))

            .requestMatchers(combine(
                matchers(HttpMethod.POST, "user/ban", "user/unban"),
                matchers(HttpMethod.PUT, "profile"),
                matchers(HttpMethod.DELETE, "game", "genre", "media", "platform", "rating", "ratingsystem")
            )).access(customAccess(Role.MODERATOR, true, true))

            .requestMatchers(combine(
                matchers(HttpMethod.GET, "media/search"),
                matchers(HttpMethod.POST, "game", "genre", "media", "platform", "rating", "ratingsystem"),
                matchers(HttpMethod.PUT, "game", "genre", "media", "platform", "rating", "ratingsystem")
            )).access(customAccess(Role.EDITOR, true, true))

            .requestMatchers(combine(
                matchers(HttpMethod.POST, "gamelist"),
                matchers(HttpMethod.PUT, "gamelist", "profile/self"),
                matchers(HttpMethod.DELETE, "gamelist")
            )).access(customAccess(Role.MEMBER, true, true))

            .requestMatchers(combine(
                matchers(HttpMethod.GET, "user/available", "user/exists"),
                matchers(HttpMethod.POST, "user/request/reset", "user/request/verify"),
                matchers(HttpMethod.PUT, "user/self")
            )).access(customAccess(Role.MEMBER, false, false))

            .requestMatchers(combine(
                matchers(HttpMethod.GET, "user/self", "game", "gamelist", "genre", "media/info/**", "media/**", "platform/**", "profile/**", "rating/**", "ratingsystem/**"),
                matchers(HttpMethod.POST, "accesstoken"),
                matchers(HttpMethod.PUT, "user/reset", "user/verify")
            )).permitAll()

            // deny all other requests
            .anyRequest().denyAll()
            //.anyRequest().permitAll()
    );
    http.addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);
    http.csrf(csrf -> csrf.disable());
    http.httpBasic(Customizer.withDefaults());
    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

}
