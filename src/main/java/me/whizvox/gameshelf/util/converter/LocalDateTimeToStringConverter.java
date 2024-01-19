package me.whizvox.gameshelf.util.converter;

import me.whizvox.gameshelf.util.DateAndTimeUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

//@Component
public class LocalDateTimeToStringConverter implements Converter<LocalDateTime, String> {

  @Override
  public String convert(LocalDateTime source) {
    return DateAndTimeUtils.formatDateTime(source);
  }

}
