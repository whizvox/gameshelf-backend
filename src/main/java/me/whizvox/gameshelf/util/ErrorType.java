package me.whizvox.gameshelf.util;

import org.springframework.http.HttpStatus;

public interface ErrorType {

  String getErrorString();

  HttpStatus getStatus();

}
