package me.whizvox.gameshelf.user;

import me.whizvox.gameshelf.exception.ServiceException;
import me.whizvox.gameshelf.profile.ProfileService;
import me.whizvox.gameshelf.pwdreset.PasswordResetService;
import me.whizvox.gameshelf.pwdreset.PasswordResetToken;
import me.whizvox.gameshelf.security.AccessToken;
import me.whizvox.gameshelf.security.JWTUtil;
import me.whizvox.gameshelf.util.ArgumentsUtils;
import me.whizvox.gameshelf.util.ErrorTypes;
import me.whizvox.gameshelf.util.IDGenerator;
import me.whizvox.gameshelf.util.ServiceUtils;
import me.whizvox.gameshelf.verify.EmailVerificationService;
import me.whizvox.gameshelf.verify.EmailVerificationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.lang.Nullable;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static me.whizvox.gameshelf.util.GSLog.LOGGER;

@Service
public class UserService implements UserDetailsService {

  private static final Pattern
      USERNAME_PATTERN = Pattern.compile("[a-zA-Z0-9_\\-.]{3,20}"),
      EMAIL_PATTERN = Pattern.compile("^[\\w\\-.]+@([\\w-]+\\.)+[\\w-]{2,}$"),
      PASSWORD_PATTERN = Pattern.compile(".{6,}");

  private final IDGenerator idGen;
  private final MongoTemplate mongoTemplate;
  private final UserRepository userRepo;
  private final PasswordEncoder passwordEncoder;
  private final PasswordResetService pwdResetService;
  private final EmailVerificationService verificationService;
  private final ProfileService profileService;
  private final JWTUtil jwtUtil;

  @Autowired
  public UserService(IDGenerator idGen,
                     MongoTemplate mongoTemplate,
                     UserRepository userRepo,
                     PasswordEncoder passwordEncoder,
                     PasswordResetService pwdResetService,
                     EmailVerificationService verificationService,
                     ProfileService profileService,
                     JWTUtil jwtUtil) {
    this.idGen = idGen;
    this.mongoTemplate = mongoTemplate;
    this.userRepo = userRepo;
    this.passwordEncoder = passwordEncoder;
    this.pwdResetService = pwdResetService;
    this.verificationService = verificationService;
    this.profileService = profileService;
    this.jwtUtil = jwtUtil;
  }

  public Optional<User> findById(String id) {
    return userRepo.findById(id);
  }

  public Optional<User> findByUsername(String username) {
    return userRepo.findByUsername(username);
  }

  public Optional<User> findByEmail(String email) {
    return userRepo.findByEmail(email);
  }

  public Optional<User> findByUsernameOrEmail(String query) {
    if (USERNAME_PATTERN.matcher(query).matches()) {
      return findByUsername(query);
    } else {
      return findByEmail(query);
    }
  }

  public Page<User> findAll(Pageable pageable, MultiValueMap<String, String> args) {
    Query query = new Query();
    ArgumentsUtils.getString(args, "usernameRegex", value -> query.addCriteria(Criteria.where("username").regex(value)));
    ArgumentsUtils.getString(args, "emailRegex", value -> query.addCriteria(Criteria.where("email").regex(value)));
    ArgumentsUtils.getBoolean(args, "hasEmail", value -> {
      Criteria criteria = Criteria.where("email");
      if (value) {
        query.addCriteria(criteria.ne(null));
      } else {
        query.addCriteria(criteria.isNull());
      }
    });
    ArgumentsUtils.getEnum(args, "role", Role.class, value -> query.addCriteria(Criteria.where("role").is(value)));
    ArgumentsUtils.getBoolean(args, "verified", value -> query.addCriteria(Criteria.where("verified").is(value)));
    ArgumentsUtils.getDateTime(args, "unbannedAfter", value -> query.addCriteria(Criteria.where("banExpires").gte(value)));
    ArgumentsUtils.getDateTime(args, "unbannedBefore", value -> query.addCriteria(Criteria.where("banExpires").lte(value)));
    ArgumentsUtils.getString(args, "banStatus", value -> {
      switch (value.toLowerCase()) {
        case "notbanned" -> query.addCriteria(new Criteria().andOperator(Criteria.where("banExpires").isNull(), Criteria.where("permaBanned").is(false)));
        case "banned" -> query.addCriteria(new Criteria().orOperator(Criteria.where("banExpires").ne(null), Criteria.where("permaBanned").is(true)));
        case "tempbanned" -> query.addCriteria(new Criteria().andOperator(Criteria.where("banExpires").ne(null), Criteria.where("permaBanned").is(false)));
        case "permabanned" -> query.addCriteria(Criteria.where("permaBanned").is(true));
      }
    });
    ArgumentsUtils.getDateTime(args, "modifiedAfter", value -> query.addCriteria(Criteria.where("updatedAt").gte(value)));
    ArgumentsUtils.getDateTime(args, "modifiedBefore", value -> query.addCriteria(Criteria.where("updatedAt").lte(value)));
    query.with(pageable);
    return PageableExecutionUtils.getPage(mongoTemplate.find(query, User.class), pageable, () -> mongoTemplate.count(query.limit(-1).skip(-1), User.class));
  }

