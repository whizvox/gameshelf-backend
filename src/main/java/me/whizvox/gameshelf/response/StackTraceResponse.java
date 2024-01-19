package me.whizvox.gameshelf.response;

import org.springframework.http.HttpStatusCode;
import org.springframework.lang.Nullable;

import java.io.PrintWriter;
import java.io.StringWriter;

public class StackTraceResponse extends ErrorResponse {

  public String stackTrace;

  public StackTraceResponse(HttpStatusCode status, String message, Object details, String stackTrace) {
    super(status, message, details);
    this.stackTrace = stackTrace;
  }

  public StackTraceResponse(HttpStatusCode status, String message, @Nullable Object details, @Nullable Throwable cause) {
    super(status, message, details);
    if (cause == null) {
      stackTrace = null;
    } else {
      StringWriter writer = new StringWriter();
      cause.printStackTrace(new PrintWriter(writer));
      stackTrace = writer.toString();
    }
  }

}
