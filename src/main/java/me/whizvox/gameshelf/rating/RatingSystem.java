package me.whizvox.gameshelf.rating;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import me.whizvox.gameshelf.media.Media;
import me.whizvox.gameshelf.util.ObjectIdHexSerializer;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.time.LocalDate;
import java.util.List;

@Document("ratingSystems")
public class RatingSystem {

  @JsonSerialize(using = ObjectIdHexSerializer.class)
  public ObjectId id;

  @Indexed(unique = true)
  public String shortName;

  public String name;

  public String description;

  public List<String> regions;

  @DocumentReference
  public Media logo;

  public LocalDate founded;

  @DocumentReference
  public List<Rating> ratings;

  public RatingSystem() {
  }

  public RatingSystem(String shortName, String name, String description, List<String> regions, Media logo, LocalDate founded, List<Rating> ratings) {
    this.shortName = shortName;
    this.name = name;
    this.description = description;
    this.regions = regions;
    this.logo = logo;
    this.founded = founded;
    this.ratings = ratings;
    id = null;
  }

}
