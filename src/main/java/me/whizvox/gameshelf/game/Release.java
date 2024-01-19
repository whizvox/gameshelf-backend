package me.whizvox.gameshelf.game;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import me.whizvox.gameshelf.media.GenericMedia;
import me.whizvox.gameshelf.media.Media;
import me.whizvox.gameshelf.platform.Platform;
import me.whizvox.gameshelf.rating.Rating;
import me.whizvox.gameshelf.util.ObjectIdHexSerializer;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@Document("releases")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Release extends GameBase {

  @JsonSerialize(using = ObjectIdHexSerializer.class)
  public ObjectId id;

  public ObjectId game;

  public Release(ObjectId id, String name, String description, List<String> aliases, List<String> languages,
                 List<Platform> platforms, List<Rating> ratings, LocalDate releaseDate, List<GenericMedia> media,
                 Media boxArt, ObjectId game) {
    super(name, description, aliases, languages, platforms, ratings, releaseDate, media, boxArt);
    this.id = id;
    this.game = game;
  }

  public Release() {
  }

}
