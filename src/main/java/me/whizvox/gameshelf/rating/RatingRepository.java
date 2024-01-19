package me.whizvox.gameshelf.rating;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface RatingRepository extends MongoRepository<Rating, ObjectId> {

  @Query(value = "{ 'ratingSystem': ?0 }", fields = "{ 'ratingSystem': 0 }")
  List<Rating> findBySystem(ObjectId ratingSystemId);

}
