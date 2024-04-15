package me.whizvox.gameshelf.security;

import java.util.List;
import java.util.Objects;

public class CorsSettings {

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
    if (!(o instanceof CorsSettings that)) return false;
    return allowCredentials == that.allowCredentials && maxAge == that.maxAge &&
        Objects.equals(allowedOrigins, that.allowedOrigins) && Objects.equals(allowedMethods, that.allowedMethods) &&
        Objects.equals(allowedHeaders, that.allowedHeaders) && Objects.equals(exposedHeaders, that.exposedHeaders);
  }

  @Override
  public int hashCode() {
    return Objects.hash(allowedOrigins, allowedMethods, allowedHeaders, exposedHeaders, allowCredentials, maxAge);
  }

  @Override
  public String toString() {
    return "CorsSettings{" +
        "allowedOrigins=" + allowedOrigins +
        ", allowedMethods=" + allowedMethods +
        ", allowedHeaders=" + allowedHeaders +
        ", exposedHeaders=" + exposedHeaders +
        ", allowCredentials=" + allowCredentials +
        ", maxAge=" + maxAge +
        '}';
  }

}
