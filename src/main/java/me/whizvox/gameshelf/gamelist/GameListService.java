package me.whizvox.gameshelf.gamelist;

import me.whizvox.gameshelf.exception.ServiceException;
import me.whizvox.gameshelf.game.Game;
import me.whizvox.gameshelf.game.GameRepository;
import me.whizvox.gameshelf.game.Release;
import me.whizvox.gameshelf.game.ReleaseRepository;
import me.whizvox.gameshelf.user.User;
import me.whizvox.gameshelf.user.UserRepository;
import me.whizvox.gameshelf.util.ArgumentsUtils;
import me.whizvox.gameshelf.util.ErrorTypes;
import me.whizvox.gameshelf.util.ServiceUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class GameListService {

  private final GameListRepository listRepo;
  private final UserRepository userRepo;
  private final GameRepository gameRepo;
  private final ReleaseRepository releaseRepo;

  @Autowired
  public GameListService(GameListRepository listRepo,
                         UserRepository userRepo,
                         GameRepository gameRepo,
                         ReleaseRepository releaseRepo) {
    this.listRepo = listRepo;
    this.userRepo = userRepo;
    this.gameRepo = gameRepo;
    this.releaseRepo = releaseRepo;
  }

  public Optional<GameListEntry> findById(ObjectId id) {
    return listRepo.findById(id);
  }

  public List<GameListEntry> findByUser(ObjectId userId) {
    return listRepo.findByUser(userId);
  }

  public List<GameListEntry> findByUserAndStatus(ObjectId userId, GameStatus status) {
    return listRepo.findByUserAndStatus(userId, status);
  }

  public GameListEntry create(User user, ObjectId gameId, GameStatus status, @Nullable ObjectId releaseId, int rating,
                              @Nullable String comment, @Nullable LocalDate startDate, @Nullable LocalDate endDate,
                              int hoursPlayed) {
    Game game = ServiceUtils.getOrNotFound(gameRepo::findById, gameId, Game.class);
    if (releaseId != null) {
      Release release = ServiceUtils.getOrNotFound(releaseRepo::findById, releaseId, Release.class);
      if (!release.game.equals(game.id)) {
        throw ServiceException.error(ErrorTypes.RELEASE_DOES_NOT_CORRESPOND);
      }
    }
    if (rating < 0) {
      rating = 0;
    } else if (rating > 10) {
      rating = 10;
    }
    if (comment == null) {
      comment = "";
    } else {
      comment = comment.trim();
    }
    if (hoursPlayed < 0) {
      hoursPlayed = 0;
    }
    return listRepo.save(new GameListEntry(user.id, gameId, releaseId, status, rating, comment, startDate, endDate,
        hoursPlayed, game.title));
  }

  public GameListEntry update(@Nullable User user, ObjectId id, MultiValueMap<String, String> args) {
    GameListEntry entry = ServiceUtils.getOrNotFound(this::findById, id, GameListEntry.class);
    if (user != null && !entry.user.equals(user.id)) {
      throw ServiceException.forbidden();
    }
    ArgumentsUtils.getObjectId(args, "release", value -> {
      Release release = ServiceUtils.getOrNotFound(releaseRepo::findById, value, Release.class);
      if (!release.game.equals(entry.game)) {
        throw ServiceException.error(ErrorTypes.RELEASE_DOES_NOT_CORRESPOND);
      }
      entry.release = value;
    });
    ArgumentsUtils.getEnum(args, "status", GameStatus.class, value -> entry.status = value);
    ArgumentsUtils.getInt(args, "rating", value -> {
      if (value < 0) {
        value = 0;
      } else if (value > 10) {
        value = 10;
      }
      entry.rating = value;
    });
    ArgumentsUtils.getString(args, "comment", value -> entry.comment = value.trim());
    ArgumentsUtils.getDate(args, "startDate", value -> entry.startDate = value);
    ArgumentsUtils.getDate(args, "endDate", value -> entry.endDate = value);
    return listRepo.save(entry);
  }

  public void delete(ObjectId id) {
    listRepo.deleteById(id);
  }

}
