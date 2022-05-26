package com.infosys.dummy.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 2/12/21
 * <p>Time: 7:32 AM
 *
 * @author Miguel Mu\u00f1oz
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class Conflict409Exception extends ResponseException {
  public Conflict409Exception(String message) { super(message); }
  public Conflict409Exception(String message, Throwable cause) { super(message, cause); }
}
