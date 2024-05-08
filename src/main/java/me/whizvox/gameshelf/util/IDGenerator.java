package me.whizvox.gameshelf.util;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Random;

@Service
public class IDGenerator {

  private static final char[]
      ID_ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray(), // 62 chars
      CODE_ALPHABET = "0123456789".toCharArray();

  public static final int
      ID_LENGTH = 16,
      CODE_LENGTH = 8;

  private final Random rand;
  private final SecureRandom srand;

  public IDGenerator() {
    rand = new Random();
    srand = new SecureRandom();
  }

  private static byte idCharToByte(char c) {
    if (c <= '9') {
      return (byte) (c - '0');
    }
    if (c <= 'Z') {
      return (byte) (c - 'A');
    }
    return (byte) (c - 'a');
  }

  private static String generate(Random rand, int length, char[] alphabet) {
    char[] c = new char[length];
    for (int i = 0; i < length; i++) {
      c[i] = alphabet[rand.nextInt(alphabet.length)];
    }
    return new String(c);
  }

  public String id() {
    return generate(rand, ID_LENGTH, ID_ALPHABET);
  }

  public String secureId() {
    return generate(srand, ID_LENGTH, ID_ALPHABET);
  }

  public String code() {
    return generate(srand, CODE_LENGTH, CODE_ALPHABET);
  }

}
