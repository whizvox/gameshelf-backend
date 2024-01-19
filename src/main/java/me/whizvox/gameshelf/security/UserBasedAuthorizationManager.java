package me.whizvox.gameshelf.security;

import me.whizvox.gameshelf.user.Role;
import me.whizvox.gameshelf.user.User;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.util.function.Supplier;

public class UserBasedAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

  private final Role role;
  private final String authority;
  private final boolean mustBeVerified;
  private final boolean mustNotBeBanned;

  public UserBasedAuthorizationManager(Role role, boolean mustBeVerified, boolean mustNotBeBanned) {
    this.role = role;
    authority = "ROLE_" + role.name();
    this.mustBeVerified = mustBeVerified;
    this.mustNotBeBanned = mustNotBeBanned;
  }

  @Override
  public AuthorizationDecision check(Supplier<Authentication> authSup, RequestAuthorizationContext context) {
    Authentication auth = authSup.get();
    boolean granted = false;
    if (auth == null) {
      granted = role == Role.GUEST;
    } else if (auth.getPrincipal() instanceof User user) {
      granted = user.role.hasPermission(role);
      if (mustBeVerified) {
        granted &= user.verified;
      }
      if (mustNotBeBanned) {
        granted &= !user.isBanned();
      }
    } else {
      for (GrantedAuthority authority : auth.getAuthorities()) {
        if (authority.getAuthority().equals(this.authority)) {
          granted = true;
          break;
        }
      }
    }
    return new AuthorizationDecision(granted);
  }

}
