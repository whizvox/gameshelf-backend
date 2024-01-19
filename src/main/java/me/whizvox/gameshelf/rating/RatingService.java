package me.whizvox.gameshelf.rating;

import me.whizvox.gameshelf.exception.ServiceException;
import me.whizvox.gameshelf.media.Media;
import me.whizvox.gameshelf.media.MediaService;
import me.whizvox.gameshelf.util.ArgumentsUtils;
import me.whizvox.gameshelf.util.GSLog;
import me.whizvox.gameshelf.util.ServiceUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static me.whizvox.gameshelf.util.GSLog.LOGGER;

@Service
public class RatingService {

  private final RatingRepository ratingRepo;
  private final RatingSystemRepository systemRepo;
  private final MediaService mediaService;

  @Autowired
  public RatingService(RatingRepository ratingRepo,
                       RatingSystemRepository systemRepo,
                       MediaService mediaService) {
    this.ratingRepo = ratingRepo;
    this.systemRepo = systemRepo;
    this.mediaService = mediaService;
  }

  public Optional<Rating> findById(ObjectId id) {
    return ratingRepo.findById(id);
  }

  public List<Rating> findBySystem(ObjectId ratingSystemId) {
    return ratingRepo.findBySystem(ratingSystemId);
  }

  public Page<Rating> search(Pageable pageable) {
    return ratingRepo.findAll(pageable);
  }

  public Rating create(String shortName,
                       ObjectId ratingSystemId,
                       @Nullable String name,
                       @Nullable String description,
                       @Nullable List<ObjectId> logoMediaIds,
                       @Nullable Boolean explicit) {
    RatingSystem rs = ServiceUtils.getOrNotFound(systemRepo::findById, ratingSystemId, RatingSystem.class);
    List<Media> logos = ServiceUtils.getListOrNotFound(mediaService::findById, logoMediaIds, Media.class);
    Rating rating = new Rating(shortName, Objects.requireNonNullElse(name, shortName), description,
        Objects.requireNonNullElse(explicit, false), new ArrayList<>(logos),
        new Rating.DenormalizedRatingSystem(rs.id, rs.shortName, rs.name));
    ratingRepo.save(rating);
    try {
      rs.ratings.add(rating);
    } catch (UnsupportedOperationException e) {
      rs.ratings = new ArrayList<>(rs.ratings);
      rs.ratings.add(rating);
    }
    systemRepo.save(rs);
    return rating;
  }

  public Rating update(ObjectId id, MultiValueMap<String, String> args) {
    Rating rating = ServiceUtils.getOrNotFound(this::findById, id, Rating.class);
    ArgumentsUtils.getString(args, "shortName", value -> rating.shortName = value);
    ArgumentsUtils.getString(args, "name", value -> rating.name = value);
    ArgumentsUtils.getString(args, "description", value -> rating.description = value);
    ArgumentsUtils.getBoolean(args, "explicit", value -> rating.explicit = value);
    ArgumentsUtils.getObjectIdList(args, "logos", value -> rating.logos = ServiceUtils.getListOrNotFound(mediaService::findById, value, Media.class));
    ratingRepo.save(rating);
    return rating;
  }

  public void delete(ObjectId id) {
    Rating rating = ServiceUtils.getOrNotFound(this::findById, id, Rating.class);
    RatingSystem rs = ServiceUtils.getOrNotFound(systemRepo::findById, rating.ratingSystem.id, RatingSystem.class);
    try {
      rs.ratings.removeIf(r -> r.id.equals(rating.id));
    } catch (UnsupportedOperationException e) {
      rs.ratings = new ArrayList<>(rs.ratings);
      rs.ratings.removeIf(r -> r.id.equals(rating.id));
    }
    systemRepo.save(rs);
    ratingRepo.deleteById(id);
  }

  // ===============
  //  Rating System
  // ===============

  private void checkSystemShortNameAvailable(String shortName) {
    if (!isSystemShortNameAvailable(shortName)) {
      throw ServiceException.conflict("Short name is already taken: " + shortName);
    }
  }

  public Page<RatingSystem> findAllSystems(Pageable pageable) {
    return systemRepo.findAll(pageable);
  }

  public Optional<RatingSystem> findSystemById(ObjectId id) {
    return systemRepo.findById(id);
  }

  public Optional<RatingSystem> findSystemByShortName(String shortName) {
    return systemRepo.findByShortName(shortName);
  }

  public boolean isSystemShortNameAvailable(String shortName) {
    return systemRepo.findByShortName(shortName).isEmpty();
  }

  public RatingSystem createSystem(String shortName, LocalDate founded, @Nullable String name, @Nullable String description, @Nullable List<String> regions, @Nullable ObjectId logoMediaId) {
    checkSystemShortNameAvailable(shortName);
    Media logo = ServiceUtils.getOrNotFound(mediaService::findById, logoMediaId, Media.class);
    RatingSystem rs = new RatingSystem(shortName, name, description,
        Objects.requireNonNullElse(regions, List.of()), logo, founded, List.of());
    return systemRepo.save(rs);
  }

  public RatingSystem updateSystem(ObjectId id, MultiValueMap<String, String> args) {
    RatingSystem rs = ServiceUtils.getOrNotFound(systemRepo::findById, id, RatingSystem.class);
    AtomicBoolean updateDenormalizedFields = new AtomicBoolean(false);
    ArgumentsUtils.getString(args, "shortName", value -> {
      checkSystemShortNameAvailable(value);
      rs.shortName = value;
      updateDenormalizedFields.set(true);
    });
    ArgumentsUtils.getString(args, "name", value -> {
      rs.name = value;
      updateDenormalizedFields.set(true);
    });
    ArgumentsUtils.getString(args, "description", value -> rs.description = value);
    ArgumentsUtils.getDate(args, "founded", value -> rs.founded = value);
    ArgumentsUtils.getStringList(args, "regions", value -> rs.regions = value);
    ArgumentsUtils.getObjectId(args, "logo", value -> rs.logo = ServiceUtils.getOrNotFound(mediaService::findById, value, Media.class));
    ArgumentsUtils.getObjectIdList(args, "ratings", value -> rs.ratings = value.stream()
        .map(ratingId -> ServiceUtils.getOrNotFound(this::findById, ratingId, Rating.class))
        .toList()
    );
    if (updateDenormalizedFields.get()) {
      List<Rating> ratings = findBySystem(rs.id);
      if (!ratings.isEmpty()) {
        ratings.forEach(rating -> {
          rating.ratingSystem.shortName = rs.shortName;
          rating.ratingSystem.name = rs.name;
        });
        ratingRepo.saveAll(ratings);
        LOGGER.info("Updated {} Rating records with denormalized RatingSystem fields ({})", ratings.size(), rs.id);
      }
    }
    return systemRepo.save(rs);
  }

  public void deleteSystem(ObjectId id) {
    systemRepo.deleteById(id);
  }

}
