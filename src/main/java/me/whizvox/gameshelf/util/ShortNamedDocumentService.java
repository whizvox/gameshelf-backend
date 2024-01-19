package me.whizvox.gameshelf.util;

import me.whizvox.gameshelf.exception.ServiceException;

import java.util.Optional;

public interface ShortNamedDocumentService {

  Optional<?> findByShortName(String shortName);

  default boolean isShortNameAvailable(String shortName) {
    return findByShortName(shortName).isEmpty();
  }

  default void checkShortNameAvailable(String shortName) {
    if (!isShortNameAvailable(shortName)) {
      throw ServiceException.conflict("Short name is already taken: " + shortName);
    }
  }

}
