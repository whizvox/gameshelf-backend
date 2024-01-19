package me.whizvox.gameshelf.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FileStorageConfiguration {

  @Bean
  public FileStorage fileStorage(@Value("${gameshelf.storage.local.rootDir:./storage}") String rootDir) throws Exception {
    return new LocalFileStorage(rootDir);
  }

}
