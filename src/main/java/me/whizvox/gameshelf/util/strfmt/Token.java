package me.whizvox.gameshelf.util.strfmt;

import java.util.Map;

public interface Token {

  String render(Map<String, Object> args);

}
