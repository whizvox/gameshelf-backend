package me.whizvox.gameshelf.genre;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import me.whizvox.gameshelf.util.ObjectIdHexSerializer;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("genres")
public class Genre {

  @JsonSerialize(using = ObjectIdHexSerializer.class)
  public ObjectId id;

  @Indexed(unique = true)
  public String name;

  public String description;

  public boolean explicit;

  public Genre(String name, String description, boolean explicit) {
    this.name = name;
    this.description = description;
    this.explicit = explicit;
    id = null;
  }
}
