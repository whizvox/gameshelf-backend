package me.whizvox.gameshelf.profile;

import me.whizvox.gameshelf.gamelist.GameListEntry;
import me.whizvox.gameshelf.user.User;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Document("profiles")
public class Profile {

  public String user;

  public String biography;

  public int birthdayYear;

  public int birthdayMonth;

  public int birthdayDay;

  public List<FavoriteGameEntry> favoriteGames;

  // denormalized from game list

  public float averageRating;

  public int totalGames;

  public int totalGamesPlaying;

  public int totalGamesFinished;

  public int totalGamesStopped;

  public int totalGamesDropped;

  public int totalGamesOnHold;

  public int totalGamesPlanned;

  // denormalized from user

  @Indexed(unique = true)
  public String username;

  public Profile() {
  }

  public Profile(User user) {
    this.user = user.id;
    biography = "";
    birthdayYear = 0;
    birthdayMonth = 0;
    birthdayDay = 0;
    averageRating = 0;
    totalGames = 0;
    totalGamesPlaying = 0;
    totalGamesFinished = 0;
    totalGamesStopped = 0;
    totalGamesDropped = 0;
    totalGamesOnHold = 0;
    totalGamesPlanned = 0;
    favoriteGames = new ArrayList<>();
    username = user.username;
  }

  public Profile(String userId, String biography, int birthdayYear, int birthdayMonth, int birthdayDay,
                 List<FavoriteGameEntry> favoriteGames, List<GameListEntry> games, String username) {
    user = userId;
    this.biography = biography;
    this.birthdayYear = birthdayYear;
    this.birthdayMonth = birthdayMonth;
    this.birthdayDay = birthdayDay;
    this.favoriteGames = new ArrayList<>(favoriteGames);
    this.username = username;

    totalGames = games.size();
    averageRating = 0;
    totalGamesPlaying = 0;
    totalGamesFinished = 0;
    totalGamesStopped = 0;
    totalGamesDropped = 0;
    totalGamesOnHold = 0;
    totalGamesPlanned = 0;
    for (GameListEntry game : games) {
      switch (game.status) {
        case PLAYING -> totalGamesPlaying++;
        case FINISHED -> totalGamesFinished++;
        case STOPPED -> totalGamesStopped++;
        case DROPPED -> totalGamesDropped++;
        case ON_HOLD -> totalGamesOnHold++;
        case PLANNED -> totalGamesPlanned++;
      }
      averageRating += game.rating;
    }
    averageRating /= totalGames;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Profile profile)) return false;
    return birthdayYear == profile.birthdayYear && birthdayMonth == profile.birthdayMonth &&
        birthdayDay == profile.birthdayDay && Float.compare(averageRating, profile.averageRating) == 0 &&
        totalGames == profile.totalGames && totalGamesPlaying == profile.totalGamesPlaying &&
        totalGamesFinished == profile.totalGamesFinished && totalGamesStopped == profile.totalGamesStopped &&
        totalGamesDropped == profile.totalGamesDropped && totalGamesOnHold == profile.totalGamesOnHold &&
        totalGamesPlanned == profile.totalGamesPlanned && Objects.equals(user, profile.user) &&
        Objects.equals(biography, profile.biography) && Objects.equals(favoriteGames, profile.favoriteGames) &&
        Objects.equals(username, profile.username);
  }

  @Override
  public int hashCode() {
    return Objects.hash(user, biography, birthdayYear, birthdayMonth, birthdayDay, favoriteGames, averageRating,
        totalGames, totalGamesPlaying, totalGamesFinished, totalGamesStopped, totalGamesDropped, totalGamesOnHold,
        totalGamesPlanned, username);
  }

  @Override
  public String toString() {
    return "Profile{" +
        ", user='" + user + '\'' +
        ", biography='" + biography + '\'' +
        ", birthdayYear=" + birthdayYear +
        ", birthdayMonth=" + birthdayMonth +
        ", birthdayDay=" + birthdayDay +
        ", favoriteGames=" + favoriteGames +
        ", averageRating=" + averageRating +
        ", totalGames=" + totalGames +
        ", totalGamesPlaying=" + totalGamesPlaying +
        ", totalGamesFinished=" + totalGamesFinished +
        ", totalGamesStopped=" + totalGamesStopped +
        ", totalGamesDropped=" + totalGamesDropped +
        ", totalGamesOnHold=" + totalGamesOnHold +
        ", totalGamesPlanned=" + totalGamesPlanned +
        ", username='" + username + '\'' +
        '}';
  }

}
