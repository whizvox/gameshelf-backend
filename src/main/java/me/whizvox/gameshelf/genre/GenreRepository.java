package me.whizvox.gameshelf.genre;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GenreRepository extends MongoRepository<Genre, ObjectId> {

  @Query("{ 'name': ?0 }")
  Optional<Genre> findByName(String name);

  @Query("{ 'explicit': false }")
  List<Genre> findAllNonExplicit();

}
