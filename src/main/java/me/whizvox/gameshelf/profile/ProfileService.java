package me.whizvox.gameshelf.profile;

import me.whizvox.gameshelf.exception.ServiceException;
import me.whizvox.gameshelf.game.Game;
import me.whizvox.gameshelf.game.GameBase;
import me.whizvox.gameshelf.game.GameService;
import me.whizvox.gameshelf.game.Release;
import me.whizvox.gameshelf.gamelist.GameListEntry;
import me.whizvox.gameshelf.gamelist.GameListRepository;
import me.whizvox.gameshelf.user.User;
import me.whizvox.gameshelf.user.UserRepository;
import me.whizvox.gameshelf.util.ArgumentsUtils;
import me.whizvox.gameshelf.util.ErrorTypes;
import me.whizvox.gameshelf.util.ServiceUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProfileService {

  public static final int
      MAX_BIOGRAPHY_LENGTH = 2000,
      MAX_FAVORITE_GAMES = 30;

  private final ProfileRepository profileRepo;
  private final GameListRepository listRepo;
  private final UserRepository userRepo;
  private final GameService gameService;

  @Autowired
  public ProfileService(ProfileRepository profileRepo,
                        GameListRepository listRepo,
                        UserRepository userRepo,
                        GameService gameService) {
    this.profileRepo = profileRepo;
    this.listRepo = listRepo;
    this.userRepo = userRepo;
    this.gameService = gameService;
  }

  public Optional<Profile> findById(ObjectId id) {
    return profileRepo.findById(id);
  }

  public Optional<Profile> findByUsername(String username) {
    return profileRepo.findByUsername(username);
  }

  public Profile create(User user) {
    if (user.id == null) {
      throw ServiceException.internalServerError("User ID cannot be null", null);
    }
    return profileRepo.save(new Profile(user));
  }

  public Profile update(ObjectId id, MultiValueMap<String, String> args) {
    Profile profile = ServiceUtils.getOrNotFound(this::findById, id, Profile.class);
    ArgumentsUtils.getString(args, "biography", value -> {
      if (value == null || value.isBlank()) {
        value = "";
      } else if (value.length() > MAX_BIOGRAPHY_LENGTH) {
        throw ServiceException.error(ErrorTypes.BIOGRAPHY_TOO_LONG);
      }
      profile.biography = value;
    });
    ArgumentsUtils.getInt(args, "birthdayYear", value -> {
      if (value < 0) {
        throw ServiceException.error(ErrorTypes.INVALID_BIRTHDAY);
      }
      profile.birthdayYear = value;
    });
    ArgumentsUtils.getInt(args, "birthdayMonth", value -> {
      if (value < 1 || value > 12) {
        throw ServiceException.error(ErrorTypes.INVALID_BIRTHDAY);
      }
      profile.birthdayMonth = value;
    });
    ArgumentsUtils.getInt(args, "birthdayDay", value -> {
      if (value < 1 || value > 31) {
        throw ServiceException.error(ErrorTypes.INVALID_BIRTHDAY);
      }
      profile.birthdayDay = value;
    });
    ArgumentsUtils.getObjectIdList(args, "favoriteGames", value -> {
      if (value.size() > MAX_FAVORITE_GAMES) {
        throw ServiceException.error(ErrorTypes.TOO_MANY_FAVORITE_GAMES);
      }
      List<FavoriteGameEntry> games = new ArrayList<>();
      value.forEach(gameId -> {
        GameBase gameBase = ServiceUtils.getOrNotFound(gameService::findEitherById, gameId, GameBase.class);
        FavoriteGameEntry entry;
        if (gameBase instanceof Game game) {
          entry = new FavoriteGameEntry(game);
        } else if (gameBase instanceof Release release) {
          entry = new FavoriteGameEntry(release);
        } else {
          throw ServiceException.error(ErrorTypes.INVALID_GAME_AND_RELEASE, Map.of("gameOrReleaseId", gameId));
        }
        games.add(entry);
      });
      profile.favoriteGames = games;
    });
    return profileRepo.save(profile);
  }

  public Profile updateGameListFields(ObjectId id) {
    Profile profile = ServiceUtils.getOrNotFound(profileRepo::findById, id, Profile.class);
    List<GameListEntry> entries = listRepo.findByUser(id);
    profile.averageRating = 0;
    profile.totalGames = entries.size();
    profile.totalGamesPlaying = 0;
    profile.totalGamesFinished = 0;
    profile.totalGamesStopped = 0;
    profile.totalGamesDropped = 0;
    profile.totalGamesOnHold = 0;
    profile.totalGamesPlanned = 0;
    for (GameListEntry entry : entries) {
      if (entry.rating > 0) {
        profile.averageRating += entry.rating;
      }
      switch (entry.status) {
        case PLAYING -> profile.totalGamesPlaying++;
        case FINISHED -> profile.totalGamesFinished++;
        case STOPPED -> profile.totalGamesStopped++;
        case DROPPED -> profile.totalGamesDropped++;
        case ON_HOLD -> profile.totalGamesOnHold++;
        case PLANNED -> profile.totalGamesPlanned++;
      }
    }
    profile.averageRating /= entries.size();
    return profileRepo.save(profile);
  }

  public Profile updateUsername(ObjectId id, String username) {
    Profile profile = ServiceUtils.getOrNotFound(profileRepo::findById, id, Profile.class);
    profile.username = username;
    return profileRepo.save(profile);
  }

  public Profile updateUsername(ObjectId id) {
    User user = ServiceUtils.getOrNotFound(userRepo::findById, id, User.class);
    return updateUsername(id, user.username);
  }

}
