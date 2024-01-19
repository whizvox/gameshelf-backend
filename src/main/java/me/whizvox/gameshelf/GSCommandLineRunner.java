package me.whizvox.gameshelf;

import me.whizvox.gameshelf.user.Role;
import me.whizvox.gameshelf.user.UserService;
import me.whizvox.gameshelf.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import static me.whizvox.gameshelf.util.GSLog.LOGGER;

@Component
public class GSCommandLineRunner implements CommandLineRunner {

  private static final String
      ENABLE_SUPERUSER_OPTION = "--initsuperuser",
      SUPERUSER_USERNAME = "superuser";

  private final UserService userService;

  @Autowired
  public GSCommandLineRunner(UserService userService) {
    this.userService = userService;
  }

  @Override
  public void run(String... args) throws Exception {
    boolean createSuperUser = false;
    for (String arg : args) {
      if (arg.equals(ENABLE_SUPERUSER_OPTION)) {
        createSuperUser = true;
        break;
      }
    }
    if (createSuperUser) {
      if (userService.isUsernameAvailable(SUPERUSER_USERNAME)) {
        String password = StringUtils.createSecureRandomSequence(20);
        userService.create(SUPERUSER_USERNAME, null, password, Role.SUPERUSER, true);
        LOGGER.info("Created superuser account. Password: " + password);
      } else {
        LOGGER.debug("Superuser account already exists. Consider removing --initSuperUser from the arguments");
      }
    } else {
      LOGGER.debug("Skipping creation of super user account");
    }
  }

}
