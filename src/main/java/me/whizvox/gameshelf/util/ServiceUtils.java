package me.whizvox.gameshelf.util;

import me.whizvox.gameshelf.exception.ServiceException;
import me.whizvox.gameshelf.game.Game;
import me.whizvox.gameshelf.game.GameRelation;
import me.whizvox.gameshelf.media.GenericMedia;
import me.whizvox.gameshelf.media.Media;
import org.bson.types.ObjectId;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class ServiceUtils {

  public static <ID, T> T getOrNotFound(Function<ID, Optional<T>> getter, ID id, Class<T> clazz) {
    return getter.apply(id).orElseThrow(() -> ServiceException.notFound("No " + clazz.getSimpleName() + " found with ID " + id));
  }

  public static <ID, T> List<T> getListOrNotFound(Function<ID, Optional<T>> getter, @Nullable List<ID> ids, Class<T> clazz) {
    if (ids == null) {
      return List.of();
    } else {
      return ids.stream()
          .map(id -> getOrNotFound(getter, id, clazz))
          .toList();
    }
  }

  public static <T> void checkUnique(Function<T, Boolean> isUniqueFunc, T identifier, String fieldName) {
    if (!isUniqueFunc.apply(identifier)) {
      throw ServiceException.conflict("%s is already taken: %s".formatted(fieldName, identifier));
    }
  }

  public static void checkRegex(Pattern pattern, String str, Supplier<String> errorMessageSupplier) {
    if (!pattern.matcher(str).matches()) {
      throw ServiceException.badRequest(errorMessageSupplier.get());
    }
  }

  public static void checkRegex(Pattern pattern, String str, String fieldName) {
    checkRegex(pattern, str, () -> "Invalid " + fieldName);
  }

  public static void checkBounds(int value, int min, int max, ErrorType errorType) {
    if (value < min || value > max) {
      throw ServiceException.error(errorType);
    }
  }

  public static void checkLength(String str, int min, int max, ErrorType errorType) {
    checkBounds(str.length(), min, max, errorType);
  }

  public static void verifyGenericMedia(@Nullable GenericMedia media, Function<String, Optional<Media>> getter) {
    if (media != null && media.isSelfHosted()) {
      getOrNotFound(getter, media.id, Media.class);
    }
  }

  public static void verifyGenericMedia(@Nullable List<GenericMedia> media, Function<String, Optional<Media>> getter) {
    if (media != null) {
      media.forEach(gMedia -> verifyGenericMedia(gMedia, getter));
    }
  }

  public static void verifyGameRelations(@Nullable List<GameRelation> relations, Function<ObjectId, Optional<Game>> getter) {
    if (relations != null) {
      relations.forEach(relation -> getOrNotFound(getter, relation.game, Game.class));
    }
  }

}
