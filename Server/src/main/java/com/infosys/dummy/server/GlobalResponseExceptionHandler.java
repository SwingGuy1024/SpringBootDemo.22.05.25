package com.infosys.dummy.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infosys.dummy.exception.ResponseException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
@SuppressWarnings("unused") // Some of these get called only by the Spring framework, the IDE will think they're unused.
@ControllerAdvice
public class GlobalResponseExceptionHandler
    extends ResponseEntityExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalResponseExceptionHandler.class);
  private static final ObjectMapper mapper = new ObjectMapper();
  private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;

  private final ZoneId zoneId;

  @Autowired
  public GlobalResponseExceptionHandler(ApplicationContext applicationContext) {
    this.zoneId = getZone(applicationContext);
  }

  @NotNull
  private static ZoneId getZone(ApplicationContext context) {
    String zone = context.getEnvironment().getProperty("my-app.zoneId");
    if (zone != null) {
      try {
        return ZoneId.of(zone);
      } catch (RuntimeException e) {
        log.warn("Unknown Zone: {} Use a zoneId specified in {}", zone, ZoneId.class);
      }
    }
    return ZoneId.systemDefault();
  }

  /**
   * This method fills in a ResponseBody when a ResponseException is thrown. It also logs any caught exceptions.
   * It gets called by the SpringFramework.
   * @param ex The ResponseException
   * @param request The WebRequest
   * @return A ResponseEntity with a useful error message.
   */
  @ExceptionHandler(ResponseException.class)
  protected ResponseEntity<Object> handleConflict(RuntimeException ex, WebRequest request) {
    logStandardMessage(ex, request);
    ResponseException responseException = (ResponseException) ex;
    String body = new ResponseBody(responseException, request).toString();
    return handleExceptionInternal(ex, body, new HttpHeaders(), responseException.getHttpStatus(), request);
  }

  /**
   * This method gives me useful (but wordy) error messages when the Spring Server code rejects an invalid DTO before
   * it even reaches my code. Without this, these errors return no error message at all.
   * @param ex The exception
   * @param headers The headers
   * @param status The HttpStatus
   * @param request The WebRequest
   * @return A ResponseEntity with a useful error message
   */
  @Override
  protected @NotNull ResponseEntity<Object> handleMethodArgumentNotValid(
      @NotNull MethodArgumentNotValidException ex,
      @NotNull HttpHeaders headers,
      @NotNull HttpStatus status,
      @NotNull WebRequest request
  ) {
    logStandardMessage(ex, request);
    ResponseBody responseBody = new ResponseBody(ex, status, request);
    String bodyAsString = responseBody.toString();
    return new ResponseEntity<>(bodyAsString, headers, status);
  }

  private static String extractPath(final WebRequest webRequest) {
    NativeWebRequest nativeWebRequest = (NativeWebRequest) webRequest;
    final HttpServletRequest httpServletRequest = nativeWebRequest.getNativeRequest(HttpServletRequest.class);
    if (httpServletRequest != null) {
      return httpServletRequest.getRequestURI();
    }
    //noinspection HardCodedStringLiteral
    return "(unknown)"; // Shouldn't happen
  }

  /**
   * Log a standard error message including the request path and the error message.
   * @param throwable The Exception or Error
   * @param request The WebRequest, which holds the original request path
   */
  private static void logStandardMessage(Throwable throwable, WebRequest request) {
    // On ResponseException, and its status is < 500, it's not caused by a bug, so we just log the message and path.
    // If it's not, it's a bug, so we include a stack trace.
    if (throwable instanceof ResponseException && ((ResponseException)throwable).getHttpStatus().value() < 500) {
      if (log.isInfoEnabled()) {
        log.info("Error processing request at {}", extractPath(request));
        log.info(((ResponseException)throwable).getHttpStatus().toString());
        log.info(throwable.getMessage());
      }
    } else if (log.isErrorEnabled()) {
      log.error("Error processing request at {}:", extractPath(request), throwable);
    }
  }

  private class ResponseBody {
    private final String timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;

    ResponseBody(ResponseException responseException, WebRequest webRequest) {
      this(responseException, responseException.getHttpStatus(), webRequest);
    }

    ResponseBody(Throwable throwable, HttpStatus httpStatus, WebRequest webRequest) {
      this.message = throwable.getMessage();
      this.status = httpStatus.value();
      this.error = httpStatus.getReasonPhrase();
      this.timestamp = createTimestamp();
      this.path = extractPath(webRequest);
    }

    @NotNull
    private String createTimestamp() {
      return formatter.format(OffsetDateTime.now(zoneId));
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
