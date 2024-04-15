package me.whizvox.gameshelf.security;

import java.util.Collections;
import java.util.List;

public record CorsGroups(List<CorsSettings> groups) {

  public CorsGroups(List<CorsSettings> groups) {
    this.groups = Collections.unmodifiableList(groups);
  }

}