  public boolean isUsernameAvailable(String username) {
    return findByUsername(username).isEmpty();
  }

  public boolean isEmailAvailable(String email) {
    return findByEmail(email).isEmpty();
  }

  public User create(String username, @Nullable String email, String password, @Nullable Role role, boolean verified) {
    if (role == Role.GUEST) {
      throw ServiceException.error(ErrorTypes.CANNOT_CREATE_GUEST_USER);
    }
    ServiceUtils.checkRegex(USERNAME_PATTERN, username, "username");
    ServiceUtils.checkUnique(this::isUsernameAvailable, username, "username");
    if (email != null) {
      ServiceUtils.checkRegex(EMAIL_PATTERN, email, "email");
      ServiceUtils.checkUnique(this::isEmailAvailable, email, "email");
    }
    ServiceUtils.checkRegex(PASSWORD_PATTERN, password, "password");
    if (role == null) {
      role = Role.MEMBER;
    }
    User user = new User(idGen.secureId(), username, email, passwordEncoder.encode(password), role, verified);
    userRepo.save(user);
    profileService.create(user);
    LOGGER.info("User and profile {} has been created", user.toFriendlyString());
    return user;
  }

  private User update(User user, MultiValueMap<String, String> args) {
    ArgumentsUtils.getString(args, "username", value -> {
      ServiceUtils.checkRegex(USERNAME_PATTERN, value, "email");
      ServiceUtils.checkUnique(this::isEmailAvailable, value, "email");
      user.username = value;
    });
    ArgumentsUtils.getString(args, "email", value -> {
      if (value.equals("null")) {
        user.email = null;
      } else {
        ServiceUtils.checkRegex(EMAIL_PATTERN, value, "username");
        ServiceUtils.checkUnique(this::isUsernameAvailable, value, "username");
        user.email = value;
      }
    });
    ArgumentsUtils.getString(args, "password", value -> {
      ServiceUtils.checkRegex(PASSWORD_PATTERN, value, "password");
      user.encpwd = passwordEncoder.encode(value);
    });
    ArgumentsUtils.getEnum(args, "role", Role.class, value -> {
      if (value == Role.GUEST) {
        throw ServiceException.error(ErrorTypes.CANNOT_CREATE_GUEST_USER);
      }
      user.role = value;
    });
    ArgumentsUtils.getBoolean(args, "verified", value -> user.verified = value);
    user.updatedAt = LocalDateTime.now();
    userRepo.save(user);
    if (args.containsKey("username")) {
      profileService.updateUsername(user.id, user.username);
    }
    LOGGER.info("User {} has been updated ({})", user.toFriendlyString(), String.join(", ", args.keySet()));
    return user;
  }

  public User update(String id, MultiValueMap<String, String> args) {
    User user = ServiceUtils.getOrNotFound(this::findById, id, User.class);
    return update(user, args);
  }

  public User updateSelf(User user, String currentPassword, MultiValueMap<String, String> args) {
    if (!passwordEncoder.matches(currentPassword, user.encpwd)) {
      throw ServiceException.badRequest("Current password does not match");
    }
    // only allow updating of username, email, or password with this method
    MultiValueMap<String, String> argsCopy = new LinkedMultiValueMap<>();
    args.forEach((key, values) -> {
      switch (key) {
        case "username", "email", "password" -> argsCopy.put(key, values);
      }
    });
    return update(user, argsCopy);
  }

  private void ban_do(User issuer, String targetId, int days, boolean permaBan) {
    if (targetId.equals(issuer.id)) {
      throw ServiceException.error(ErrorTypes.CANNOT_BAN_SELF);
    }
    User target = ServiceUtils.getOrNotFound(this::findById, targetId, User.class);
    if (!issuer.role.hasPermission(target.role)) {
      throw ServiceException.error(ErrorTypes.TARGET_ROLE_HIGHER);
    }
    if (permaBan) {
      target.banPermanently();
    } else {
      target.banTemporarily(days);
    }
    userRepo.save(target);
    if (permaBan) {
      LOGGER.info("User {} has been permanently banned by {}", target.toFriendlyString(), issuer.toFriendlyString());
    } else {
      //noinspection DataFlowIssue
      LOGGER.info("User {} has been banned until {} by {}", target.toFriendlyString(), target.banExpires.format(DateTimeFormatter.ISO_DATE), issuer.toFriendlyString());
    }
  }

