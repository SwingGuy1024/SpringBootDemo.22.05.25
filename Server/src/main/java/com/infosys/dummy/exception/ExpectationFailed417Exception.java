package com.infosys.dummy.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * <p>Expectation Failed is the closest HttpStatus message I could come up with to mean the token has timed out. There 
 * is no Timed-out or Expired message.</p>
 *
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 1/31/21
 * <p>Time: 1:12 AM
 *
 * @author Miguel Muñoz
 */
@ResponseStatus(HttpStatus.EXPECTATION_FAILED)
public class ExpectationFailed417Exception extends ResponseException {
  public ExpectationFailed417Exception(final String message, final Throwable cause) {
    super(message, cause);
  }
}
