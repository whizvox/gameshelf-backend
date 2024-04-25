package me.whizvox.gameshelf.media;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import me.whizvox.gameshelf.util.ObjectIdHexSerializer;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document
public class Media {

  @JsonSerialize(using = ObjectIdHexSerializer.class)
  public ObjectId id;

  @Indexed
  public long size;

  @Indexed
  public String mimeType;

  @Indexed
  public String fileName;

  @TextIndexed
  public String altText;

  public List<String> tags;

  @Indexed
  public LocalDateTime uploaded;

  @Indexed
  public LocalDateTime lastEdited;

  public Media() {
  }

  public Media(long size, String mimeType, String fileName, String altText, List<String> tags) {
    this.size = size;
    this.mimeType = mimeType;
    this.fileName = fileName;
    this.altText = altText;
    this.tags = tags;
    id = null;
    uploaded = LocalDateTime.now();
    lastEdited = null;
  }

}
