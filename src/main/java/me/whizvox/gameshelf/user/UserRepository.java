package me.whizvox.gameshelf.user;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

  @Query(value = "{ 'username': ?0 }", collation = "{ 'locale': 'en', 'strength': 2 }")
  Optional<User> findByUsername(String username);

  @Query(value = "{ 'email': ?0 }", collation = "{ 'locale': 'en', 'strength': 2 }")
  Optional<User> findByEmail(String email);

  @Query(value = "{ $and: [ {'banExpires': {$ne: null}}, {'banExpires': {$lte: ?0}}, {'permaBanned': false} ] }")
  List<User> findAllWithExpiredBans(LocalDateTime now);

}
