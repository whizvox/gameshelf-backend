package me.whizvox.gameshelf.gamelist;

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
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("gamelist")
public class GameListController {

  private final GameListService listService;

  @Autowired
  public GameListController(GameListService listService) {
    this.listService = listService;
  }

  @GetMapping
  public ResponseEntity<Object> get(@RequestParam(required = false) ObjectId id,
                                    @RequestParam(required = false) ObjectId user,
                                    @RequestParam(required = false) GameStatus status) {
    if (id != null) {
      return ApiResponse.ok(listService.findById(id));
    }
    if (user == null) {
      throw ServiceException.error(ErrorTypes.MISSING_PARAMETER, "Missing 'user' parameter");
    }
    if (status == null) {
      return ApiResponse.ok(listService.findByUser(user));
    }
    return ApiResponse.ok(listService.findByUserAndStatus(user, status));
  }

  @PostMapping
  public ResponseEntity<Object> create(@AuthenticationPrincipal User user,
                                       @RequestParam ObjectId game,
                                       @RequestParam GameStatus status,
                                       @RequestParam(required = false) ObjectId release,
                                       @RequestParam(defaultValue = "0") int rating,
                                       @RequestParam(required = false) String comment,
                                       @RequestParam(required = false) LocalDate startDate,
                                       @RequestParam(required = false) LocalDate endDate,
                                       @RequestParam(defaultValue = "0") int hoursPlayed) {
    return ApiResponse.created(
        listService.create(user, game, status, release, rating, comment, startDate, endDate, hoursPlayed)
    );
  }

  @PutMapping
  public ResponseEntity<Object> update(@AuthenticationPrincipal User user,
                                       @RequestParam ObjectId id,
                                       @RequestParam MultiValueMap<String, String> args) {
    return ApiResponse.ok(listService.update(user, id, args));
  }

  @DeleteMapping("{id}")
  public ResponseEntity<Object> delete(@PathVariable ObjectId id) {
    listService.delete(id);
    return ApiResponse.ok();
  }

}
