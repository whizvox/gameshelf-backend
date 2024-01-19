package me.whizvox.gameshelf.util;

import me.whizvox.gameshelf.exception.ServiceException;
import me.whizvox.gameshelf.media.GenericMedia;
import org.bson.types.ObjectId;
import org.springframework.util.MultiValueMap;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

public class ArgumentsUtils {

  public static <T> void getValue(MultiValueMap<String, String> args, String key, Consumer<T> consumer, Function<String, T> parser) {
    if (args.containsKey(key)) {
      T value = parser.apply(args.getFirst(key));
      consumer.accept(value);
    }
  }

  public static void getString(MultiValueMap<String, String> args, String key, Consumer<String> consumer) {
    getValue(args, key, consumer, s -> s);
  }

  public static void getBoolean(MultiValueMap<String, String> args, String key, Consumer<Boolean> consumer) {
    getValue(args, key, consumer, StringUtils::parseBoolean);
  }

  public static void getObjectId(MultiValueMap<String, String> args, String key, Consumer<ObjectId> consumer) {
    getValue(args, key, consumer, ObjectId::new);
  }

  public static void getInt(MultiValueMap<String, String> args, String key, Consumer<Integer> consumer) {
    getValue(args, key, consumer, Integer::parseInt);
  }

  public static void getDate(MultiValueMap<String, String> args, String key, Consumer<LocalDate> consumer) {
    getValue(args, key, consumer, DateAndTimeUtils::parseDate);
  }

  public static void getDateTime(MultiValueMap<String, String> args, String key, Consumer<LocalDateTime> consumer) {
    getValue(args, key, consumer, DateAndTimeUtils::parseDateTime);
  }

  public static <T extends Enum<T>> void getEnum(MultiValueMap<String, String> args, String key, Class<T> enumClass, Consumer<T> consumer) {
    getString(args, key, s -> {
      try {
        int ordinal = Integer.parseInt(s);
        if (ordinal >= 0 && ordinal < enumClass.getEnumConstants().length) {
          consumer.accept(enumClass.getEnumConstants()[ordinal]);
        } else {
          throw ServiceException.badRequest("Invalid ordinal for " + enumClass.getSimpleName() + ": " + s);
        }
      } catch (NumberFormatException e) {
        for (T value : enumClass.getEnumConstants()) {
          if (value.toString().equalsIgnoreCase(s)) {
            consumer.accept(value);
            return;
          }
        }
        throw ServiceException.badRequest("Invalid name " + enumClass.getSimpleName() + ": " + s);
      }
    });
  }

  public static <T> void getList(MultiValueMap<String, String> args, String key, Consumer<List<T>> consumer, Function<String, T> parser) {
    if (args.containsKey(key)) {
      List<T> list = args.get(key).stream().map(parser).toList();
      consumer.accept(list);
    }
  }

  public static void getStringList(MultiValueMap<String, String> args, String key, Consumer<List<String>> consumer) {
    getList(args, key, consumer, s -> s);
  }

  public static void getObjectIdList(MultiValueMap<String, String> args, String key, Consumer<List<ObjectId>> consumer) {
    getList(args, key, consumer, ObjectId::new);
  }

  public static void getGenericMediaList(MultiValueMap<String, String> args, String key, Consumer<List<GenericMedia>> consumer) {
    getList(args, key, consumer, GenericMedia::parse);
  }

}
