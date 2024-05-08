package me.whizvox.gameshelf.util.strfmt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StringFormatter {

  private final Token[] tokens;

  private StringFormatter(Token[] tokens) {
    this.tokens = tokens;
  }

  public String render(Map<String, Object> arguments) {
    StringBuilder sb = new StringBuilder();
    for (Token token : tokens) {
      sb.append(token.render(arguments));
    }
    return sb.toString();
  }

  public static StringFormatter of(String format) {
    int last = 0;
    boolean escape = false;
    List<Token> tokens = new ArrayList<>();
    char c;
    for (int i = 0; i < format.length(); i++) {
      c = format.charAt(i);
      if (escape) {
        escape = false;
      } else {
        if (c == '{') {
          tokens.add(new LiteralToken(format.substring(last, i)));
          last = i;
        } else if (c == '}') {
          tokens.add(new ArgumentToken(format.substring(last + 1, i)));
          last = i + 1;
        } else if (c == '\\') {
          escape = true;
        }
      }
    }
    if (last == format.length() - 1) {
      tokens.add(new LiteralToken(format.substring(last)));
    }
    return new StringFormatter(tokens.toArray(Token[]::new));
  }

}
