package me.whizvox.gameshelf.profile;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface ProfileRepository extends MongoRepository<Profile, ObjectId> {

  @Query("{ 'username': ?0 }")
  Optional<Profile> findByUser(String userId);

  @Query("{ 'username': ?0 }")
  Optional<Profile> findByUsername(String username);

}
