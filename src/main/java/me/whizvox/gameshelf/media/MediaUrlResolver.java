package me.whizvox.gameshelf.media;

import me.whizvox.gameshelf.util.strfmt.StringFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MediaUrlResolver {

  private final StringFormatter formatter;

  public MediaUrlResolver(@Value("${gameshelf.media.urlResolverFormat}") String format) {
    formatter = StringFormatter.of(format);
  }

  public String resolve(String basePath) {
    Map<String, Object> args = Map.of("path", basePath);
    return formatter.render(args);
  }

}
