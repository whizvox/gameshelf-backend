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

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("${apiPrefix}/ratingsystem")
public class RatingSystemController {

  private final RatingService ratingService;

  @Autowired
  public RatingSystemController(RatingService ratingService) {
    this.ratingService = ratingService;
  }

  @GetMapping
  public ResponseEntity<Object> get(@RequestParam(required = false) ObjectId id,
                                    @RequestParam(required = false) String shortName,
                                    @PageableDefault Pageable pageable) {
    if (id != null) {
      return ApiResponse.ok(ratingService.findSystemById(id));
    } else if (shortName != null) {
      return ApiResponse.ok(ratingService.findSystemByShortName(shortName));
    }
    return ApiResponse.ok(new PagedData(ratingService.findAllSystems(pageable)));
  }

  @GetMapping("/available")
  public ResponseEntity<Object> isShortNameAvailable(@RequestParam String shortName) {
    return ApiResponse.ok(ratingService.isSystemShortNameAvailable(shortName));
  }

  @PostMapping
  public ResponseEntity<Object> create(@RequestParam String shortName,
                                       @RequestParam String name,
                                       @RequestParam LocalDate founded,
                                       @RequestParam(required = false) String description,
                                       @RequestParam(required = false) List<String> regions,
                                       @RequestParam(required = false) String logo) {
    RatingSystem rs = ratingService.createSystem(shortName, founded, name, description, regions, logo);
    return ApiResponse.ok(rs);
  }

  @PutMapping
  public ResponseEntity<Object> update(@RequestParam ObjectId id,
                                       @RequestParam MultiValueMap<String, String> args) {
    RatingSystem rs = ratingService.updateSystem(id, args);
    return ApiResponse.ok(rs);
  }

  @DeleteMapping
  public ResponseEntity<Object> delete(@RequestParam ObjectId id) {
    ratingService.deleteSystem(id);
    return ApiResponse.ok();
  }

}
