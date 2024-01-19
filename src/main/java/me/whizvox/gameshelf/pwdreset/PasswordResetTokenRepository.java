package me.whizvox.gameshelf.pwdreset;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;

public interface PasswordResetTokenRepository extends MongoRepository<PasswordResetToken, String> {

  @Query(value = "{ 'expired': { $lt: ?0 } }", delete = true)
  void deleteExpired(LocalDateTime now);

}
