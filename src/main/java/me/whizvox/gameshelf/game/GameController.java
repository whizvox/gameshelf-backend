package me.whizvox.gameshelf.game;

import me.whizvox.gameshelf.media.GenericMedia;
import me.whizvox.gameshelf.response.ApiResponse;
import me.whizvox.gameshelf.response.PagedData;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("${apiPrefix}/game")
public class GameController {

  private final GameService gameService;

  @Autowired
  public GameController(GameService gameService) {
    this.gameService = gameService;
  }

  @GetMapping
  public ResponseEntity<Object> get(@RequestParam(required = false) ObjectId id,
                                    @PageableDefault Pageable pageable) {
    if (id != null) {
      return ApiResponse.ok(gameService.findById(id));
    }
    return ApiResponse.ok(new PagedData(gameService.findAll(pageable)));
  }

  @PostMapping
  public ResponseEntity<Object> create(@RequestParam String name,
                                       @RequestParam(required = false) String description,
                                       @RequestParam(required = false) LocalDate releaseDate,
                                       @RequestParam(required = false) List<String> aliases,
                                       @RequestParam(required = false) List<String> languages,
                                       @RequestParam(required = false) List<ObjectId> platforms,
                                       @RequestParam(required = false) List<ObjectId> ratings,
                                       @RequestParam(required = false) List<GenericMedia> media,
                                       @RequestParam(required = false) ObjectId boxArt,
                                       @RequestParam(required = false) List<GameRelation> relations) {
    return ApiResponse.ok(gameService.create(name, description, releaseDate, aliases, languages, platforms, ratings, media, boxArt, relations));
  }

  @PutMapping
  public ResponseEntity<Object> update(@RequestParam ObjectId id,
                                       @RequestParam MultiValueMap<String, String> args) {
    return ApiResponse.ok(gameService.update(id, args));
  }

  @DeleteMapping
  public ResponseEntity<Object> delete(@RequestParam ObjectId id,
                                       @RequestParam(defaultValue = "false") boolean includeReleases) {
    gameService.delete(id, includeReleases);
    return ApiResponse.ok();
  }

}
