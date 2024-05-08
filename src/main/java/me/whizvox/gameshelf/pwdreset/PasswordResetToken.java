package me.whizvox.gameshelf.pwdreset;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document("passwordResetTokens")
public class PasswordResetToken {

  @Id
  public String id;

  public String user;

  public LocalDateTime expires;

  public PasswordResetToken() {
  }

  public PasswordResetToken(String id, String user, LocalDateTime expires) {
    this.id = id;
    this.user = user;
    this.expires = expires;
  }

}
