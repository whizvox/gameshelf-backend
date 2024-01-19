package me.whizvox.gameshelf.user;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import me.whizvox.gameshelf.util.ObjectIdHexSerializer;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;

public class UserInfo {

  @JsonSerialize(using = ObjectIdHexSerializer.class)
  public ObjectId id;
  public String username;
  public String email;
  public Role role;
  public boolean verified;
  public LocalDateTime banExpires;
  public LocalDateTime lastModified;

  public UserInfo(ObjectId id, String username, String email, Role role, boolean verified, LocalDateTime banExpires, LocalDateTime lastModified) {
    this.id = id;
    this.username = username;
    this.email = email;
    this.role = role;
    this.verified = verified;
    this.banExpires = banExpires;
    this.lastModified = lastModified;
  }

  public UserInfo(User user) {
    this(user.id, user.username, user.email, user.role, user.verified, user.banExpires, user.lastModified);
  }

}
