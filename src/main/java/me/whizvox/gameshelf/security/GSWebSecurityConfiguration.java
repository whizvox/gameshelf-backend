package me.whizvox.gameshelf.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import me.whizvox.gameshelf.user.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@EnableWebSecurity
@Configuration
public class GSWebSecurityConfiguration {

  private static final Logger LOG = LoggerFactory.getLogger(GSWebSecurityConfiguration.class);

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
                                                 JWTAuthenticationFilter authFilter) throws Exception {
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
    http.addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class);
    http.cors(Customizer.withDefaults());
    http.csrf(csrf -> csrf.disable());
    http.httpBasic(Customizer.withDefaults());
    return http.build();
  }

  @Bean @Primary
  public CorsConfigurationSource corsConfigurationSource(CorsGroups groups) {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    groups.groups().forEach(settings -> {
      CorsConfiguration config = new CorsConfiguration();
      if (settings.permitDefault) {
        config.applyPermitDefaultValues();
      }
      config.setAllowedOriginPatterns(settings.allowedOrigins);
      config.setAllowedMethods(settings.allowedMethods);
      config.setAllowedHeaders(settings.allowedHeaders);
      config.setExposedHeaders(settings.exposedHeaders);
      config.setAllowCredentials(settings.allowCredentials);
      source.registerCorsConfiguration(settings.pattern, config);
    });
    return source;
  }

  @Bean
  public CorsGroups corsGroups(@Value("${gameshelf.cors.settingsFile:cors.json}") String settingsFile,
                               @Value("${gameshelf.cors.generateSettingsFile:true}") boolean generateFile,
                               ObjectMapper objectMapper) {
    Path path = Paths.get(settingsFile);
    List<CorsGroupSettings> groups;
    if (Files.exists(path)) {
      LOG.debug("Loading CORS settings file from {}", settingsFile);
      try (InputStream in = Files.newInputStream(path)) {
        groups = objectMapper.readValue(in, new TypeReference<>() {});
      } catch (IOException e) {
        throw new RuntimeException("Could not read CORS settings file: " + settingsFile, e);
      }
    } else {
      if (generateFile) {
        LOG.info("Generating CORS settings file at {}", settingsFile);
        groups = List.of(new CorsGroupSettings());
        ObjectWriter prettyPrinter = objectMapper.writerWithDefaultPrettyPrinter();
        try (OutputStream out = Files.newOutputStream(path)) {
          prettyPrinter.writeValue(out, groups);
        } catch (IOException e) {
          LOG.warn("Could not save CORS settings file: " + settingsFile, e);
        }
      } else {
        throw new IllegalArgumentException("CORS settings file generation turned off and no CORS settings file found");
      }
    }
    return new CorsGroups(groups);
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

}
