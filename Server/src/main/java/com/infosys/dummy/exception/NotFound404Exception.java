package com.infosys.dummy.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 9/30/18
 * <p>Time: 5:53 PM
 *
 * @author Miguel Mu\u00f1oz
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFound404Exception extends ResponseException {

	public NotFound404Exception(final String message) {
		super(message);
	}

	/**
	 * Construct a NotFound404Exception from an entity class and an ID, with a descriptive message containing both.
	 * @param entityClass The entity class that was not found
	 * @param id The id of the missing entity.
	 */
	public NotFound404Exception(Class<?> entityClass, Object id) {
		super(String.format("Not found: Entity with ID = %s of %s", id, entityClass.getSimpleName()));
	}

	public NotFound404Exception(final String message, final Throwable cause) {
		super(message, cause);
	}
}
