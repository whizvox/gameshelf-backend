package me.whizvox.gameshelf.rating;

import me.whizvox.gameshelf.response.ApiResponse;
import me.whizvox.gameshelf.response.PagedData;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${apiPrefix}/rating")
public class RatingController {

  private final RatingService ratingService;

  @Autowired
  public RatingController(RatingService ratingService) {
    this.ratingService = ratingService;
  }

  @GetMapping
  public ResponseEntity<Object> get(@RequestParam(required = false) ObjectId id,
                                    @RequestParam(required = false) ObjectId system,
                                    @PageableDefault Pageable pageable) {
    if (id != null) {
      return ApiResponse.ok(ratingService.findById(id));
    } else if (system != null) {
      return ApiResponse.ok(ratingService.findBySystem(system));
    }
    return ApiResponse.ok(new PagedData(ratingService.search(pageable)));
  }

  @PostMapping
  public ResponseEntity<Object> create(@RequestParam String shortName,
                                       @RequestParam ObjectId system,
                                       @RequestParam(required = false) String name,
                                       @RequestParam(required = false) String description,
                                       @RequestParam(required = false) List<String> logos,
                                       @RequestParam(required = false) Boolean explicit) {
    return ApiResponse.ok(ratingService.create(shortName, system, name, description, logos, explicit));
  }

  @PutMapping
  public ResponseEntity<Object> update(@RequestParam ObjectId id,
                                       @RequestParam MultiValueMap<String, String> args) {
    return ApiResponse.ok(ratingService.update(id, args));
  }

  @DeleteMapping
  public ResponseEntity<Object> delete(@RequestParam ObjectId id) {
    ratingService.delete(id);
    return ApiResponse.ok();
  }

}
