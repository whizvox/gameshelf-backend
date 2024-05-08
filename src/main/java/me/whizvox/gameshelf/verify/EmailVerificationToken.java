package me.whizvox.gameshelf.verify;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document("emailVerificationTokens")
public class EmailVerificationToken {

  @Id
  public String id;

  public String user;

  public LocalDateTime expires;

  public EmailVerificationToken() {
  }

  public EmailVerificationToken(String id, String user, LocalDateTime expires) {
    this.id = id;
    this.user = user;
    this.expires = expires;
  }

}
