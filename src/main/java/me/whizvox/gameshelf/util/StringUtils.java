package me.whizvox.gameshelf.util;

import java.security.SecureRandom;

public class StringUtils {

  private static final char[] RAND_SEQUENCE_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

  public static String createSecureRandomSequence(int length) {
    SecureRandom rand = new SecureRandom();
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      sb.append(RAND_SEQUENCE_CHARS[rand.nextInt(RAND_SEQUENCE_CHARS.length)]);
    }
    return sb.toString();
  }

  public static String replaceAll(String str, String replacing, String replaceWith) {
    StringBuilder sb = new StringBuilder();
    final int W = replaceWith.length();
    final int L = str.length() - replacing.length();
    int last = 0;
    for (int i = 0; i < L; i++) {
      if (str.substring(i, i + W).equals(replacing)) {
        sb.append(str, last, i);
        sb.append(replaceWith);
        last = i + W;
      }
    }
    sb.append(str, last, str.length());
    return sb.toString();
  }

  public static boolean isTraversingFilePath(String path) {
    return path.equals("..") || path.contains("../") || path.contains("..\\") || path.contains("/..") || path.contains("\\..");
  }

  public static boolean parseBoolean(String str) {
    if (str != null) {
      if (str.equals("0") || str.equalsIgnoreCase("false")) {
        return false;
      }
      if (str.equals("1") || str.equalsIgnoreCase("true")) {
        return true;
      }
    }
    throw new IllegalArgumentException("Invalid boolean: " + str);
  }

  public static boolean isNullOrBlank(String str) {
    return str == null || str.isBlank();
  }

}
