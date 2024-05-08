package me.whizvox.gameshelf.gamelist;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.Objects;

@Document("gameLists")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GameListEntry {

  @Id
  public ObjectId id;

  public String user;

  public ObjectId game;

  public ObjectId release;

  public GameStatus status;

  public int rating;

  public String comment;

  public LocalDate startDate;

  public LocalDate endDate;

  public int hoursPlayed;

  // denormalized fields

  public String title;

  public GameListEntry() {
  }

  public GameListEntry(String user, ObjectId game, ObjectId release, GameStatus status, int rating, String comment,
                       LocalDate startDate, LocalDate endDate, int hoursPlayed, String title) {
    id = null;
    this.user = user;
    this.game = game;
    this.release = release;
    this.status = status;
    this.rating = rating;
    this.comment = comment;
    this.startDate = startDate;
    this.endDate = endDate;
    this.hoursPlayed = hoursPlayed;
    this.title = title;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    GameListEntry that = (GameListEntry) o;
    return rating == that.rating && hoursPlayed == that.hoursPlayed && Objects.equals(id, that.id) && Objects.equals(user, that.user) && Objects.equals(game, that.game) && Objects.equals(release, that.release) && status == that.status && Objects.equals(comment, that.comment) && Objects.equals(startDate, that.startDate) && Objects.equals(endDate, that.endDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, user, game, release, status, rating, comment, startDate, endDate, hoursPlayed);
  }

  @Override
  public String toString() {
    return "GameListEntry{" +
        "id=" + id +
        ", user=" + user +
        ", game=" + game +
        ", release=" + release +
        ", status=" + status +
        ", rating=" + rating +
        ", comment='" + comment + '\'' +
        ", startDate=" + startDate +
        ", endDate=" + endDate +
        ", hoursPlayed=" + hoursPlayed +
        ", title='" + title + '\'' +
        '}';
  }

}
