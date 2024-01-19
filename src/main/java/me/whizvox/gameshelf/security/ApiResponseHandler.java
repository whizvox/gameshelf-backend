package me.whizvox.gameshelf.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import me.whizvox.gameshelf.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.util.MimeTypeUtils;

import java.io.IOException;

public class ApiResponseHandler {

  private final ObjectMapper objectMapper;
  private final ApiResponse response;

  public ApiResponseHandler(ObjectMapper objectMapper, ApiResponse response) {
    this.objectMapper = objectMapper;
    this.response = response;
  }

  public ApiResponseHandler(HttpStatus status, ObjectMapper objectMapper) {
    this(objectMapper, new ApiResponse(status, status.isError()));
  }

  public void handle(HttpServletResponse response) throws IOException {
    response.setStatus(this.response.status);
    response.setContentType(MimeTypeUtils.APPLICATION_JSON_VALUE);
    objectMapper.writeValue(response.getWriter(), this.response);
  }

}
