package me.whizvox.gameshelf.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;

import java.util.Objects;
import java.util.Optional;

public class ApiResponse {

  @JsonIgnore
  public HttpStatusCode actualStatus;

  public int status;
  public Object error;

  public ApiResponse(HttpStatusCode status, Object error) {
    actualStatus = status;
    this.status = status.value();
    this.error = error;
  }

  public static ResponseEntity<Object> create(ApiResponse response) {
    return new ResponseEntity<>(response, response.actualStatus);
  }

  public static ResponseEntity<Object> create(HttpStatus status, Object data) {
    if (data == null) {
      return create(new ApiResponse(status, status.isError()));
    } else if (data instanceof Optional<?> op) {
      data = op.orElse(null);
    }
    return create(new DataResponse(status, data));
  }

  public static ResponseEntity<Object> ok(Object data) {
    return create(HttpStatus.OK, data);
  }

  public static ResponseEntity<Object> ok() {
    return ok(null);
  }

  public static ResponseEntity<Object> created(Object data) {
    return create(HttpStatus.CREATED, data);
  }

  public static ResponseEntity<Object> created() {
    return created(null);
  }

  public static ResponseEntity<Object> badRequest(String message, Object details) {
    return create(new ErrorResponse(HttpStatus.BAD_REQUEST, message, details));
  }

  public static ResponseEntity<Object> unauthorized() {
    return create(new ErrorResponse(HttpStatus.UNAUTHORIZED, "Unauthorized", null));
  }

  public static ResponseEntity<Object> forbidden(@Nullable String message, @Nullable Object details) {
    return create(new ErrorResponse(HttpStatus.FORBIDDEN, Objects.requireNonNullElse(message, "Unauthorized"), details));
  }

  public static ResponseEntity<Object> internalServerError(String message, Object details) {
    return create(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, message, details));
  }

}
