package me.whizvox.gameshelf.pwdreset;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document("passwordResetTokens")
public class PasswordResetToken {

  @Id
  public String id;

  public ObjectId user;

  public LocalDateTime expires;

  public PasswordResetToken() {
  }

  public PasswordResetToken(String id, ObjectId user, LocalDateTime expires) {
    this.id = id;
    this.user = user;
    this.expires = expires;
  }

}
