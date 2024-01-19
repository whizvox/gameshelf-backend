package me.whizvox.gameshelf.user;

import org.springframework.lang.Nullable;

public enum Role {

  GUEST,
  MEMBER,
  EDITOR,
  MODERATOR,
  ADMIN,
  SUPERUSER;

  public boolean hasPermission(Role minRole) {
    return ordinal() >= minRole.ordinal();
  }

  public Role[] getGrantedRoles() {
    Role[] roles = new Role[ordinal() + 1];
    System.arraycopy(values(), 0, roles, 0, ordinal() + 1);
    return roles;
  }

  @Nullable
  public static Role fromInt(int i) {
    if (i >= 0 && i < values().length) {
      return values()[i];
    }
    return null;
  }

  @Nullable
  public static Role fromName(String name) {
    for (Role role : Role.values()) {
      if (role.toString().equalsIgnoreCase(name)) {
        return role;
      }
    }
    return null;
  }

}
