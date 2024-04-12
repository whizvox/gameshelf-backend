package me.whizvox.gameshelf.security;

import java.time.LocalDateTime;
import java.util.Objects;

public class AccessToken {

  public String token;
  public LocalDateTime issued;
  public LocalDateTime expires;

  public AccessToken(String token, LocalDateTime issued, LocalDateTime expires) {
    this.token = token;
    this.issued = issued;
    this.expires = expires;
  }

  public AccessToken() {
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof AccessToken that)) return false;
    return Objects.equals(token, that.token) && Objects.equals(issued, that.issued) && Objects.equals(expires, that.expires);
  }

  @Override
  public int hashCode() {
    return Objects.hash(token, issued, expires);
  }

  @Override
  public String toString() {
    return "AccessToken{" +
        "token='" + token + '\'' +
        ", issued=" + issued +
        ", expires=" + expires +
        '}';
  }

}
