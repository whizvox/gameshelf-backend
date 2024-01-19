package me.whizvox.gameshelf.gamelist;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface GameListRepository extends MongoRepository<GameListEntry, ObjectId> {

  @Query("{ 'user': ?0 }")
  List<GameListEntry> findByUser(ObjectId user);

  @Query("{ 'user': ?0, 'status': ?1 }")
  List<GameListEntry> findByUserAndStatus(ObjectId user, GameStatus status);

}
