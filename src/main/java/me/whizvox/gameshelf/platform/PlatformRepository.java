package me.whizvox.gameshelf.platform;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface PlatformRepository extends MongoRepository<Platform, ObjectId> {

  @Query("{ 'shortName': ?0 }")
  Optional<Platform> findByShortName(String shortName);

}
