package me.whizvox.gameshelf.util.converter;

import me.whizvox.gameshelf.util.DateAndTimeUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

//@Component
public class StringToLocalDateConverter implements Converter<String, LocalDate> {

  @Override
  public LocalDate convert(String source) {
    return DateAndTimeUtils.parseDate(source);
  }

}
