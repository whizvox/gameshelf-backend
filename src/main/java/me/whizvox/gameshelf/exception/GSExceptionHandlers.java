package me.whizvox.gameshelf.exception;

import me.whizvox.gameshelf.response.ApiResponse;
import me.whizvox.gameshelf.response.ErrorResponse;
import me.whizvox.gameshelf.response.StackTraceResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GSExceptionHandlers extends ResponseEntityExceptionHandler {

  private final boolean showStackTrace;

  public GSExceptionHandlers(@Value("${gameshelf.response.exception.showStackTrace:false}") boolean showStackTrace) {
    this.showStackTrace = showStackTrace;
  }

  @ExceptionHandler(value = ServiceException.class)
  public ResponseEntity<Object> handleServiceException(ServiceException e) {
    if (e.status.isError()) {
      if (showStackTrace) {
        return ApiResponse.create(new StackTraceResponse(e.status, e.getMessage(), e.details, e));
      }
      return ApiResponse.create(new ErrorResponse(e.status, e.getMessage(), e.details));
    }
    return ApiResponse.internalServerError("Unexpected service exception", e);
  }

}
