package me.whizvox.gameshelf.media;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface MediaRepository extends MongoRepository<Media, ObjectId> {

}
