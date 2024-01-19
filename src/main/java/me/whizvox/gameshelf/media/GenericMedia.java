package me.whizvox.gameshelf.media;

import java.util.Set;

/**
 * Any database record that can hold "media" will use this class. Media can either be hosted on a Game Shelf instance,
 * or some external website, such as YouTube or Vimeo.
 */
public class GenericMedia {

  /**
   * The source of this media.
   */
  public String source;

  /**
   * The identification string for this piece of media. Depending on the value of {@link #source}, this will refer to
   * different things.
   * <br>
   * If the source is <code>self</code>, then this media is available to fetch at <code>/media/{id}</code>
   */
  public String id;

  public GenericMedia() {
  }

  public GenericMedia(String source, String id) {
    this.source = source;
    this.id = id;
  }

  public boolean isSelfHosted() {
    return "self".equals(source);
  }

  public static final Set<String> ALLOWED_SOURCES = Set.of("self", "youtube", "vimeo", "imgur");

  public static GenericMedia parse(String str) {
    int separatorIndex = str.indexOf(',');
    if (separatorIndex < 0) {
      throw new IllegalArgumentException("Invalid generic media string: " + str);
    }
    String source = str.substring(0, separatorIndex).toLowerCase();
    if (!ALLOWED_SOURCES.contains(source)) {
      throw new IllegalArgumentException("Unknown source: " + source);
    }
    return new GenericMedia(source, str.substring(separatorIndex + 1));
  }

}
