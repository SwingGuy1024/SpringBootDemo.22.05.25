package com.infosys.dummy.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 1/11/21
 * <p>Time: 10:42 PM
 *
 * @author Miguel Mu\u00f1oz
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class NotAuthenticated401Exception extends ResponseException {
  public NotAuthenticated401Exception(final String message) {
    super(message);
  }

  public NotAuthenticated401Exception(final String message, final Throwable cause) {
    super(message, cause);
  }
}
