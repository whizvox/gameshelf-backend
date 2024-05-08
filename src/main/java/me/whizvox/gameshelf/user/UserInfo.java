package me.whizvox.gameshelf.user;

import java.time.LocalDateTime;

public class UserInfo {

  public String id;
  public String username;
  public String email;
  public Role role;
  public boolean verified;
  public LocalDateTime banExpires;
  public boolean permaBanned;
  public LocalDateTime lastModified;

  public UserInfo(String id, String username, String email, Role role, boolean verified, LocalDateTime banExpires, boolean permaBanned, LocalDateTime lastModified) {
    this.id = id;
    this.username = username;
    this.email = email;
    this.role = role;
    this.verified = verified;
    this.banExpires = banExpires;
    this.permaBanned = permaBanned;
    this.lastModified = lastModified;
  }

  public UserInfo(User user) {
    this(user.id, user.username, user.email, user.role, user.verified, user.banExpires, user.permaBanned, user.updatedAt);
  }

}
