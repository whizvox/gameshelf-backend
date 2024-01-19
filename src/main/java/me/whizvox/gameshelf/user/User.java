package me.whizvox.gameshelf.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import me.whizvox.gameshelf.util.DateAndTimeUtils;
import me.whizvox.gameshelf.util.ObjectIdHexSerializer;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
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

  public static final LocalDateTime PERMABAN_DATE_TIME = LocalDateTime.of(99999, 12, 31, 23, 59, 59);

  @JsonSerialize(using = ObjectIdHexSerializer.class)
  public ObjectId id;

  @Indexed(unique = true, collation = "{ 'locale': 'en', strength: 2 }")
  public String username;

  @Indexed(unique = true, sparse = true, collation = "{ 'locale': 'en', strength: 2 }")
  public String email;

  public String encpwd;

  public Role role;

  public boolean verified;

  public LocalDateTime banExpires;

  public LocalDateTime lastModified;

  public User() {
  }

  public User(String username, String email, String encpwd, Role role, boolean verified) {
    id = null;
    lastModified = null;
    banExpires = null;
    this.username = username;
    this.email = email;
    this.encpwd = encpwd;
    this.role = role;
    this.verified = verified;
  }

  public boolean isBanned() {
    return banExpires != null;
  }

  public boolean shouldBeUnbanned() {
    return banExpires != null && banExpires.isBefore(LocalDateTime.now());
  }

  public void banPermanently() {
    banExpires = PERMABAN_DATE_TIME;
  }

  public void banTemporarily(int days) {
    banExpires = LocalDateTime.now().plusDays(days);
  }

  public void unban() {
    banExpires = null;
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
    if (o == null || getClass() != o.getClass()) return false;
    User user = (User) o;
    return verified == user.verified && Objects.equals(id, user.id) && Objects.equals(username, user.username) &&
        Objects.equals(email, user.email) && Objects.equals(encpwd, user.encpwd) && role == user.role &&
        DateAndTimeUtils.equalsMinusNanos(banExpires, user.banExpires) &&
        DateAndTimeUtils.equalsMinusNanos(lastModified, user.lastModified);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, username, email, encpwd, role, verified, banExpires, lastModified);
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
        ", lastModified=" + lastModified +
        '}';
  }

}
