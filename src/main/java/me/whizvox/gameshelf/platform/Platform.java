package me.whizvox.gameshelf.platform;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import me.whizvox.gameshelf.media.GenericMedia;
import me.whizvox.gameshelf.media.Media;
import me.whizvox.gameshelf.util.ObjectIdHexSerializer;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@Document("platforms")
public class Platform {

  @JsonSerialize(using = ObjectIdHexSerializer.class)
  public ObjectId id;

  @Indexed(unique = true)
  public String shortName;

  public String name;

  public String description;

  public Media image;

  public LocalDate releaseDate;

  public List<GenericMedia> media;

  public Platform() {
  }

  public Platform(String shortName, String name, String description, Media image, LocalDate releaseDate, List<GenericMedia> media) {
    this.shortName = shortName;
    this.name = name;
    this.description = description;
    this.image = image;
    this.releaseDate = releaseDate;
    this.media = media;
    id = null;
  }

}
