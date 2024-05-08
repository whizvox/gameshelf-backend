package me.whizvox.gameshelf.util.strfmt;

import java.util.Map;

public record ArgumentToken(String name) implements Token {

  @Override
  public String render(Map<String, Object> args) {
    return String.valueOf(args.getOrDefault(name, ""));
  }

}
