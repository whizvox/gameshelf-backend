package me.whizvox.gameshelf;

import me.whizvox.gameshelf.util.StringUtils;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class StringUtilsTests {

  @Test
  void createSecureRandomSequence() {
    Set<String> sequences = new HashSet<>();
    for (int i = 0; i < 10; i++) {
      String seq = StringUtils.createSecureRandomSequence(12);
      assertThat(seq).hasSize(12);
      assertThat(seq).containsPattern("[A-Z0-9]+");
      assertThat(sequences).doesNotContain(seq);
      sequences.add(seq);
    }
  }

  @Test
  void replaceAll() {

  }

}
