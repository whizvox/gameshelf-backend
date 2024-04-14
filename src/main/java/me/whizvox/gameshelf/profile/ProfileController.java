package me.whizvox.gameshelf.profile;

import me.whizvox.gameshelf.exception.ServiceException;
import me.whizvox.gameshelf.response.ApiResponse;
import me.whizvox.gameshelf.user.User;
import me.whizvox.gameshelf.util.ErrorTypes;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequestMapping("${apiPrefix}/profile")
public class ProfileController {

  private final ProfileService profileService;

  @Autowired
  public ProfileController(ProfileService profileService) {
    this.profileService = profileService;
  }

  @GetMapping
  public ResponseEntity<Object> get(@RequestParam(required = false) ObjectId id,
                                    @RequestParam(required = false) String username) {
    if (id != null) {
      return ApiResponse.ok(profileService.findById(id));
    }
    if (username == null) {
      throw ServiceException.error(ErrorTypes.MISSING_PARAMETER, "Must specify 'id' or 'username'");
    }
    return ApiResponse.ok(profileService.findByUsername(username));
  }

  @GetMapping("self")
  public ResponseEntity<Object> getSelf(@AuthenticationPrincipal User user) {
    return ApiResponse.ok(user == null ? Optional.empty() : profileService.findById(user.id));
  }

  @PutMapping
  public ResponseEntity<Object> update(@RequestParam ObjectId id,
                                       @RequestParam MultiValueMap<String, String> args) {
    return ApiResponse.ok(profileService.update(id, args));
  }

  @PutMapping("self")
  public ResponseEntity<Object> updateSelf(@AuthenticationPrincipal User user,
                                           @RequestParam MultiValueMap<String, String> args) {
    return ApiResponse.ok(profileService.update(user.id, args));
  }

}
