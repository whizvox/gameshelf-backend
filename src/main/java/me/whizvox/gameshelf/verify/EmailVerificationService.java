package me.whizvox.gameshelf.verify;

import me.whizvox.gameshelf.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class EmailVerificationService {

  private static final int TOKEN_LENGTH = 12;

  private final EmailVerificationTokenRepository tokenRepo;

  @Autowired
  public EmailVerificationService(EmailVerificationTokenRepository tokenRepo) {
    this.tokenRepo = tokenRepo;
  }

  public Optional<EmailVerificationToken> find(String token) {
    return tokenRepo.findById(token);
  }

  public boolean exists(String token) {
    return find(token).isPresent();
  }

  public EmailVerificationToken create(String userId) {
    String tokenStr = StringUtils.createSecureRandomSequence(TOKEN_LENGTH);
    EmailVerificationToken token = new EmailVerificationToken(tokenStr, userId, LocalDateTime.now().plusMinutes(30));
    tokenRepo.save(token);
    return token;
  }

  public void delete(String token) {
    tokenRepo.deleteById(token);
  }

  public void deleteAllExpired() {
    tokenRepo.deleteAllExpired(LocalDateTime.now());
  }

}
