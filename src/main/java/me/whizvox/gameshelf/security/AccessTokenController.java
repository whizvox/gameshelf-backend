package me.whizvox.gameshelf.security;

import me.whizvox.gameshelf.response.ApiResponse;
import me.whizvox.gameshelf.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AccessTokenController {

  private final UserService userService;

  @Autowired
  public AccessTokenController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping("${apiPrefix}/accesstoken")
  public ResponseEntity<Object> requestAccessToken(@RequestParam String username,
                                                   @RequestParam String password,
                                                   @RequestParam(defaultValue = "false") boolean rememberMe) {
    AccessToken accessToken = userService.generateAccessToken(username, password, rememberMe);
    return ApiResponse.ok(accessToken);
  }

}
