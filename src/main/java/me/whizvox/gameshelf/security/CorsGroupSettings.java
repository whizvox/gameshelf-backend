package me.whizvox.gameshelf.security;

import java.util.List;
import java.util.Objects;

public class CorsGroupSettings {

  public boolean permitDefault = true;
  public String pattern = "/**";
  public List<String> allowedOrigins = List.of("*");
  public List<String> allowedMethods = List.of("GET", "POST", "HEAD");
  public List<String> allowedHeaders = List.of("*");
  public List<String> exposedHeaders = List.of();
  public boolean allowCredentials = false;
  public long maxAge = 1800L;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof CorsGroupSettings settings)) return false;
    return permitDefault == settings.permitDefault && allowCredentials == settings.allowCredentials &&
        maxAge == settings.maxAge && Objects.equals(pattern, settings.pattern) &&
        Objects.equals(allowedOrigins, settings.allowedOrigins) &&
        Objects.equals(allowedMethods, settings.allowedMethods) &&
        Objects.equals(allowedHeaders, settings.allowedHeaders) &&
        Objects.equals(exposedHeaders, settings.exposedHeaders);
  }

  @Override
  public int hashCode() {
    return Objects.hash(permitDefault, pattern, allowedOrigins, allowedMethods, allowedHeaders, exposedHeaders,
        allowCredentials, maxAge);
  }

  @Override
  public String toString() {
    return "CorsSettings{" +
        "permitDefault=" + permitDefault +
        ", pattern='" + pattern + '\'' +
        ", allowedOrigins=" + allowedOrigins +
        ", allowedMethods=" + allowedMethods +
        ", allowedHeaders=" + allowedHeaders +
        ", exposedHeaders=" + exposedHeaders +
        ", allowCredentials=" + allowCredentials +
        ", maxAge=" + maxAge +
        '}';
  }

}
