package me.whizvox.gameshelf.exception;

import me.whizvox.gameshelf.util.ErrorType;
import me.whizvox.gameshelf.util.ErrorTypes;
import org.springframework.http.HttpStatus;

public class ServiceException extends RuntimeException {

  public final HttpStatus status;
  public final Object details;

  public ServiceException(HttpStatus status, String message, Object details, Throwable cause) {
    super(message, cause);
    this.status = status;
    this.details = details;
  }

  public static ServiceException error(HttpStatus status, String message, Object details, Throwable cause) {
    return new ServiceException(status, message, details, cause);
  }

  public static ServiceException error(HttpStatus status, Object details, Throwable cause) {
    return error(status, status.getReasonPhrase(), details, cause);
  }

  public static ServiceException error(HttpStatus status, Object details) {
    return error(status, details, null);
  }

  public static ServiceException error(HttpStatus status) {
    return error(status, null);
  }

  public static ServiceException error(ErrorType type, Object details, Throwable cause) {
    return error(type.getStatus(), type.getErrorString(), details, cause);
  }

  public static ServiceException error(ErrorType type, Object details) {
    return error(type, details, null);
  }

  public static ServiceException error(ErrorType type) {
    return error(type, null);
  }

  public static ServiceException badRequest(Object details) {
    return error(ErrorTypes.BAD_REQUEST, details);
  }

  public static ServiceException unauthorized() {
    return error(ErrorTypes.UNAUTHORIZED);
  }

  public static ServiceException forbidden() {
    return error(ErrorTypes.FORBIDDEN);
  }

  public static ServiceException notFound(String message) {
    return error(ErrorTypes.NOT_FOUND, message);
  }

  public static ServiceException notFound() {
    return error(ErrorTypes.NOT_FOUND);
  }

  public static ServiceException conflict(String message) {
    return error(ErrorTypes.CONFLICT, message);
  }

  public static ServiceException conflict() {
    return error(ErrorTypes.CONFLICT);
  }

  public static ServiceException internalServerError(Object details, Throwable cause) {
    return error(ErrorTypes.INTERNAL_SERVER_ERROR, details, cause);
  }

  public static ServiceException internalServerError(Object details) {
    return internalServerError(details, null);
  }

}
