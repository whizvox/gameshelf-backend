package me.whizvox.gameshelf.security;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public record CorsGroups(List<CorsGroupSettings> groups) {

  public CorsGroups(List<CorsGroupSettings> groups) {
    this.groups = Collections.unmodifiableList(groups);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof CorsGroups that)) return false;
    return Objects.equals(groups, that.groups);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(groups);
  }

  @Override
  public String toString() {
    return "CorsGroups{" +
        "groups=" + groups +
        '}';
  }

}
