package me.whizvox.gameshelf.game;

import me.whizvox.gameshelf.media.GenericMedia;
import me.whizvox.gameshelf.media.Media;
import me.whizvox.gameshelf.platform.Platform;
import me.whizvox.gameshelf.rating.Rating;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.time.LocalDate;
import java.util.List;

public class GameBase {

  public String title;

  public String description;

  public List<String> aliases;

  public List<String> languages;

  @DocumentReference
  public List<Platform> platforms;

  @DocumentReference
  public List<Rating> ratings;

  public LocalDate releaseDate;

  public List<GenericMedia> media;

  @DocumentReference
  public Media boxArt;

  public GameBase() {
  }

  public GameBase(String title, String description, List<String> aliases, List<String> languages, List<Platform> platforms, List<Rating> ratings, LocalDate releaseDate, List<GenericMedia> media, Media boxArt) {
    this.title = title;
    this.description = description;
    this.aliases = aliases;
    this.languages = languages;
    this.platforms = platforms;
    this.ratings = ratings;
    this.releaseDate = releaseDate;
    this.media = media;
    this.boxArt = boxArt;
  }

}
