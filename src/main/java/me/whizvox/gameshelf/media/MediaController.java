package me.whizvox.gameshelf.media;

import me.whizvox.gameshelf.exception.ServiceException;
import me.whizvox.gameshelf.response.ApiResponse;
import me.whizvox.gameshelf.response.PagedData;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("${apiPrefix}/media")
public class MediaController {

  private final MediaService mediaService;

  @Autowired
  public MediaController(MediaService mediaService) {
    this.mediaService = mediaService;
  }

  @GetMapping("/search")
  public ResponseEntity<Object> search(@RequestParam MultiValueMap<String, String> args,
                                       @PageableDefault Pageable pageable) {
    return ApiResponse.ok(new PagedData(mediaService.findAll(pageable, args)));
  }

  @GetMapping("info/{id}")
  public ResponseEntity<Object> getInfo(@PathVariable ObjectId id) {
    return ApiResponse.ok(mediaService.findById(id));
  }

  @GetMapping("{id}")
  public ResponseEntity<Object> getResource(@PathVariable ObjectId id,
                                            // small optimization in case we already called GET info/{id}
                                            @RequestParam(required = false) String mimeType) {
    if (mimeType == null) {
      Media media = mediaService.findById(id).orElseThrow(() -> ServiceException.notFound("Could not find media with ID " + id));
      mimeType = media.mimeType;
    }
    byte[] data;
    try {
      data = StreamUtils.copyToByteArray(mediaService.openStream(id));
    } catch (IOException e) {
      throw ServiceException.internalServerError("Could not copy media bytes", e);
    }
    return ResponseEntity
        .ok()
        .contentType(MediaType.parseMediaType(mimeType))
        .contentLength(data.length)
        .body(new ByteArrayResource(data));
  }

  @PostMapping
  public ResponseEntity<Object> upload(@RequestParam MultipartFile file,
                                       @RequestParam(required = false) String altText) {
    try {
      return ApiResponse.ok(mediaService.create(file.getInputStream(), file.getSize(), file.getContentType(), altText));
    } catch (IOException e) {
      throw ServiceException.internalServerError("Could not access file stream", e);
    }
  }

  @PutMapping
  public ResponseEntity<Object> update(@RequestParam ObjectId id,
                                       @RequestParam(required = false) MultipartFile file,
                                       @RequestParam MultiValueMap<String, String> args) {
    return ApiResponse.ok(mediaService.update(id, file, args));
  }

  @DeleteMapping("{id}")
  public ResponseEntity<Object> delete(@PathVariable ObjectId id) {
    mediaService.delete(id);
    return ApiResponse.ok();
  }

}
