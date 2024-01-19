package me.whizvox.gameshelf.rating;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import me.whizvox.gameshelf.media.Media;
import me.whizvox.gameshelf.util.ObjectIdHexSerializer;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.List;

@Document("ratings")
public class Rating {

  @JsonSerialize(using = ObjectIdHexSerializer.class)
  public ObjectId id;

  public String shortName;

  public String name;

  public String description;

  public boolean explicit;

  @DocumentReference
  public List<Media> logos;

  public DenormalizedRatingSystem ratingSystem;

  public Rating() {
  }

  public Rating(String shortName, String name, String description, boolean explicit, List<Media> logos, DenormalizedRatingSystem ratingSystem) {
    this.shortName = shortName;
    this.name = name;
    this.description = description;
    this.explicit = explicit;
    this.logos = logos;
    this.ratingSystem = ratingSystem;
    id = null;
  }

  public static class DenormalizedRatingSystem {

    @JsonSerialize(using = ObjectIdHexSerializer.class)
    public ObjectId id;

    public String shortName;

    public String name;

    public DenormalizedRatingSystem(ObjectId id, String shortName, String name) {
      this.id = id;
      this.shortName = shortName;
      this.name = name;
    }

  }

}