  public void banTemporarily(User issuer, String targetId, int days) {
    if (days <= 0) {
      throw ServiceException.badRequest("Days cannot be below 1");
    }
    ban_do(issuer, targetId, days, false);
  }

  public void banPermanently(User issuer, String targetId) {
    ban_do(issuer, targetId, 0, true);
  }

  public void unban(User issuer, String targetId) {
    User target = ServiceUtils.getOrNotFound(this::findById, targetId, User.class);
    if (!issuer.role.hasPermission(target.role)) {
      throw ServiceException.error(ErrorTypes.TARGET_ROLE_HIGHER);
    }
    if (!target.isBanned()) {
      throw ServiceException.error(ErrorTypes.USER_NOT_BANNED);
    }
    target.unban();
    userRepo.save(target);
    LOGGER.info("User {} has been unbanned by {}", target.toFriendlyString(), issuer.toFriendlyString());
  }

  public void clearExpiredBans() {
    List<User> users = userRepo.findAllWithExpiredBans(LocalDateTime.now());
    users.forEach(user -> {
      user.banExpires = null;
      LOGGER.debug("Ban for {} has expired and has been cleared", user.toFriendlyString());
    });
    userRepo.saveAll(users);
    LOGGER.info("{} users' bans have expired and have been cleared", users.size());
  }

  public void delete(String id) {
    findById(id).ifPresent(user -> {
      userRepo.deleteById(id);
      LOGGER.info("User {} has been deleted", user.toFriendlyString());
    });
  }

  /*
   * ACCESS TOKEN
   */

  public Optional<User> getUserFromAccessToken(String accessToken) {
    String id = jwtUtil.extractUserId(jwtUtil.extractClaims(accessToken));
    return findById(id);
  }

  public AccessToken generateAccessToken(String username, String password, boolean rememberMe) {
    User user = findByUsernameOrEmail(username).orElseThrow(() ->
        ServiceException.error(ErrorTypes.INVALID_USERNAME_OR_PASSWORD)
    );
    if (!passwordEncoder.matches(password, user.encpwd)) {
      throw ServiceException.error(ErrorTypes.INVALID_USERNAME_OR_PASSWORD);
    }
    return jwtUtil.generateToken(user.id, rememberMe ? Duration.ofDays(7) : Duration.ofHours(2));
  }

  /*
   * EMAIL VERIFICATION
   */

  public boolean isEmailVerificationTokenValid(String token) {
    return verificationService.exists(token);
  }

  public void sendVerificationEmail(String userId) {
    User user = ServiceUtils.getOrNotFound(this::findById, userId, User.class);
    EmailVerificationToken token = verificationService.create(userId);
    // TODO Send email to user's email address
    LOGGER.debug("User {} has created a new verification token", user.toFriendlyString());
  }

  public void verifyEmail(String verifyToken) {
    EmailVerificationToken token = ServiceUtils.getOrNotFound(verificationService::find, verifyToken, EmailVerificationToken.class);
    User user = ServiceUtils.getOrNotFound(this::findById, token.user, User.class);
    user.verified = true;
    userRepo.save(user);
    LOGGER.debug("User {} has verified their email address", user.toFriendlyString());
    verificationService.delete(verifyToken);
  }

  /*
   * PASSWORD RESET
   */

  public boolean isPasswordResetTokenValid(String token) {
    return pwdResetService.exists(token);
  }

  public void requestPasswordReset(String userId) {
    User user = ServiceUtils.getOrNotFound(this::findById, userId, User.class);
    PasswordResetToken token = pwdResetService.create(userId);
    // TODO Send email to user's email address
    LOGGER.debug("User {} has requested a password reset", user.toFriendlyString());
  }

  public void resetPassword(String resetToken, String newPassword) {
    PasswordResetToken token = ServiceUtils.getOrNotFound(pwdResetService::find, resetToken, PasswordResetToken.class);
    User user = ServiceUtils.getOrNotFound(this::findById, token.user, User.class);
    ServiceUtils.checkRegex(PASSWORD_PATTERN, newPassword, "password");
    user.encpwd = passwordEncoder.encode(newPassword);
    userRepo.save(user);
    LOGGER.debug("User {} has reset their password", user.toFriendlyString());
    pwdResetService.delete(resetToken);
  }

  @Override
  public User loadUserByUsername(String username) throws UsernameNotFoundException {
    return findByUsernameOrEmail(username).orElseThrow(() -> new UsernameNotFoundException("Unknown username or email"));
  }

}
