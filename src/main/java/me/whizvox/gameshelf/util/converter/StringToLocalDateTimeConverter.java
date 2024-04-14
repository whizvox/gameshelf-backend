package me.whizvox.gameshelf.util.converter;

import me.whizvox.gameshelf.util.DateAndTimeUtils;
import org.springframework.core.convert.converter.Converter;

import java.time.LocalDateTime;

//@Component
public class StringToLocalDateTimeConverter implements Converter<String, LocalDateTime> {

  @Override
  public LocalDateTime convert(String source) {
    return DateAndTimeUtils.parseDateTime(source);
  }

}
