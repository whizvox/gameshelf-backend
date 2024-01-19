package me.whizvox.gameshelf.storage;

import me.whizvox.gameshelf.exception.ServiceException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class LocalFileStorage implements FileStorage {

  private final Path rootDir;

  public LocalFileStorage(String rootDir) throws IOException {
    this.rootDir = Paths.get(rootDir).toAbsolutePath().normalize();
  }

  public Path resolve(String filePath) {
    Path path = rootDir.resolve(filePath).normalize();
    if (!path.startsWith(rootDir)) {
      throw ServiceException.badRequest("Illegal file path");
    }
    return path;
  }

  private void makeParentDirectories(Path path) throws IOException {
    Files.createDirectories(path.getParent());
  }

  @Override
  public boolean exists(String filePath) {
    return Files.exists(resolve(filePath));
  }

  @Override
  public void upload(String filePath, InputStream in, boolean replaceExisting) {
    try {
      Path path = resolve(filePath);
      if (!replaceExisting) {
        makeParentDirectories(path);
      }
      if (replaceExisting) {
        Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
      } else {
        Files.copy(in, path);
      }
    } catch (IOException e) {
      throw ServiceException.internalServerError("Could not copy file", e);
    }
  }

  @Override
  public InputStream openStream(String path) {
    try {
      return Files.newInputStream(resolve(path));
    } catch (IOException e) {
      throw ServiceException.internalServerError("Could not fetch file", e);
    }
  }

  @Override
  public void delete(String path) {
    Path filePath = resolve(path);
    try {
      Files.deleteIfExists(filePath);
    } catch (IOException e) {
      throw ServiceException.internalServerError("Could not delete file", e);
    }
  }

}
