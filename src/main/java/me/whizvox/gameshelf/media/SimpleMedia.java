package me.whizvox.gameshelf.media;

import org.bson.types.ObjectId;

import java.util.Objects;

public class SimpleMedia {

  public ObjectId id;

  public String altText;

  public SimpleMedia() {
  }

  public SimpleMedia(ObjectId id, String altText) {
    this.id = id;
    this.altText = altText;
  }

  public SimpleMedia(Media media) {
    this(media.id, media.altText);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SimpleMedia that = (SimpleMedia) o;
    return Objects.equals(id, that.id) && Objects.equals(altText, that.altText);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, altText);
  }

  @Override
  public String toString() {
    return "SimpleMedia{" +
        "id=" + id +
        ", altText='" + altText + '\'' +
        '}';
  }

}
