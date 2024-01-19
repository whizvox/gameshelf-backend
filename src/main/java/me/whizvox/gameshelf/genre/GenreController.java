package me.whizvox.gameshelf.genre;

import me.whizvox.gameshelf.response.ApiResponse;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${apiPrefix}/genre")
public class GenreController {

  private final GenreService genreService;

  @Autowired
  public GenreController(GenreService genreService) {
    this.genreService = genreService;
  }

  @GetMapping
  public ResponseEntity<Object> get(@RequestParam(required = false) ObjectId id,
                                    @RequestParam(required = false) String name,
                                    @RequestParam(defaultValue = "false") boolean includeExplicit) {
    if (id != null) {
      return ApiResponse.ok(genreService.findById(id));
    } else if (name != null) {
      return ApiResponse.ok(genreService.findByName(name));
    }
    if (includeExplicit) {
      return ApiResponse.ok(genreService.findAll());
    }
    return ApiResponse.ok(genreService.findAllNonExplicit());
  }

  @PostMapping
  public ResponseEntity<Object> create(@RequestParam String name,
                                       @RequestParam(required = false) String description,
                                       @RequestParam(required = false) Boolean explicit) {
    return ApiResponse.ok(genreService.create(name, description, explicit));
  }

  @PutMapping
  public ResponseEntity<Object> update(@RequestParam ObjectId id,
                                       @RequestParam MultiValueMap<String, String> args) {
    return ApiResponse.ok(genreService.update(id, args));
  }

  @DeleteMapping
  public ResponseEntity<Object> delete(@RequestParam ObjectId id) {
    genreService.delete(id);
    return ApiResponse.ok();
  }

}
