package me.whizvox.gameshelf.platform;

import me.whizvox.gameshelf.exception.ServiceException;
import me.whizvox.gameshelf.media.GenericMedia;
import me.whizvox.gameshelf.media.Media;
import me.whizvox.gameshelf.media.MediaRepository;
import me.whizvox.gameshelf.util.ArgumentsUtils;
import me.whizvox.gameshelf.util.ServiceUtils;
import me.whizvox.gameshelf.util.ShortNamedDocumentService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class PlatformService implements ShortNamedDocumentService {

  private final PlatformRepository platformRepo;
  private final MediaRepository mediaRepo;

  @Autowired
  public PlatformService(PlatformRepository platformRepo,
                         MediaRepository mediaRepo) {
    this.platformRepo = platformRepo;
    this.mediaRepo = mediaRepo;
  }

  public Optional<Platform> findById(ObjectId id) {
    return platformRepo.findById(id);
  }

  @Override
  public Optional<Platform> findByShortName(String shortName) {
    return platformRepo.findByShortName(shortName);
  }

  public Page<Platform> findAll(Pageable pageable) {
    return platformRepo.findAll(Objects.requireNonNullElse(pageable, Pageable.ofSize(20)));
  }

  public Platform create(String shortName, String name, @Nullable String description, @Nullable ObjectId imageMediaId, @Nullable LocalDate releaseDate, @Nullable List<GenericMedia> media) {
    checkShortNameAvailable(shortName);
    Media image;
    if (imageMediaId != null) {
      image = ServiceUtils.getOrNotFound(mediaRepo::findById, imageMediaId, Media.class);
    } else {
      image = null;
    }
    Platform platform = new Platform(shortName, name, description, image, releaseDate, Objects.requireNonNullElse(media, List.of()));
    return platformRepo.save(platform);
  }

  public Platform update(ObjectId id, MultiValueMap<String, String> args) {
    Platform platform = findById(id).orElseThrow(() -> ServiceException.notFound("Platform with id " + id + " not found"));
    ArgumentsUtils.getString(args, "shortName", value -> {
      checkShortNameAvailable(value);
      platform.shortName = value;
    });
    ArgumentsUtils.getString(args, "name", value -> platform.name = value);
    ArgumentsUtils.getString(args, "description", value -> platform.description = value);
    ArgumentsUtils.getObjectId(args, "image", value ->
        platform.image = ServiceUtils.getOrNotFound(mediaRepo::findById, value, Media.class)
    );
    ArgumentsUtils.getDate(args, "releaseDate", value -> platform.releaseDate = value);
    ArgumentsUtils.getGenericMediaList(args, "addMedia", value -> {
      if (platform.media == null) {
        platform.media = new ArrayList<>();
      }
      platform.media.addAll(value);
    });
    ArgumentsUtils.getGenericMediaList(args, "removeMedia", value -> {
      if (platform.media == null) {
        platform.media = new ArrayList<>();
      }
      platform.media.removeIf(value::contains);
    });
    ArgumentsUtils.getGenericMediaList(args, "media", value -> platform.media = value);
    return platformRepo.save(platform);
  }

  public void delete(ObjectId id) {
    platformRepo.deleteById(id);
  }

}
