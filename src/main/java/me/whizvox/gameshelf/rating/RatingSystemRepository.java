package me.whizvox.gameshelf.rating;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface RatingSystemRepository extends MongoRepository<RatingSystem, ObjectId> {

  @Query("{ 'shortName': ?0 }")
  Optional<RatingSystem> findByShortName(String shortName);

}
