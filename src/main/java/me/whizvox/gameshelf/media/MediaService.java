package me.whizvox.gameshelf.media;

import me.whizvox.gameshelf.exception.ServiceException;
import me.whizvox.gameshelf.storage.FileStorage;
import me.whizvox.gameshelf.util.ArgumentsUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class MediaService {

  private final MongoTemplate mongoTemplate;
  private final MediaRepository mediaRepo;
  private final FileStorage fileStorage;

  @Autowired
  public MediaService(MongoTemplate mongoTemplate,
                      MediaRepository mediaRepo,
                      FileStorage fileStorage) {
    this.mongoTemplate = mongoTemplate;
    this.mediaRepo = mediaRepo;
    this.fileStorage = fileStorage;
  }

  public String getFileStoragePath(ObjectId id) {
    return "media/" + id.toHexString();
  }

  public Page<Media> findAll(Pageable pageable, MultiValueMap<String, String> args) {
    Query query = new Query();
    ArgumentsUtils.getInt(args, "minSize", minSize -> {
      query.addCriteria(Criteria.where("size").gte(minSize));
    });
    ArgumentsUtils.getInt(args, "maxSize", maxSize -> {
      query.addCriteria(Criteria.where("size").lte(maxSize));
    });
    ArgumentsUtils.getDateTime(args, "uploadedAfter", uploadedAfter -> {
      query.addCriteria(Criteria.where("uploaded").gte(uploadedAfter));
    });
    ArgumentsUtils.getDateTime(args, "uploadedBefore", uploadedBefore -> {
      query.addCriteria(Criteria.where("uploaded").lte(uploadedBefore));
    });
    ArgumentsUtils.getBoolean(args, "edited", edited -> {
      Criteria criteria = Criteria.where("lastEdited");
      if (edited) {
        query.addCriteria(criteria.not().isNull());
      } else {
        query.addCriteria(criteria.isNull());
      }
    });
    ArgumentsUtils.getDateTime(args, "editedAfter", editedAfter -> {
      query.addCriteria(Criteria.where("lastEdited").gte(editedAfter));
    });
    ArgumentsUtils.getDateTime(args, "editedBefore", editedAfter -> {
      query.addCriteria(Criteria.where("lastEdited").lte(editedAfter));
    });
    // not going to happen terribly often, so it's fine for these to be regex-d
    ArgumentsUtils.getString(args, "fileName", fileName -> {
      query.addCriteria(Criteria.where("fileName").regex(Pattern.quote(fileName)));
    });
    ArgumentsUtils.getStringList(args, "allTags", tags -> {
      query.addCriteria(new Criteria().andOperator(tags.stream().map(tag -> Criteria.where("tags").regex(Pattern.quote(tag))).toList()));
    });
    ArgumentsUtils.getStringList(args, "anyTags", tags -> {
      query.addCriteria(new Criteria().orOperator(tags.stream().map(tag -> Criteria.where("tags").regex(Pattern.quote(tag))).toList()));
    });
    ArgumentsUtils.getString(args, "altText", keywords -> {
      query.addCriteria(TextCriteria.forDefaultLanguage().matchingAny(keywords.split(" ")).caseSensitive(true));
    });
    ArgumentsUtils.getString(args, "type", type -> {
      query.addCriteria(Criteria.where("mimeType").is(type));
    });
    ArgumentsUtils.getString(args, "typeRegex", type -> {
      query.addCriteria(Criteria.where("mimeType").regex(Pattern.quote(type)));
    });
    query.with(pageable);
    return PageableExecutionUtils.getPage(mongoTemplate.find(query, Media.class), pageable, () -> mongoTemplate.count(query.limit(-1).skip(-1), Media.class));
  }

  public Optional<Media> findById(ObjectId id) {
    return mediaRepo.findById(id);
  }

  public InputStream openStream(ObjectId id) {
    return fileStorage.openStream(getFileStoragePath(id));
  }

  public Media create(InputStream in, long size, String mimeType, String fileName, @Nullable String altText, List<String> tags) {
    Media media = new Media(size, mimeType, fileName, altText, tags);
    mediaRepo.save(media);
    try {
      fileStorage.upload(getFileStoragePath(media.id), in);
    } catch (ServiceException e) {
      // remove redundant record
      mediaRepo.deleteById(media.id);
      throw e;
    }
    return media;
  }

  public Media update(ObjectId id, @Nullable MultipartFile file, MultiValueMap<String, String> args) {
    Media media = findById(id).orElseThrow(() -> ServiceException.notFound("Could not find media with ID " + id));
    if (file != null) {
      media.size = file.getSize();
      media.mimeType = file.getContentType();
    }
    ArgumentsUtils.getString(args, "fileName", value -> media.fileName = value);
    ArgumentsUtils.getString(args, "altText", value -> media.altText = value);
    ArgumentsUtils.getStringList(args, "tags", value -> media.tags = value);
    media.lastEdited = LocalDateTime.now();
    mediaRepo.save(media);

    if (file != null) {
      try (InputStream in = file.getInputStream()) {
        fileStorage.upload(getFileStoragePath(id), in, true);
      } catch (IOException e) {
        throw ServiceException.internalServerError("Could not read file", e);
      }
    }
    return media;
  }

  public void delete(ObjectId id) {
    mediaRepo.deleteById(id);
    fileStorage.delete(getFileStoragePath(id));
  }

}
