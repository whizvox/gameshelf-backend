package me.whizvox.gameshelf.game;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ReleaseRepository extends MongoRepository<Release, ObjectId> {

  @Query("{ 'game': ?0 }")
  List<Release> findByGame(ObjectId gameId);

}
