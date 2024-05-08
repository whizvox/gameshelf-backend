package me.whizvox.gameshelf.media;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.util.List;

@Document
public class Media {

  @Id
  public String id;

  @Indexed
  public long size;

  @Indexed
  public String mimeType;

  @Indexed
  public String origFileName;

  @Indexed
  public String filePath;

  @TextIndexed
  public String altText;

  public List<String> tags;

  @Indexed
  public LocalDateTime createdAt;

  @Nullable
  @Indexed
  public LocalDateTime updatedAt;

  public Media() {
  }

  public Media(String id, long size, String mimeType, String origFileName, String filePath, String altText, List<String> tags) {
    this.id = id;
    this.size = size;
    this.mimeType = mimeType;
    this.origFileName = origFileName;
    this.filePath = filePath;
    this.altText = altText;
    this.tags = tags;
    createdAt = LocalDateTime.now();
    updatedAt = null;
  }

}
