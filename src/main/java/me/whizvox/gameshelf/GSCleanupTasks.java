package me.whizvox.gameshelf;

import me.whizvox.gameshelf.pwdreset.PasswordResetService;
import me.whizvox.gameshelf.user.UserService;
import me.whizvox.gameshelf.verify.EmailVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

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
  }

  @Scheduled(fixedRate = 60000)
  public void cleanupExpiredPasswordResetTokens() {
    resetService.deleteAllExpired();
  }

  @Scheduled(fixedRate = 1800000)
  public void unbanUsers() {
    userService.clearExpiredBans();
  }

}
