package me.whizvox.gameshelf.media;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import me.whizvox.gameshelf.util.ObjectIdHexSerializer;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
public class Media {

  @JsonSerialize(using = ObjectIdHexSerializer.class)
  public ObjectId id;

  @Indexed
  public long size;

  @Indexed
  public String mimeType;

  @TextIndexed
  public String altText;

  @Indexed
  public LocalDateTime uploaded;

  @Indexed
  public LocalDateTime lastEdited;

  public Media() {
  }

  public Media(long size, String mimeType, String altText) {
    this.size = size;
    this.mimeType = mimeType;
    this.altText = altText;
    id = null;
    uploaded = LocalDateTime.now();
    lastEdited = null;
  }

}
