package me.whizvox.gameshelf.user;

import me.whizvox.gameshelf.exception.ServiceException;
import me.whizvox.gameshelf.response.ApiResponse;
import me.whizvox.gameshelf.response.PagedData;
import me.whizvox.gameshelf.util.ErrorTypes;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("${apiPrefix}/user")
public class UserController {

  private final UserService userService;

  @Autowired
  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping
  public ResponseEntity<Object> get(@RequestParam(required = false) ObjectId id,
                                    @RequestParam(required = false) String username,
                                    @RequestParam(required = false) String email,
                                    @RequestParam MultiValueMap<String, String> args,
                                    @PageableDefault Pageable pageable) {
    if (id != null) {
      return ApiResponse.ok(userService.findById(id).map(UserInfo::new));
    }
    if (username != null) {
      return ApiResponse.ok(userService.findByUsername(username).map(UserInfo::new));
    }
    if (email != null) {
      return ApiResponse.ok(userService.findByEmail(email).map(UserInfo::new));
    }
    return ApiResponse.ok(new PagedData(userService.findAll(pageable, args), UserInfo::new));
  }

  @GetMapping("/self")
  public ResponseEntity<Object> getSelf(@AuthenticationPrincipal User user) {
    return ApiResponse.ok(Optional.ofNullable(user).map(UserInfo::new));
  }

  @GetMapping("/checklogin")
  public ResponseEntity<Object> isLoggedIn(@AuthenticationPrincipal User user) {
    return ApiResponse.ok(user != null);
  }

  @GetMapping("/available")
  public ResponseEntity<Object> available(@RequestParam(required = false) String username,
                                          @RequestParam(required = false) String email) {
    Map<String, Boolean> retObject = new HashMap<>();
    if (username != null) {
      retObject.put("username", userService.isUsernameAvailable(username));
    }
    if (email != null) {
      retObject.put("email", userService.isEmailAvailable(email));
    }
    if (retObject.isEmpty()) {
      throw ServiceException.badRequest("Must specify \"username\" and/or \"email\"");
    }
    return ApiResponse.ok(retObject);
  }

  @GetMapping("/exists")
  public ResponseEntity<Object> exists(@RequestParam(required = false) String reset,
                                       @RequestParam(required = false) String verify) {
    if (reset != null) {
      return ApiResponse.ok(userService.isPasswordResetTokenValid(reset));
    }
    if (verify != null) {
      return ApiResponse.ok(userService.isEmailVerificationTokenValid(verify));
    }
    throw ServiceException.badRequest("Must specify \"reset\" or \"verify\" token");
  }

  @PostMapping
  public ResponseEntity<Object> create(@RequestParam String username,
                                       @RequestParam(required = false) String email,
                                       @RequestParam String password,
                                       @RequestParam(defaultValue = "MEMBER") Role role,
                                       @RequestParam(defaultValue = "false") boolean verified,
                                       @AuthenticationPrincipal User user) {
    Role prevRole = role.getPreviousRole();
    if (prevRole != null && !user.role.hasPermission(prevRole)) {
      throw ServiceException.error(ErrorTypes.ROLE_TOO_HIGH);
    }
    User createdUser = userService.create(username, email, password, role, verified);
    return ApiResponse.created(new UserInfo(createdUser));
  }

  @PostMapping("/request/reset")
  public ResponseEntity<Object> requestPasswordReset(@AuthenticationPrincipal User user) {
    userService.requestPasswordReset(user.id);
    return ApiResponse.created();
  }

  @PostMapping("/request/verify")
  public ResponseEntity<Object> requestEmailVerification(@AuthenticationPrincipal User user) {
    userService.sendVerificationEmail(user.id);
    return ApiResponse.created();
  }

  @PutMapping
  public ResponseEntity<Object> update(@RequestParam ObjectId id,
                                       @RequestParam MultiValueMap<String, String> args,
                                       @AuthenticationPrincipal User user) {
    User updatedUser = userService.update(id, args);
    return ApiResponse.ok(new UserInfo(updatedUser));
  }

  @PutMapping("/self")
  public ResponseEntity<Object> updateSelf(@AuthenticationPrincipal User user,
                                           @RequestParam String currentPassword,
                                           @RequestParam MultiValueMap<String, String> args) {
    User updatedUser = userService.updateSelf(user, currentPassword, args);
    return ApiResponse.ok(new UserInfo(updatedUser));
  }

  @PostMapping("/ban")
  public ResponseEntity<Object> banUser(@AuthenticationPrincipal User user,
                                        @RequestParam ObjectId target,
                                        @RequestParam(required = false) Integer days,
                                        @RequestParam(required = false) Boolean forever) {
    if (forever != null && forever) {
      userService.banPermanently(user, target);
    } else if (days != null) {
      userService.banTemporarily(user, target, days);
    } else {
      throw ServiceException.badRequest("Must specify days or forever");
    }
    return ApiResponse.ok();
  }

  @PostMapping("/unban")
  public ResponseEntity<Object> unbanUser(@AuthenticationPrincipal User issuer,
                                          @RequestParam ObjectId target) {
    userService.unban(issuer, target);
    return ApiResponse.ok();
  }

  @PutMapping("/reset")
  public ResponseEntity<Object> resetPassword(@RequestParam String token,
                                              @RequestParam String newPassword) {
    userService.resetPassword(token, newPassword);
    return ApiResponse.ok();
  }

  @PutMapping("/verify")
  public ResponseEntity<Object> verifyEmail(@RequestParam String token) {
    userService.verifyEmail(token);
    return ApiResponse.ok();
  }

  @DeleteMapping
  public ResponseEntity<Object> delete(@RequestParam ObjectId id) {
    userService.delete(id);
    return ApiResponse.ok();
  }

}
