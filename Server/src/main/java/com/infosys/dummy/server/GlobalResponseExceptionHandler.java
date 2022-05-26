package com.infosys.dummy.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infosys.dummy.exception.ResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 2/15/21
 * <p>Time: 8:12 AM
 *
 * @author Miguel Mu\u00f1oz
 */
@ControllerAdvice
public class GlobalResponseExceptionHandler
    extends ResponseEntityExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalResponseExceptionHandler.class);

  @ExceptionHandler(ResponseException.class)
  protected ResponseEntity<Object> handleConflict(RuntimeException ex, WebRequest request) {
    ResponseException responseException = (ResponseException) ex;
    ResponseBody responseBody = new ResponseBody(responseException, request);
    log.debug(responseBody.toString());
    return handleExceptionInternal(ex, responseBody.toString(), new HttpHeaders(), responseException.getHttpStatus(), request);
  }

  @SuppressWarnings("unused")
  private static class ResponseBody {
    private final String timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;
    private static final ZoneId utcZone = ZoneId.of("Etc/Universal");

    ResponseBody(ResponseException responseException, WebRequest webRequest) {
      this.message = responseException.getMessage();
      this.status = responseException.getStatusCode();
      this.error = responseException.getErrorName();
      this.timestamp = formatter.format(OffsetDateTime.now(utcZone));
      this.path = getPath(webRequest);
    }

    private static String getPath(final WebRequest webRequest) {
      NativeWebRequest nativeWebRequest = (NativeWebRequest) webRequest;
      final HttpServletRequest httpServletRequest = nativeWebRequest.getNativeRequest(HttpServletRequest.class);
      if (httpServletRequest != null) {
        return httpServletRequest.getRequestURI();
      }
      //noinspection HardCodedStringLiteral
      return "(unknown)"; // Shouldn't happen
    }

    public String getTimestamp() {
      return timestamp;
    }

    public int getStatus() {
      return status;
    }

    public String getError() {
      return error;
    }

    public String getMessage() {
      return message;
    }

    public String getPath() {
      return path;
    }

    @Override
    public String toString() {
      try {
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
      } catch (JsonProcessingException e) {
        //noinspection ProhibitedExceptionThrown
        throw new RuntimeException(e); // Shouldn't happen
      }
    }
  }
}
