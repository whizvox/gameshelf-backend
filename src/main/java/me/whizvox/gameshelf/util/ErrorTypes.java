package me.whizvox.gameshelf.util;

import org.springframework.http.HttpStatus;

public enum ErrorTypes implements ErrorType {

  // 400
  BAD_REQUEST,
  MISSING_PARAMETER,
  INVALID_USERNAME_OR_PASSWORD,
  USER_ALREADY_BANNED,
  USER_NOT_BANNED,
  ROLE_TOO_HIGH,
  CANNOT_CREATE_GUEST_USER,
  BIOGRAPHY_TOO_LONG,
  INVALID_BIRTHDAY,
  TOO_MANY_FAVORITE_GAMES,
  RELEASE_DOES_NOT_CORRESPOND,
  INVALID_GAME_AND_RELEASE,

  // 401
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED),
  ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED),

  // 403
  FORBIDDEN(HttpStatus.FORBIDDEN),
  CANNOT_BAN_SELF(HttpStatus.FORBIDDEN),
  TARGET_ROLE_HIGHER(HttpStatus.FORBIDDEN),

  // 404
  NOT_FOUND(HttpStatus.NOT_FOUND),

  // 409
  CONFLICT(HttpStatus.CONFLICT),

  // 500
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

  public final HttpStatus status;

  ErrorTypes(HttpStatus status) {
    this.status = status;
  }

  ErrorTypes() {
    this(HttpStatus.BAD_REQUEST);
  }

  @Override
  public HttpStatus getStatus() {
    return status;
  }

  @Override
  public String getErrorString() {
    return toString();
  }

}
