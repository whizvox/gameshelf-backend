package me.whizvox.gameshelf.pwdreset;

import me.whizvox.gameshelf.util.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PasswordResetService {

  public static final int TOKEN_LENGTH = 12;

  private final PasswordResetTokenRepository tokenRepo;

  @Autowired
  public PasswordResetService(PasswordResetTokenRepository tokenRepo) {
    this.tokenRepo = tokenRepo;
  }

  public Optional<PasswordResetToken> find(String token) {
    return tokenRepo.findById(token);
  }

  public boolean exists(String token) {
    return find(token).isPresent();
  }

  public PasswordResetToken create(ObjectId user) {
    String tokenStr = StringUtils.createSecureRandomSequence(TOKEN_LENGTH);
    PasswordResetToken token = new PasswordResetToken(tokenStr, user, LocalDateTime.now().plusMinutes(30));
    tokenRepo.save(token);
    return token;
  }

  public void delete(String token) {
    tokenRepo.deleteById(token);
  }

  public void deleteAllExpired() {
    tokenRepo.deleteExpired(LocalDateTime.now());
  }

}
