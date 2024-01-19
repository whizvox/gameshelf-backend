package me.whizvox.gameshelf.response;

import org.springframework.http.HttpStatusCode;

public class DataResponse extends ApiResponse {

  public Object data;

  public DataResponse(HttpStatusCode status, Object data) {
    super(status, false);
    this.data = data;
  }

}
