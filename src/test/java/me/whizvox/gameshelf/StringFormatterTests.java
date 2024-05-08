package me.whizvox.gameshelf;

import me.whizvox.gameshelf.util.strfmt.ArgumentToken;
import me.whizvox.gameshelf.util.strfmt.LiteralToken;
import me.whizvox.gameshelf.util.strfmt.StringFormatter;
import me.whizvox.gameshelf.util.strfmt.Token;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class StringFormatterTests {

  private static final Map<String, Object>
      TEST_ARGS1 = Map.of("arg1", "Hello", "arg2", "World", "arg3", 57),
      TEST_ARGS2 = Map.of("arg1", "Goodbye", "arg2", "Everyone", "arg3", "NaN");

  @Test
  void literalToken_render() {
    Token token = new LiteralToken("Hi!");
    assertThat(token.render(TEST_ARGS1)).isEqualTo("Hi!");
    token = new LiteralToken("Nice to meet you");
    assertThat(token.render(TEST_ARGS1)).isEqualTo("Nice to meet you");
  }

  @Test
  void argumentToken_render() {
    Token token = new ArgumentToken("arg1");
    assertThat(token.render(TEST_ARGS1)).isEqualTo("Hello");
    token = new ArgumentToken("arg3");
    assertThat(token.render(TEST_ARGS1)).isEqualTo("57");
  }

  @Test
  void argumentToken_render_missingArgument() {
    Token token = new ArgumentToken("arg4");
    assertThat(token.render(TEST_ARGS1)).isEmpty();
    token = new ArgumentToken("ajsdjnk");
    assertThat(token.render(TEST_ARGS1)).isEmpty();
  }

  @Test
  void stringFormatter_basic() {
    StringFormatter sf = StringFormatter.of("{arg1} {arg2}. Number={arg3}");
    assertThat(sf.render(TEST_ARGS1)).isEqualTo("Hello World. Number=57");
    assertThat(sf.render(TEST_ARGS2)).isEqualTo("Goodbye Everyone. Number=NaN");

    sf = StringFormatter.of("arg1={arg1}, arg2={arg2}, arg3={arg3}, something else");
    assertThat(sf.render(TEST_ARGS1)).isEqualTo("arg1=Hello, arg2=World, arg3=57");
    assertThat(sf.render(TEST_ARGS2)).isEqualTo("arg1=Goodbye, arg2=Everyone, arg3=NaN");
  }

}
