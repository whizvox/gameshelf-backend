package me.whizvox.gameshelf.util.strfmt;

import java.util.Map;

public record LiteralToken(String literal) implements Token {

  @Override
  public String render(Map<String, Object> args) {
    return literal;
  }

}
