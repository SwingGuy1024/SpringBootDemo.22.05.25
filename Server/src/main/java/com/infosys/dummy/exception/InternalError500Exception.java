package com.infosys.dummy.exception;

import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class InternalError500Exception extends ResponseException {
    public InternalError500Exception(RuntimeException runtimeException) {
        super(runtimeException.getMessage(), runtimeException);
    }
}
