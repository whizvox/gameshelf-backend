package me.whizvox.gameshelf.verify;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;

public interface EmailVerificationTokenRepository extends MongoRepository<EmailVerificationToken, String> {

  @Query(value = "{ 'expires': { $lt: ?0 } }", delete = true)
  void deleteAllExpired(LocalDateTime now);

}
