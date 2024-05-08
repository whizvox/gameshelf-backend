package me.whizvox.gameshelf.platform;

import me.whizvox.gameshelf.media.GenericMedia;
import me.whizvox.gameshelf.response.ApiResponse;
import me.whizvox.gameshelf.response.PagedData;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("${apiPrefix}/platform")
public class PlatformController {

  private final PlatformService platformService;

  @Autowired
  public PlatformController(PlatformService platformService) {
    this.platformService = platformService;
  }

  @GetMapping
  public ResponseEntity<Object> get(@RequestParam(required = false) ObjectId id,
                                    @RequestParam(required = false) String shortName,
                                    @PageableDefault(size = 20) Pageable pageable) {
    if (id != null) {
      return ApiResponse.ok(platformService.findById(id));
    } else if (shortName != null) {
      return ApiResponse.ok(platformService.findByShortName(shortName));
    } else {
      Page<Platform> platforms = platformService.findAll(pageable);
      PagedData data = new PagedData(platforms);
      return ApiResponse.ok(data);
    }
  }

  @GetMapping("/available")
  public ResponseEntity<Object> isShortNameAvailable(@RequestParam String shortName) {
    return ApiResponse.ok(platformService.isShortNameAvailable(shortName));
  }

  @PostMapping
  public ResponseEntity<Object> create(@RequestParam String shortName,
                                       @RequestParam String name,
                                       @RequestParam(required = false) String description,
                                       @RequestParam(required = false) String image,
                                       @RequestParam(required = false) LocalDate releaseDate,
                                       @RequestParam(required = false) List<GenericMedia> media) {
    Platform platform = platformService.create(shortName, name, description, image, releaseDate, media);
    return ApiResponse.ok(platform);
  }

  @PutMapping
  public ResponseEntity<Object> update(@RequestParam ObjectId id,
                                       @RequestParam MultiValueMap<String, String> args) {
    Platform platform = platformService.update(id, args);
    return ApiResponse.ok(platform);
  }

  @DeleteMapping
  public ResponseEntity<Object> delete(@RequestParam ObjectId id) {
    platformService.delete(id);
    return ApiResponse.ok();
  }

}
