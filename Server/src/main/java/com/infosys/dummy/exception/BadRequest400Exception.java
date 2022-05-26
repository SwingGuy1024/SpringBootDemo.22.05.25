package com.infosys.dummy.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 9/30/18
 * <p>Time: 6:08 PM
 *
 * @author Miguel Mu\u00f1oz
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequest400Exception extends ResponseException {

	public BadRequest400Exception(String message) {
		super(message);
	}
	
	public BadRequest400Exception(String message, Throwable cause) {
		super(message, cause);
	}

	public BadRequest400Exception(Throwable t) {
		super(t.getMessage(), t);
	}
}
