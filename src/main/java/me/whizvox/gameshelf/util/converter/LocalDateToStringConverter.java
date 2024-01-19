package me.whizvox.gameshelf.util.converter;

import me.whizvox.gameshelf.util.DateAndTimeUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

//@Component
public class LocalDateToStringConverter implements Converter<LocalDate, String> {

  @Override
  public String convert(LocalDate source) {
    return DateAndTimeUtils.formatDate(source);
  }

}
