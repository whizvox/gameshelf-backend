package me.whizvox.gameshelf;

import me.whizvox.gameshelf.pwdreset.PasswordResetService;
import me.whizvox.gameshelf.user.UserService;
import me.whizvox.gameshelf.verify.EmailVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import static me.whizvox.gameshelf.util.GSLog.LOGGER;

@Configuration
public class GSCleanupTasks {

  private final EmailVerificationService verificationService;
  private final PasswordResetService resetService;
  private final UserService userService;

  @Autowired
  public GSCleanupTasks(EmailVerificationService verificationService,
                        PasswordResetService resetService,
                        UserService userService) {
    this.verificationService = verificationService;
    this.resetService = resetService;
    this.userService = userService;
  }

  @Scheduled(fixedRate = 60000)
  public void cleanupExpiredEmailVerificationTokens() {
    verificationService.deleteAllExpired();
    LOGGER.trace("Cleaned up expired verification tokens");
  }

  @Scheduled(fixedRate = 60000)
  public void cleanupExpiredPasswordResetTokens() {
    resetService.deleteAllExpired();
    LOGGER.trace("Cleaned up expired password reset tokens");
  }

  @Scheduled(fixedRate = 1800000)
  public void unbanUsers() {
    userService.clearExpiredBans();
  }

}
