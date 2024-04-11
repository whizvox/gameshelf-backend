package me.whizvox.gameshelf.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import me.whizvox.gameshelf.util.DateAndTimeUtils;
import me.whizvox.gameshelf.util.ObjectIdHexSerializer;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.lang.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

@Document("users")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User implements UserDetails {

  @JsonSerialize(using = ObjectIdHexSerializer.class)
  public ObjectId id;

  @Indexed(unique = true, collation = "{ 'locale': 'en', strength: 2 }")
  public String username;

  @Indexed(unique = true, collation = "{ 'locale': 'en', strength: 2 }")
  public String email;

  public String encpwd;

  public Role role;

  public boolean verified;

  @Nullable
  public LocalDateTime banExpires;

  public boolean permaBanned;

  @Nullable
  public LocalDateTime lastModified;

  public User() {
  }

  public User(String username, String email, String encpwd, Role role, boolean verified) {
    id = null;
    lastModified = null;
    banExpires = null;
    permaBanned = false;
    this.username = username;
    this.email = email;
    this.encpwd = encpwd;
    this.role = role;
    this.verified = verified;
  }

  public boolean isBanned() {
    return banExpires != null || permaBanned;
  }

  public void banPermanently() {
    banExpires = null;
    permaBanned = true;
  }

  public void banTemporarily(int days) {
    banExpires = LocalDateTime.now().plusDays(days);
    permaBanned = false;
  }

  public void unban() {
    banExpires = null;
    permaBanned = false;
  }

  public String toFriendlyString() {
    return id.toHexString() + " (" + username + ")";
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    // user is granted all authorities including and below their role
    return Arrays.stream(role.getGrantedRoles()).map(r -> new SimpleGrantedAuthority("ROLE_" + r.name())).toList();
  }

  @Override
  public String getPassword() {
    return encpwd;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof User user)) return false;
    return verified == user.verified && permaBanned == user.permaBanned && Objects.equals(id, user.id) &&
        Objects.equals(username, user.username) && Objects.equals(email, user.email) &&
        Objects.equals(encpwd, user.encpwd) && role == user.role &&
        DateAndTimeUtils.equalsMinusNanos(banExpires, user.banExpires) &&
        DateAndTimeUtils.equalsMinusNanos(lastModified, user.lastModified);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, username, email, encpwd, role, verified, banExpires, permaBanned, lastModified);
  }

  @Override
  public String toString() {
    return "User{" +
        "id=" + id +
        ", username='" + username + '\'' +
        ", email='" + email + '\'' +
        ", encpwd='" + encpwd + '\'' +
        ", role=" + role +
        ", verified=" + verified +
        ", banExpires=" + banExpires +
        ", permaBanned=" + permaBanned +
        ", lastModified=" + lastModified +
        '}';
  }

}
