package me.whizvox.gameshelf.response;

import org.springframework.http.HttpStatusCode;

public class ErrorResponse extends ApiResponse {

  public Object details;

  public ErrorResponse(HttpStatusCode status, String message, Object details) {
    super(status, message);
    this.details = details;
  }

}
