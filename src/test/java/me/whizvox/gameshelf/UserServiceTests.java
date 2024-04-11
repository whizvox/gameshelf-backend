package me.whizvox.gameshelf;

import me.whizvox.gameshelf.exception.ServiceException;
import me.whizvox.gameshelf.profile.ProfileRepository;
import me.whizvox.gameshelf.user.Role;
import me.whizvox.gameshelf.user.User;
import me.whizvox.gameshelf.user.UserRepository;
import me.whizvox.gameshelf.user.UserService;
import me.whizvox.gameshelf.util.DateAndTimeUtils;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class UserServiceTests {

  private final UserRepository userRepo;
  private final ProfileRepository profileRepo;
  private final UserService userService;

  @Autowired
  public UserServiceTests(UserRepository userRepo,
                          ProfileRepository profileRepo,
                          UserService userService) {
    this.userRepo = userRepo;
    this.profileRepo = profileRepo;
    this.userService = userService;
  }

  private User createUser(String username, String password, Role role, boolean verified) {
    return userService.create(username, username + "@example.com", password, role, verified);
  }

  private User createMember() {
    return createUser("userMember", "password123", Role.MEMBER, true);
  }

  private User createUnverified() {
    return createUser("userUnverified", "password456", Role.MEMBER, false);
  }

  private User createEditor() {
    return createUser("userEditor", "password789", Role.EDITOR, true);
  }

  private User createModerator() {
    return createUser("userModerator", "password012", Role.MODERATOR, true);
  }

  private User createAdmin() {
    return createUser("userAdmin", "password345", Role.ADMIN, true);
  }

  private User createSuperuser() {
    return userService.create("superuser", null, "password678", Role.SUPERUSER, true);
  }

  private User createBanned(int daysOffset) {
    User user = userService.create("userBanned", "userBanned@example.com", "password000", Role.MEMBER, true);
    user.banTemporarily(daysOffset);
    userRepo.save(user);
    return user;
  }

  private User createPermabanned() {
    User user = userService.create("userPermabanned", "userPermabanned@example.com", "password111", Role.MEMBER, true);
    user.banPermanently();
    userRepo.save(user);
    return user;
  }

  private List<User> createAll() {
    return List.of(
        createMember(),
        createUnverified(),
        createEditor(),
        createModerator(),
        createAdmin(),
        createSuperuser()
    );
  }

  private MultiValueMap<String, String> createMultiValueMap(Map<String, String> map) {
    var linkedValueMap = new LinkedMultiValueMap<String, String>();
    map.forEach((key, value) -> linkedValueMap.put(key, List.of(value)));
    return linkedValueMap;
  }

  private Pageable defaultPageable() {
    return PageRequest.of(0, 20);
  }

  @BeforeEach
  void setUp() {
    profileRepo.deleteAll();
    userRepo.deleteAll();
  }

  @Test
  void findById_presentIfExists() {
    User knownMember = createMember();
    Optional<User> member = userService.findById(knownMember.id);
    assertThat(member).contains(knownMember);

    User knownEditor = createEditor();
    Optional<User> editor = userService.findById(knownEditor.id);
    assertThat(editor).contains(knownEditor);
  }

  @Test
  void findById_emptyIfNotExists() {
    assertThat(userService.findById(new ObjectId())).isEmpty();
    assertThat(userService.findById(new ObjectId("659516322e6f3303fba4c016"))).isEmpty();
  }

  @Test
  void findByUsername_presentIfExists() {
    User knownMember = createMember();
    assertThat(userService.findByUsername(knownMember.username)).contains(knownMember);
    assertThat(userService.findByUsername(knownMember.username.toUpperCase())).contains(knownMember);

    User knownAdmin = createAdmin();
    assertThat(userService.findByUsername(knownAdmin.username)).contains(knownAdmin);
    assertThat(userService.findByUsername(knownAdmin.username.toUpperCase())).contains(knownAdmin);
  }

  @Test
  void findByUsername_emptyIfNotExists() {
    createAll();
    assertThat(userService.findByUsername("ajklnsjknd")).isEmpty();
    assertThat(userService.findByUsername("")).isEmpty();
    assertThat(userService.findByUsername("User_Member")).isEmpty();
  }

  @Test
  void findByEmail_presentIfExists() {
    User knownMember = createMember();
    assertThat(userService.findByEmail(knownMember.email)).contains(knownMember);
    assertThat(userService.findByEmail(knownMember.email.toUpperCase())).contains(knownMember);

    User knownMod = createModerator();
    assertThat(userService.findByEmail(knownMod.email)).contains(knownMod);
    assertThat(userService.findByEmail(knownMod.email.toUpperCase())).contains(knownMod);
  }

  @Test
  void findByEmail_emptyIfNotExists() {
    createAll();
    assertThat(userService.findByEmail("kjnaskjd@example.com")).isEmpty();
    assertThat(userService.findByEmail("")).isEmpty();
    assertThat(userService.findByEmail("User_Member@example.com")).isEmpty();
  }

  @Test
  void findByUsernameOrEmail_presentIfUsernameExists() {
    User knownMember = createMember();
    assertThat(userService.findByUsernameOrEmail(knownMember.username)).contains(knownMember);
    assertThat(userService.findByUsernameOrEmail(knownMember.username.toUpperCase())).contains(knownMember);

    User knownEditor = createEditor();
    assertThat(userService.findByUsernameOrEmail(knownEditor.username)).contains(knownEditor);
    assertThat(userService.findByUsernameOrEmail(knownEditor.username.toUpperCase())).contains(knownEditor);
  }

  @Test
  void findByUsernameOrEmail_presentIfEmailExists() {
    User knownMember = createMember();
    assertThat(userService.findByUsernameOrEmail(knownMember.email)).contains(knownMember);
    assertThat(userService.findByUsernameOrEmail(knownMember.email.toUpperCase())).contains(knownMember);

    User knownEditor = createEditor();
    assertThat(userService.findByUsernameOrEmail(knownEditor.email)).contains(knownEditor);
    assertThat(userService.findByUsernameOrEmail(knownEditor.email.toUpperCase())).contains(knownEditor);
  }

  @Test
  void findByUsernameOrEmail_emptyIfNotExists() {
    createAll();

    assertThat(userService.findByUsernameOrEmail("jlkanskjnd")).isEmpty();
    assertThat(userService.findByUsernameOrEmail("")).isEmpty();
    assertThat(userService.findByUsernameOrEmail("User_Member")).isEmpty();
    assertThat(userService.findByUsernameOrEmail("User_Member@example.com")).isEmpty();
  }

  @Test
  void findAll_defaultPageableNoArgs() {
    List<User> knownUsers = createAll();
    Page<User> users = userService.findAll(defaultPageable(), new LinkedMultiValueMap<>());
    assertThatList(users.toList()).containsExactlyInAnyOrderElementsOf(knownUsers);
  }

  @Test
  void findAll_usernameRegex() {
    List<User> knownUsers = createAll();
    User member = knownUsers.get(0);
    User admin = knownUsers.get(4);

    Page<User> users = userService.findAll(defaultPageable(), createMultiValueMap(Map.of("usernameRegex", "user")));
    assertThatStream(users.stream())
        .containsExactlyInAnyOrderElementsOf(knownUsers);
    users = userService.findAll(defaultPageable(), createMultiValueMap(Map.of("usernameRegex", "min")));
    assertThatStream(users.stream())
        .containsExactly(admin);
    users = userService.findAll(defaultPageable(), createMultiValueMap(Map.of("usernameRegex", "[Mm]ember")));
    assertThatStream(users.stream())
        .containsExactly(member);
    users = userService.findAll(defaultPageable(), createMultiValueMap(Map.of("usernameRegex", "ajklnskjdna")));
    assertThatStream(users.stream())
        .isEmpty();
  }

  @Test
  void findAll_emailRegex() {
    List<User> knownUsers = createAll();
    User member = knownUsers.get(0);
    User admin = knownUsers.get(4);

    Page<User> users = userService.findAll(defaultPageable(), createMultiValueMap(Map.of("emailRegex", "user")));
    // exclude superuser since it doesn't have an email
    assertThatStream(users.stream()).containsExactlyInAnyOrderElementsOf(knownUsers.subList(0, knownUsers.size() - 1));
    users = userService.findAll(defaultPageable(), createMultiValueMap(Map.of("emailRegex", "min")));
    assertThatStream(users.stream()).containsExactly(admin);
    users = userService.findAll(defaultPageable(), createMultiValueMap(Map.of("emailRegex", "[Mm]ember")));
    assertThatStream(users.stream()).containsExactly(member);
    users = userService.findAll(defaultPageable(), createMultiValueMap(Map.of("emailRegex", "ajklnskjdna")));
    assertThatStream(users.stream()).isEmpty();
  }

  @Test
  void findAll_hasEmail() {
    List<User> knownUsers = createAll();
    User superUser = knownUsers.get(5);
    Page<User> users = userService.findAll(defaultPageable(), createMultiValueMap(Map.of("hasEmail", "true")));
    assertThatStream(users.stream()).containsExactlyInAnyOrderElementsOf(knownUsers.subList(0, knownUsers.size() - 1));
    users = userService.findAll(defaultPageable(), createMultiValueMap(Map.of("hasEmail", "false")));
    assertThatStream(users.stream()).containsExactly(superUser);
  }

  @Test
  void findAll_role() {
    List<User> knownUsers = createAll();
    User member = knownUsers.get(0);
    User unverified = knownUsers.get(1);
    User editor = knownUsers.get(2);

    Page<User> users = userService.findAll(defaultPageable(), createMultiValueMap(Map.of("role", "MEMBER")));
    assertThatStream(users.stream()).containsExactly(member, unverified);
    users = userService.findAll(defaultPageable(), createMultiValueMap(Map.of("role", "EDITOR")));
    assertThatStream(users.stream()).containsExactly(editor);
  }

  @Test
  void findAll_invalidRoleThrows() {
    assertThrows(ServiceException.class, () -> userService.findAll(defaultPageable(), createMultiValueMap(Map.of("role", "akjsjkd"))));
    assertThrows(ServiceException.class, () -> userService.findAll(defaultPageable(), createMultiValueMap(Map.of("role", ""))));
  }

  @Test
  void findAll_verified() {
    List<User> knownUsers = createAll();
    User unverified = knownUsers.get(1);

    Page<User> users = userService.findAll(defaultPageable(), createMultiValueMap(Map.of("verified", "true")));
    assertThatStream(users.stream())
        .containsExactlyInAnyOrderElementsOf(knownUsers.stream().filter(user -> user != unverified).toList());
    users = userService.findAll(defaultPageable(), createMultiValueMap(Map.of("verified", "false")));
    assertThatStream(users.stream())
        .containsExactly(unverified);
  }

  @Test
  void findAll_unbannedAfterAndBefore() {
    createAll();
    User banned = createBanned(1);
    String before = DateAndTimeUtils.formatDateTime(LocalDateTime.now());
    String after = DateAndTimeUtils.formatDateTime(LocalDateTime.now().plusDays(2));
    Page<User> users = userService.findAll(defaultPageable(), createMultiValueMap(Map.of("unbannedAfter", before)));
    assertThatStream(users.stream()).containsExactly(banned);
    users = userService.findAll(defaultPageable(), createMultiValueMap(Map.of("unbannedAfter", after)));
    assertThatStream(users.stream()).isEmpty();
    users = userService.findAll(defaultPageable(), createMultiValueMap(Map.of("unbannedBefore", before)));
    assertThatStream(users.stream()).isEmpty();
    users = userService.findAll(defaultPageable(), createMultiValueMap(Map.of("unbannedBefore", after)));
    assertThatStream(users.stream()).containsExactly(banned);
  }

  @Test
  void findAll_banned() {
    List<User> notBanned = createAll();
    User banned = createBanned(1);
    Page<User> users = userService.findAll(defaultPageable(), createMultiValueMap(Map.of("banStatus", "banned")));
    assertThatStream(users.stream()).containsExactly(banned);
    users = userService.findAll(defaultPageable(), createMultiValueMap(Map.of("banStatus", "notbanned")));
    assertThatStream(users.stream()).containsExactlyInAnyOrderElementsOf(notBanned);
    users = userService.findAll(defaultPageable(), createMultiValueMap(Map.of("banStatus", "tempbanned")));
    assertThatStream(users.stream()).containsExactly(banned);
    users = userService.findAll(defaultPageable(), createMultiValueMap(Map.of("banStatus", "permabanned")));
    assertThatStream(users.stream()).isEmpty();
  }

  @Test
  void findAll_permabanned() {
    List<User> notBanned = createAll();
    User permaBanned = createPermabanned();
    Page<User> users = userService.findAll(defaultPageable(), createMultiValueMap(Map.of("banStatus", "banned")));
    assertThatStream(users.stream()).containsExactly(permaBanned);
    users = userService.findAll(defaultPageable(), createMultiValueMap(Map.of("banStatus", "notbanned")));
    assertThatStream(users.stream()).containsExactlyInAnyOrderElementsOf(notBanned);
    users = userService.findAll(defaultPageable(), createMultiValueMap(Map.of("banStatus", "tempbanned")));
    assertThatStream(users.stream()).isEmpty();
    users = userService.findAll(defaultPageable(), createMultiValueMap(Map.of("banStatus", "permabanned")));
    assertThatStream(users.stream()).containsExactly(permaBanned);
  }

  @Test
  void findAll_modifiedAfterAndBefore() {
    List<User> knownUsers = createAll();
    User member = knownUsers.get(0);
    member.lastModified = LocalDateTime.now();
    userRepo.save(member);
    String before = DateAndTimeUtils.formatDateTime(LocalDateTime.now().minusDays(1));
    String after = DateAndTimeUtils.formatDateTime(LocalDateTime.now().plusDays(1));

    Page<User> users = userService.findAll(defaultPageable(), createMultiValueMap(Map.of("modifiedAfter", before)));
    assertThatStream(users.stream()).containsExactly(member);
    users = userService.findAll(defaultPageable(), createMultiValueMap(Map.of("modifiedAfter", after)));
    assertThatStream(users.stream()).isEmpty();
    users = userService.findAll(defaultPageable(), createMultiValueMap(Map.of("modifiedBefore", before)));
    assertThatStream(users.stream()).isEmpty();
    users = userService.findAll(defaultPageable(), createMultiValueMap(Map.of("modifiedBefore", after)));
    assertThatStream(users.stream()).containsExactly(member);
  }

  @Test
  void isUsernameAvailable_trueIfAvailable() {
    createMember();

    assertThat(userService.isUsernameAvailable("kjasdjkbasd")).isTrue();
    assertThat(userService.isUsernameAvailable("user_Member")).isTrue();
    assertThat(userService.isUsernameAvailable("")).isTrue();
  }

  @Test
  void isUsernameAvailable_falseIfNotAvailable() {
    User member = createMember();
    User admin = createAdmin();

    assertThat(userService.isUsernameAvailable(member.username)).isFalse();
    assertThat(userService.isUsernameAvailable(member.username.toUpperCase())).isFalse();
    assertThat(userService.isUsernameAvailable(admin.username)).isFalse();
    assertThat(userService.isUsernameAvailable(admin.username.toUpperCase())).isFalse();
  }

  @Test
  void isEmailAvailable_trueIfAvailable() {
    createMember();

    assertThat(userService.isEmailAvailable("alkslknjdsad@example.com")).isTrue();
    assertThat(userService.isEmailAvailable("")).isTrue();
    assertThat(userService.isEmailAvailable("user_member@example.com")).isTrue();
  }

  @Test
  void isEmailAvailable_falseIfNotAvailable() {
    User member = createMember();
    User unverified = createUnverified();

    assertThat(userService.isEmailAvailable(member.email)).isFalse();
    assertThat(userService.isEmailAvailable(member.email.toUpperCase())).isFalse();
    assertThat(userService.isEmailAvailable(unverified.email)).isFalse();
    assertThat(userService.isEmailAvailable(unverified.email.toUpperCase())).isFalse();
  }

  @Test
  void create_successIfValidArguments() {
    User user = userService.create("user1", "user1@example.com", "password123", Role.MEMBER, false);
    assertThat(userService.findById(user.id)).contains(user);

    user = userService.create("user_2-._ABCdefgh123", "user2@example.com", "password456", Role.EDITOR, true);
    assertThat(userService.findById(user.id)).contains(user);

    user = userService.create("user3", "user3@example.com", "123456", Role.MEMBER, false);
    assertThat(userService.findById(user.id)).contains(user);

    user = userService.create("user4", "user4@example.com", "jKASBJIKPD*&@&*EYBDYOASDASD*&G\u30A1\u30A2", Role.MEMBER, false);
    assertThat(userService.findById(user.id)).contains(user);
  }

  @Test
  void create_invalidUsernameThrows() {
    assertThrows(ServiceException.class, () -> userService.create("a", "user1@example.com", "password123", Role.MEMBER, false));
    assertThrows(ServiceException.class, () -> userService.create("", "user1@example.com", "password123", Role.MEMBER, false));
    assertThrows(ServiceException.class, () -> userService.create("user$%1", "user1@example.com", "password123", Role.MEMBER, false));
    assertThrows(ServiceException.class, () -> userService.create("user11111111111111111", "user1@example.com", "password123", Role.MEMBER, false));
  }

  @Test
  void create_takenUsernameThrows() {
    createMember();
    createAdmin();
    assertThrows(ServiceException.class, () -> userService.create("userMember", "user1@example.com", "password123", Role.MEMBER, false));
    assertThrows(ServiceException.class, () -> userService.create("UserMember", "user1@example.com", "password123", Role.MEMBER, false));
    assertThrows(ServiceException.class, () -> userService.create("useradmin", "user1@example.com", "password123", Role.MEMBER, false));
    assertThrows(ServiceException.class, () -> userService.create("UserAdmin", "user1@example.com", "password123", Role.MEMBER, false));
  }

  @Test
  void create_invalidEmailThrows() {
    assertThrows(ServiceException.class, () -> userService.create("user1", "user1", "password123", Role.MEMBER, false));
    assertThrows(ServiceException.class, () -> userService.create("user1", "", "password123", Role.MEMBER, false));
    assertThrows(ServiceException.class, () -> userService.create("user1", "user1@example", "password123", Role.MEMBER, false));
    assertThrows(ServiceException.class, () -> userService.create("user1", "user1@example.c", "password123", Role.MEMBER, false));
  }

  @Test
  void create_takenEmailThrows() {
    createMember();
    createAdmin();
    assertThrows(ServiceException.class, () -> userService.create("user1", "userMember@example.com", "password123", Role.MEMBER, false));
    assertThrows(ServiceException.class, () -> userService.create("user1", "UserMember@example.com", "password123", Role.MEMBER, false));
    assertThrows(ServiceException.class, () -> userService.create("user1", "userADMIN@example.com", "password123", Role.MEMBER, false));
    assertThrows(ServiceException.class, () -> userService.create("user1", "UserAdmin@example.com", "password123", Role.MEMBER, false));
  }

  @Test
  void create_invalidPasswordThrows() {
    assertThrows(ServiceException.class, () -> userService.create("user1", "user1@example.com", "12345", Role.MEMBER, false));
    assertThrows(ServiceException.class, () -> userService.create("user1", "user1@example.com", "", Role.MEMBER, false));
  }

  @Test
  void create_memberIfNullRole() {
    User user = userService.create("user1", "user1@example.com", "password123", null, false);
    assertThat(userService.findById(user.id)).contains(user);
    assertThat(user.role).isEqualTo(Role.MEMBER);
  }

  // TODO Add more tests

}
