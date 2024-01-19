package me.whizvox.gameshelf.game;

import me.whizvox.gameshelf.exception.ServiceException;
import me.whizvox.gameshelf.media.GenericMedia;
import me.whizvox.gameshelf.response.ApiResponse;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("${apiPrefix}/release")
public class ReleaseController {

  private final GameService gameService;

  @Autowired
  public ReleaseController(GameService gameService) {
    this.gameService = gameService;
  }

  @GetMapping
  public ResponseEntity<Object> get(@RequestParam(required = false) ObjectId id,
                                    @RequestParam(defaultValue = "false") boolean inherit,
                                    @RequestParam(required = false) ObjectId game) {
    if (id != null) {
      return ApiResponse.ok(gameService.findReleaseById(id, inherit));
    } else if (game != null) {
      return ApiResponse.ok(gameService.findReleasesByGame(game));
    }
    throw ServiceException.badRequest("\"id\" or \"game\" must be defined");
  }

  @PostMapping
  public ResponseEntity<Object> create(@RequestParam ObjectId game,
                                       @RequestParam(required = false) String name,
                                       @RequestParam(required = false) String description,
                                       @RequestParam(required = false) LocalDate releaseDate,
                                       @RequestParam(required = false) List<String> aliases,
                                       @RequestParam(required = false) List<String> languages,
                                       @RequestParam(required = false) List<ObjectId> platforms,
                                       @RequestParam(required = false) List<ObjectId> ratings,
                                       @RequestParam(required = false) List<GenericMedia> media,
                                       @RequestParam(required = false) ObjectId boxArt) {
    return ApiResponse.ok(gameService.addRelease(game, name, description, releaseDate, aliases, languages, platforms, ratings, media, boxArt));
  }

  @PutMapping
  public ResponseEntity<Object> update(@RequestParam ObjectId id,
                                       @RequestParam MultiValueMap<String, String> args) {
    return ApiResponse.ok(gameService.updateRelease(id, args));
  }

  @DeleteMapping
  public ResponseEntity<Object> delete(@RequestParam ObjectId id) {
    gameService.deleteRelease(id);
    return ApiResponse.ok();
  }

}
