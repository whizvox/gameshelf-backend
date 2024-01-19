package me.whizvox.gameshelf.user;

import me.whizvox.gameshelf.exception.ServiceException;
import me.whizvox.gameshelf.profile.ProfileService;
import me.whizvox.gameshelf.pwdreset.PasswordResetService;
import me.whizvox.gameshelf.pwdreset.PasswordResetToken;
import me.whizvox.gameshelf.util.ArgumentsUtils;
import me.whizvox.gameshelf.util.ErrorTypes;
import me.whizvox.gameshelf.util.ServiceUtils;
import me.whizvox.gameshelf.verify.EmailVerificationService;
import me.whizvox.gameshelf.verify.EmailVerificationToken;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.lang.Nullable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

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

  private final MongoTemplate mongoTemplate;
  private final UserRepository userRepo;
  private final PasswordEncoder passwordEncoder;
  private final PasswordResetService pwdResetService;
  private final EmailVerificationService verificationService;
  private final ProfileService profileService;

  @Autowired
  public UserService(MongoTemplate mongoTemplate,
                     UserRepository userRepo,
                     PasswordEncoder passwordEncoder,
                     PasswordResetService pwdResetService,
                     EmailVerificationService verificationService,
                     ProfileService profileService) {
    this.mongoTemplate = mongoTemplate;
    this.userRepo = userRepo;
    this.passwordEncoder = passwordEncoder;
    this.pwdResetService = pwdResetService;
    this.verificationService = verificationService;
    this.profileService = profileService;
  }

  public Optional<User> findById(ObjectId id) {
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
    ArgumentsUtils.getBoolean(args, "banned", value -> {
      Criteria criteria = Criteria.where("banExpires");
      if (value) {
        query.addCriteria(criteria.ne(null));
      } else {
        query.addCriteria(criteria.isNull());
      }
    });
    ArgumentsUtils.getBoolean(args, "permabanned", value -> {
      if (value) {
        query.addCriteria(Criteria.where("banExpires").gte(User.PERMABAN_DATE_TIME));
      } else {
        query.addCriteria(new Criteria().orOperator(
            Criteria.where("banExpires").isNull(),
            Criteria.where("banExpires").lt(User.PERMABAN_DATE_TIME))
        );
      }
    });
    ArgumentsUtils.getDateTime(args, "modifiedAfter", value -> query.addCriteria(Criteria.where("lastModified").gte(value)));
    ArgumentsUtils.getDateTime(args, "modifiedBefore", value -> query.addCriteria(Criteria.where("lastModified").lte(value)));
    query.with(pageable);
    return PageableExecutionUtils.getPage(mongoTemplate.find(query, User.class), pageable, () -> mongoTemplate.count(query.limit(-1).skip(-1), User.class));
  }

  public boolean isUsernameAvailable(String username) {
    return findByUsername(username).isEmpty();
  }

  public boolean isEmailAvailable(String email) {
    return findByEmail(email).isEmpty();
  }

  public User create(String username, String email, String password, @Nullable Role role, boolean verified) {
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
    User user = new User(username, email, passwordEncoder.encode(password), role, verified);
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
    ArgumentsUtils.getEnum(args, "role", Role.class, value -> user.role = value);
    ArgumentsUtils.getBoolean(args, "verified", value -> user.verified = value);
    user.lastModified = LocalDateTime.now();
    userRepo.save(user);
    if (args.containsKey("username")) {
      profileService.updateUsername(user.id, user.username);
    }
    LOGGER.info("User {} has been updated ({})", user.toFriendlyString(), String.join(", ", args.keySet()));
    return user;
  }

  public User update(ObjectId id, MultiValueMap<String, String> args) {
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

  private void ban_do(User issuer, ObjectId targetId, LocalDateTime banExpires) {
    if (targetId.equals(issuer.id)) {
      throw ServiceException.error(ErrorTypes.CANNOT_BAN_SELF);
    }
    User target = ServiceUtils.getOrNotFound(this::findById, targetId, User.class);
    if (!issuer.role.hasPermission(target.role)) {
      throw ServiceException.error(ErrorTypes.TARGET_ROLE_HIGHER);
    }
    if (target.isBanned()) {
      throw ServiceException.error(ErrorTypes.USER_ALREADY_BANNED);
    }
    target.banExpires = banExpires;
    userRepo.save(target);
    if (banExpires.equals(User.PERMABAN_DATE_TIME)) {
      LOGGER.info("User {} has been permanently banned by {}", target.toFriendlyString(), issuer.toFriendlyString());
    } else {
      LOGGER.info("User {} has been banned until {} by {}", target.toFriendlyString(), banExpires.format(DateTimeFormatter.ISO_DATE), issuer.toFriendlyString());
    }
  }

  public void banTemporarily(User issuer, ObjectId targetId, int days) {
    if (days <= 0) {
      throw ServiceException.badRequest("Days cannot be below 1");
    }
    ban_do(issuer, targetId, LocalDateTime.now().plusDays(days));
  }

  public void banPermanently(User issuer, ObjectId targetId) {
    ban_do(issuer, targetId, User.PERMABAN_DATE_TIME);
  }

  public void unban(User issuer, ObjectId targetId) {
    User target = ServiceUtils.getOrNotFound(this::findById, targetId, User.class);
    if (!issuer.role.hasPermission(target.role)) {
      throw ServiceException.error(ErrorTypes.TARGET_ROLE_HIGHER);
    }
    if (!target.isBanned()) {
      throw ServiceException.error(ErrorTypes.USER_NOT_BANNED);
    }
    target.banExpires = null;
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

  public void delete(ObjectId id) {
    findById(id).ifPresent(user -> {
      userRepo.deleteById(id);
      LOGGER.info("User {} has been deleted", user.toFriendlyString());
    });
  }

  /*
   * EMAIL VERIFICATION
   */

  public boolean isEmailVerificationTokenValid(String token) {
    return verificationService.exists(token);
  }

  public void sendVerificationEmail(ObjectId userId) {
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

  public void requestPasswordReset(ObjectId userId) {
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
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return findByUsernameOrEmail(username).orElseThrow(() -> new UsernameNotFoundException("Unknown username or email"));
  }

}
