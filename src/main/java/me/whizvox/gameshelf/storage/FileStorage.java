package me.whizvox.gameshelf.storage;

import java.io.InputStream;

public interface FileStorage {

  boolean exists(String path);

  void upload(String path, InputStream in, boolean replaceExisting);

  default void upload(String path, InputStream in) {
    upload(path, in, false);
  }

  InputStream openStream(String path);

  void delete(String path);

}
