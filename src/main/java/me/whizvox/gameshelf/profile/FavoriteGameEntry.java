package me.whizvox.gameshelf.profile;

import me.whizvox.gameshelf.game.Game;
import me.whizvox.gameshelf.game.GameBase;
import me.whizvox.gameshelf.game.Release;
import me.whizvox.gameshelf.media.Media;
import me.whizvox.gameshelf.platform.Platform;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FavoriteGameEntry {

  public ObjectId id;

  public boolean isRelease;

  public String title;

  public Media boxArt;

  public List<Platform> platforms;

  public FavoriteGameEntry() {
  }

  public FavoriteGameEntry(ObjectId id, boolean isRelease, String title, Media boxArt, List<Platform> platforms) {
    this.id = id;
    this.isRelease = isRelease;
    this.title = title;
    this.boxArt = boxArt;
    this.platforms = new ArrayList<>(platforms);
  }

  FavoriteGameEntry(GameBase game, ObjectId id, boolean isRelease) {
    this(id, isRelease, game.title, game.boxArt, game.platforms);
  }

  public FavoriteGameEntry(Game game) {
    this(game, game.id, false);
  }

  public FavoriteGameEntry(Release release) {
    this(release, release.id, true);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    FavoriteGameEntry that = (FavoriteGameEntry) o;
    return isRelease == that.isRelease && Objects.equals(id, that.id) && Objects.equals(title, that.title) && Objects.equals(boxArt, that.boxArt) && Objects.equals(platforms, that.platforms);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, isRelease, title, boxArt, platforms);
  }

  @Override
  public String toString() {
    return "FavoriteGameEntry{" +
        "id=" + id +
        ", isRelease=" + isRelease +
        ", title='" + title + '\'' +
        ", boxArt=" + boxArt +
        ", platforms=" + platforms +
        '}';
  }

}
