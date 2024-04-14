package me.whizvox.gameshelf.game;

import me.whizvox.gameshelf.media.GenericMedia;
import me.whizvox.gameshelf.media.Media;
import me.whizvox.gameshelf.media.MediaService;
import me.whizvox.gameshelf.platform.Platform;
import me.whizvox.gameshelf.platform.PlatformService;
import me.whizvox.gameshelf.rating.Rating;
import me.whizvox.gameshelf.rating.RatingService;
import me.whizvox.gameshelf.util.ArgumentsUtils;
import me.whizvox.gameshelf.util.ServiceUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class GameService {

  private final GameRepository gameRepo;
  private final ReleaseRepository releaseRepo;
  private final MediaService mediaService;
  private final PlatformService platformService;
  private final RatingService ratingService;

  @Autowired
  public GameService(GameRepository gameRepo,
                     ReleaseRepository releaseRepo,
                     MediaService mediaService,
                     PlatformService platformService,
                     RatingService ratingService) {
    this.gameRepo = gameRepo;
    this.releaseRepo = releaseRepo;
    this.mediaService = mediaService;
    this.platformService = platformService;
    this.ratingService = ratingService;
  }

  public Optional<Game> findById(ObjectId id) {
    return gameRepo.findById(id);
  }

  public Optional<GameBase> findEitherById(ObjectId id) {
    Optional<Game> game = findById(id);
    if (game.isPresent()) {
      return game.map(g -> g);
    }
    return findReleaseById(id).map(r -> r);
  }

  public Page<Game> findAll(Pageable pageable) {
    return gameRepo.findAll(pageable);
  }

  public Game create(String name,
                     @Nullable String description,
                     @Nullable LocalDate releaseDate,
                     @Nullable List<String> aliases,
                     @Nullable List<String> languages,
                     @Nullable List<ObjectId> platformIds,
                     @Nullable List<ObjectId> ratingIds,
                     @Nullable List<GenericMedia> media,
                     @Nullable ObjectId boxArtMediaId,
                     @Nullable List<GameRelation> relations) {
    List<Platform> platforms = ServiceUtils.getListOrNotFound(platformService::findById, platformIds, Platform.class);
    List<Rating> ratings = ServiceUtils.getListOrNotFound(ratingService::findById, ratingIds, Rating.class);
    Media boxArt = ServiceUtils.getOrNotFound(mediaService::findById, boxArtMediaId, Media.class);
    ServiceUtils.verifyGenericMedia(media, mediaService::findById);
    ServiceUtils.verifyGameRelations(relations, gameRepo::findById);
    Game game = new Game(null, name, description, Objects.requireNonNullElse(aliases, List.of()),
        Objects.requireNonNullElse(languages, List.of()), platforms, ratings, releaseDate,
        Objects.requireNonNullElse(media, List.of()), boxArt, List.of(), relations);
    return gameRepo.save(game);
  }

  private void readUpdateArguments(GameBase game, MultiValueMap<String, String> args) {
    ArgumentsUtils.getString(args, "name", value -> game.title = value);
    ArgumentsUtils.getString(args, "description", value -> game.description = value);
    ArgumentsUtils.getDate(args, "releaseDate", value -> game.releaseDate = value);
    ArgumentsUtils.getStringList(args, "aliases", value -> game.aliases = value);
    ArgumentsUtils.getStringList(args, "languages", value -> game.languages = value);
    ArgumentsUtils.getObjectIdList(args, "platforms", value ->
        game.platforms = ServiceUtils.getListOrNotFound(platformService::findById, value, Platform.class)
    );
    ArgumentsUtils.getObjectIdList(args, "ratings", value ->
        game.ratings = ServiceUtils.getListOrNotFound(ratingService::findById, value, Rating.class)
    );
    ArgumentsUtils.getGenericMediaList(args, "media", value -> {
      ServiceUtils.verifyGenericMedia(value, mediaService::findById);
      game.media = value;
    });
    ArgumentsUtils.getObjectId(args, "boxArt", value ->
        game.boxArt = ServiceUtils.getOrNotFound(mediaService::findById, value, Media.class)
    );
  }

  public Game update(ObjectId id, MultiValueMap<String, String> args) {
    Game game = ServiceUtils.getOrNotFound(gameRepo::findById, id, Game.class);
    readUpdateArguments(game, args);
    ArgumentsUtils.getStringList(args, "relations", value -> {
      List<GameRelation> relations = value.stream().map(GameRelation::parse).toList();
      ServiceUtils.verifyGameRelations(relations, gameRepo::findById);
      game.relations = relations;
    });
    return gameRepo.save(game);
  }

  public void delete(ObjectId id, boolean deleteReleases) {
    if (deleteReleases) {
      Game game = ServiceUtils.getOrNotFound(gameRepo::findById, id, Game.class);
      releaseRepo.deleteAll(game.releases);
    }
    gameRepo.deleteById(id);
  }

  public Optional<Release> findReleaseById(ObjectId id) {
    return findReleaseById(id, false);
  }

  public Optional<Release> findReleaseById(ObjectId id, boolean inherit) {
    Optional<Release> op = releaseRepo.findById(id);
    if (op.isEmpty() || !inherit) {
      return op;
    }
    Release release = op.get();
    Game game = ServiceUtils.getOrNotFound(gameRepo::findById, release.game, Game.class);
    if (release.title == null) {
      release.title = game.title;
    }
    if (release.description == null) {
      release.description = game.description;
    }
    if (release.aliases == null || release.aliases.isEmpty()) {
      release.aliases = game.aliases;
    }
    if (release.languages == null || release.languages.isEmpty()) {
      release.languages = game.languages;
    }
    if (release.platforms == null || release.platforms.isEmpty()) {
      release.platforms = game.platforms;
    }
    if (release.releaseDate == null) {
      release.releaseDate = game.releaseDate;
    }
    if (release.media == null || release.media.isEmpty()) {
      release.media = game.media;
    }
    return Optional.of(release);
  }

  public List<Release> findReleasesByGame(ObjectId gameId) {
    return releaseRepo.findByGame(gameId);
  }

  public Release addRelease(ObjectId gameId,
                            @Nullable String name,
                            @Nullable String description,
                            @Nullable LocalDate releaseDate,
                            @Nullable List<String> aliases,
                            @Nullable List<String> languages,
                            @Nullable List<ObjectId> platformIds,
                            @Nullable List<ObjectId> ratingIds,
                            @Nullable List<GenericMedia> media,
                            @Nullable ObjectId boxArtMediaId) {
    ServiceUtils.getOrNotFound(gameRepo::findById, gameId, Game.class);
    List<Platform> platforms = ServiceUtils.getListOrNotFound(platformService::findById, platformIds, Platform.class);
    List<Rating> ratings = ServiceUtils.getListOrNotFound(ratingService::findById, ratingIds, Rating.class);
    Media boxArt = ServiceUtils.getOrNotFound(mediaService::findById, boxArtMediaId, Media.class);
    ServiceUtils.verifyGenericMedia(media, mediaService::findById);
    Release release = new Release(null, name, description, Objects.requireNonNullElse(aliases, List.of()),
        Objects.requireNonNullElse(languages, List.of()), platforms, ratings, releaseDate, media, boxArt, gameId);
    return releaseRepo.save(release);
  }

  public Release updateRelease(ObjectId id, MultiValueMap<String, String> args) {
    Release release = ServiceUtils.getOrNotFound(releaseRepo::findById, id, Release.class);
    readUpdateArguments(release, args);
    return releaseRepo.save(release);
  }

  public void deleteRelease(ObjectId id) {
    Release release = ServiceUtils.getOrNotFound(releaseRepo::findById, id, Release.class);
    Game game = ServiceUtils.getOrNotFound(gameRepo::findById, release.game, Game.class);
    game.releases.removeIf(r -> r.id.equals(id));
    releaseRepo.deleteById(id);
  }

}
